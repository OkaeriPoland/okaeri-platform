package eu.okaeri.platform.core.plan;

import lombok.Data;

@Data
public class ExecutionResult {
    private final ExecutionPlan plan;
    private final long startTime;
    private final long completedTasks;
    private final long totalMillis;
}
