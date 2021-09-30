package eu.okaeri.platform.web.annotation;

import io.javalin.http.HandlerType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestHandler {
    String path() default "";
    HandlerType type() default HandlerType.GET;
    String[] permittedRoles() default {};
}
