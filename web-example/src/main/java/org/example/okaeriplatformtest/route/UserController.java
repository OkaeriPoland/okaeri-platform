package org.example.okaeriplatformtest.route;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Component;
import eu.okaeri.platform.web.annotation.*;
import eu.okaeri.platform.web.meta.context.RequestContext;
import org.example.okaeriplatformtest.persistence.User;
import org.example.okaeriplatformtest.persistence.UserRepository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
// prefix for paths of RequestHandlers inside this component
@Controller(path = "/user")
public class UserController {

    @Inject
    private UserRepository userRepository;

    @GetHandler(path = "/{id}", permittedRoles = {"USER_READ"})
    public void userGet(RequestContext context, @PathParam("id") UUID id) {
        // using #jsonOpt method from RequestContext instead of #json
        // results in serialized object or in the case of empty
        // optional in the {"error": "NOT_FOUND"}
        context.jsonOpt(this.userRepository.findByPath(id));
    }

    @DeleteHandler(path = "/{id}", permittedRoles = {"USER_WRITE"})
    public void userDelete(RequestContext context, @PathParam("id") UUID id) {
        context.json(Map.of("status", this.userRepository.deleteByPath(id)));
    }

    @GetHandler(permittedRoles = {"USER_READ"})
    public void userList(RequestContext context) {
        context.json(this.userRepository.findAll());
    }

    @PutHandler(permittedRoles = {"USER_WRITE"})
    public void userPut(RequestContext context) {
        context.json(this.userRepository.save(context.bodyAsClass(User.class)));
    }

    @GetHandler(path = "/createRandom", permittedRoles = {"USER_WRITE"})
    public void createRandom(RequestContext context) {
        User randomUser = this.userRepository.findOrCreateByPath(UUID.randomUUID());
        randomUser.setName("Random User " + ThreadLocalRandom.current().nextInt(10_000));
        randomUser.save();
        context.json(randomUser);
    }
}
