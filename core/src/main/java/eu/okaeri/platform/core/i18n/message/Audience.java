package eu.okaeri.platform.core.i18n.message;

import eu.okaeri.i18n.message.Message;
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
public class Audience implements Closeable {

    protected final Collection<Object> targets;
    protected final List<MessageDispatcher<Message>> messages = new ArrayList<>();

    public Audience andOf(@NonNull Object... targets) {
        this.targets.addAll(Arrays.asList(targets));
        return this;
    }

    public Audience andOf(@NonNull Collection<Object> targets) {
        this.targets.addAll(targets);
        return this;
    }

    public Audience accept(@NonNull MessageDispatcher<Message> dispatcher) {
        this.messages.add(dispatcher);
        return this;
    }

    @SafeVarargs
    public final Audience accept(@NonNull MessageDispatcher<Message>... dispatchers) {
        if (dispatchers.length == 0) {
            return this;
        }
        if (dispatchers.length == 1) {
            this.messages.add(dispatchers[0]);
            return this;
        }
        this.messages.addAll(Arrays.asList(dispatchers));
        return this;
    }

    @Override
    public void close() {
        for (Object target : this.targets) {
            for (MessageDispatcher<Message> message : this.messages) {
                message.sendTo(target);
            }
        }
    }
}
