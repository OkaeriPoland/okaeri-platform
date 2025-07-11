package eu.okaeri.platform.core.plan.task;

import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.component.creator.DefaultScanRequirementHandler;
import eu.okaeri.platform.core.component.creator.ScanRequirementHandler;
import eu.okaeri.platform.core.plan.ExecutionTask;

import java.util.Optional;

public class ScanRequirementSetupTask implements ExecutionTask<OkaeriPlatform> {

    @Override
    public void execute(OkaeriPlatform platform) {

        Optional<ScanRequirementHandler> scanRequirementHandler = platform.getInjector().get("scanRequirementHandler", ScanRequirementHandler.class);
        if (scanRequirementHandler.isPresent()) {
            platform.getCreator().setScanRequirementHandler(scanRequirementHandler.get());
            return;
        }

        platform.getCreator().setScanRequirementHandler(new DefaultScanRequirementHandler(platform));
    }
}
