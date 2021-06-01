package eu.okaeri.platform.core.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(External.List.class)
public @interface External {

    String name() default "";
    Class<?> type();
    Class<?> of();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        External[] value();
    }
}
