package eu.okaeri.platform.bukkit.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(WithBean.List.class)
public @interface WithBean {

    Class<?> value();
    boolean register() default true;

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        WithBean[] value();
    }
}
