package eu.okaeri.platform.core.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Scan.List.class)
public @interface Scan {

    String value() default "";
    String[] exclusions() default {};
    boolean deep() default false;

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        Scan[] value();
    }
}
