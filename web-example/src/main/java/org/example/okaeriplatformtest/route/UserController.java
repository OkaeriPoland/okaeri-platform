package org.example.okaeriplatformtest.route;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Component;
import eu.okaeri.platform.web.annotation.*;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import org.example.okaeriplatformtest.persistence.User;
import org.example.okaeriplatformtest.persistence.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
// prefix for paths of RequestHandlers inside this component
@Controller(path = "/user")
public class UserController {

    @Inject
    private UserRepository userRepository;

    @GetHandler(path = "/{id}", permittedRoles = {"USER_READ"})
    public void userGet(Context context, @PathParam("id") UUID id) {

        Optional<User> dataOptional = this.userRepository.findByPath(id);
        if (dataOptional.isEmpty()) {
            context.status(HttpCode.NOT_FOUND).json(Map.of("error", HttpCode.NOT_FOUND));
            return;
        }

        context.json(dataOptional.get());
    }

    @DeleteHandler(path = "/{id}", permittedRoles = {"USER_WRITE"})
    public void userDelete(Context context, @PathParam("id") UUID id) {
        context.json(Map.of("status", this.userRepository.deleteByPath(id)));
    }

    @GetHandler(permittedRoles = {"USER_READ"})
    public void userList(Context context) {
        context.json(this.userRepository.findAll());
    }

    @PutHandler(permittedRoles = {"USER_WRITE"})
    public void userPut(Context context) {
        context.json(this.userRepository.save(context.bodyAsClass(User.class)));
    }

    @GetHandler(path = "/createRandom", permittedRoles = {"USER_WRITE"})
    public void createRandom(Context context) {
        User randomUser = this.userRepository.findOrCreateByPath(UUID.randomUUID());
        randomUser.setName("Random User " + ThreadLocalRandom.current().nextInt(10_000));
        randomUser.save();
        context.json(randomUser);
    }
}
