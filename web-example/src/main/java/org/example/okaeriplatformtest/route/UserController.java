package org.example.okaeriplatformtest.route;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Component;
import eu.okaeri.platform.web.annotation.Handler;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpCode;
import org.example.okaeriplatformtest.persistence.User;
import org.example.okaeriplatformtest.persistence.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class UserController {

    @Inject
    private UserRepository userRepository;

    @Handler(path = "/user/{id}", type = HandlerType.GET)
    public void userGet(Context context) {

        UUID id = UUID.fromString(context.pathParam("id")); // FIXME: invalid uuid error handling
        Optional<User> dataOptional = this.userRepository.findByPath(id);

        if (dataOptional.isEmpty()) {
            context.status(HttpCode.NOT_FOUND).json(Map.of("error", HttpCode.NOT_FOUND));
            return;
        }

        context.json(dataOptional.get());
    }

    @Handler(path = "/user/{id}", type = HandlerType.DELETE)
    public void userDelete(Context context) {
        UUID id = UUID.fromString(context.pathParam("id")); // FIXME: invalid uuid error handling
        context.json(Map.of("status", this.userRepository.deleteByPath(id)));
    }

    @Handler(path = "/user", type = HandlerType.GET)
    public void userList(Context context) {
        context.json(this.userRepository.findAll());
    }

    @Handler(path = "/user", type = HandlerType.PUT)
    public void userPut(Context context) {
        context.json(this.userRepository.save(context.bodyAsClass(User.class)));
    }

    @Handler(path = "/user/createRandom", type = HandlerType.GET)
    public void createRandom(Context context) {
        User randomUser = this.userRepository.findOrCreateByPath(UUID.randomUUID());
        randomUser.setName("Random User " + ThreadLocalRandom.current().nextInt(10_000));
        randomUser.save();
        context.json(randomUser);
    }
}
