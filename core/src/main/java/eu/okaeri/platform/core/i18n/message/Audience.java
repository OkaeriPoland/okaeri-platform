package eu.okaeri.platform.core.i18n.message;

import eu.okaeri.i18n.message.MessageDispatcher;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class Audience<M> implements Closeable {

    protected final Collection<Object> targets;
    protected final List<MessageDispatcher<M>> messages = new ArrayList<>();

    public Audience<M> andOf(@NonNull Object... targets) {
        this.targets.addAll(Arrays.asList(targets));
        return this;
    }

    public Audience<M> andOf(@NonNull Collection<Object> targets) {
        this.targets.addAll(targets);
        return this;
    }

    public Audience<M> accept(@NonNull MessageDispatcher<M> dispatcher) {
        this.messages.add(dispatcher);
        return this;
    }

    @SuppressWarnings("unchecked")
    public Audience<M> accept(@NonNull MessageDispatcher<M>... dispatchers) {
        this.messages.addAll(Arrays.asList(dispatchers));
        return this;
    }

    @Override
    public void close() {
        for (Object target : this.targets) {
            for (MessageDispatcher<M> message : this.messages) {
                message.sendTo(target);
            }
        }
    }
}
