package eu.okaeri.platform.core.plan;

import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.plan.task.PlannedMethodTask;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class ExecutionPlan {

    private final OkaeriPlatform platform;
    private final Map<ExecutionPhase, List<ExecutionTask<?>>> tasks = new ConcurrentHashMap<>();
    private final Map<ExecutionTask<?>, List<String>> taskDependencies = new ConcurrentHashMap<>();
    private final Map<ExecutionTask<?>, String> taskNames = new ConcurrentHashMap<>();

    public void add(@NonNull ExecutionPhase phase, @NonNull ExecutionTask<?> task) {
        this.add(phase, task, task.getClass().getName(), Collections.emptyList());
    }

    public void add(@NonNull ExecutionPhase phase, @NonNull ExecutionTask<?> task, @NonNull String name) {
        this.add(phase, task, name, Collections.emptyList());
    }

    public void add(@NonNull ExecutionPhase phase, @NonNull ExecutionTask<?> task, @NonNull String taskName, @NonNull List<String> taskDependencies) {
        this.tasks.computeIfAbsent(phase, (t) -> new ArrayList<>()).add(task);
        this.taskDependencies.put(task, taskDependencies);
        this.taskNames.put(task, taskName);
    }

    public void addMethods(@NonNull Object parent) {
        for (Method declaredMethod : parent.getClass().getDeclaredMethods()) {
            Planned[] annotations = declaredMethod.getAnnotationsByType(Planned.class);
            if (annotations.length != 1) {
                continue;
            }
            Planned planned = annotations[0];
            ExecutionPhase phase = planned.value();
            declaredMethod.setAccessible(true);
            this.add(phase, new PlannedMethodTask(parent, declaredMethod));
        }
    }

    public ExecutionResult execute() {
        return this.execute(System.currentTimeMillis());
    }

    public ExecutionResult execute(long startTime) {
        return this.execute(System.currentTimeMillis(), ExecutionPhase.PRE_SHUTDOWN, ExecutionPhase.SHUTDOWN, ExecutionPhase.POST_SHUTDOWN);
    }

    public ExecutionResult execute(long startTime, @NonNull ExecutionPhase... blacklistedPhases) {

        long totalTasks = 0;
        Set<ExecutionPhase> blacklistedPhaseSet = new HashSet<>(Arrays.asList(blacklistedPhases));

        for (ExecutionPhase phase : ExecutionPhase.values()) {
            if (blacklistedPhaseSet.contains(phase)) {
                continue;
            }
            totalTasks += this.execute(phase);
        }

        long took = System.currentTimeMillis() - startTime;
        return new ExecutionResult(this, startTime, totalTasks, took);
    }

    public long execute(@NonNull List<ExecutionPhase> phases) {
        return phases.stream()
                .map(this::execute)
                .mapToInt(Long::intValue)
                .sum();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public long execute(@NonNull ExecutionPhase phase) {

        if (!this.tasks.containsKey(phase)) {
            return 0;
        }

        long totalTasks = 0;
        List<ExecutionTask<?>> targetTasks = this.tasks.get(phase);

        for (ExecutionTask targetTask : targetTasks) {
            try {
                targetTask.execute(this.platform);
            } catch (Exception exception) {
                throw new RuntimeException("Platform task fail - " + targetTask.getClass().getSimpleName() + " [phase=" + phase + "]", exception);
            }
            totalTasks++;
        }

        return totalTasks;
    }

    public static <T extends OkaeriPlatform> ExecutionResult dispatch(@NonNull T platform) {
        long start = System.currentTimeMillis();
        ExecutionPlan plan = new ExecutionPlan(platform);
        platform.plan(plan);
        plan.addMethods(platform);
        return plan.execute(start);
    }
}
