package eu.okaeri.platform.core.component.creator;

import eu.okaeri.platform.core.annotation.Scan;
import lombok.NonNull;

import java.util.List;

public interface ScanRequirementHandler {

    boolean meetsRequirement(@NonNull Class<?> parent, @NonNull Scan scan, @NonNull List<String> requires);
}
