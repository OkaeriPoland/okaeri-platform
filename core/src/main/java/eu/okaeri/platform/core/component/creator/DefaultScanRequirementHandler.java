package eu.okaeri.platform.core.component.creator;

import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.annotation.Scan;
import eu.okaeri.platform.core.exception.BreakException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DefaultScanRequirementHandler implements ScanRequirementHandler {

    protected static final String REQ_CLASS = "class:";

    private final OkaeriPlatform platform;

    @Override
    public boolean meetsRequirement(@NonNull Class<?> parent, @NonNull Scan scan, @NonNull List<String> requires) {

        boolean meets = true;
        for (String requirement : requires) {
            if (requirement.startsWith(REQ_CLASS)) {
                String className = requirement.substring(REQ_CLASS.length());
                try {
                    Class.forName(className, true, parent.getClassLoader());
                } catch (ClassNotFoundException e) {
                    this.fail(parent, scan, requirement);
                    meets = false;
                }
            } else {
                throw new BreakException("Unsupported @Scan requirement: " + requirement);
            }
        }

        return meets;
    }

    protected void fail(@NonNull Class<?> parent, @NonNull Scan scan, @NonNull String requirement) {
        this.platform.log("Missing requirement " + requirement + " (skipping scan of '" + scan.value() + "' from " + parent + ")");
    }
}
