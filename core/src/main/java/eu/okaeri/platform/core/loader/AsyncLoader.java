package eu.okaeri.platform.core.loader;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class AsyncLoader {
    private final String name;
    private final Runnable runnable;
    private Thread thread;
    private boolean done;
}
