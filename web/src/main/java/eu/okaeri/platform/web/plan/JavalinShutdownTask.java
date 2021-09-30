package eu.okaeri.platform.web.plan;

import eu.okaeri.platform.core.plan.ExecutionTask;
import eu.okaeri.platform.web.OkaeriWebApplication;

public class JavalinShutdownTask implements ExecutionTask<OkaeriWebApplication> {

    @Override
    public void execute(OkaeriWebApplication platform) {
        platform.getJavalin().stop();
    }
}
