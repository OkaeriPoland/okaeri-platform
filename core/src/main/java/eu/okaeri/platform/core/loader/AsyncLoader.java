package eu.okaeri.platform.core.loader;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class AsyncLoader {
    @NonNull private final String name;
    @NonNull private final Runnable runnable;
    private Thread thread;
    private boolean done;
}
