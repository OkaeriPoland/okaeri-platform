package eu.okaeri.platform.bukkit.persistence.inmemory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class InMemoryPlayerTask<T extends TrackedDocument> implements Runnable {

    private final InMemoryPlayerHandler<T> handler;

    @Override
    public void run() {
        this.handler.saveAll();
    }
}
