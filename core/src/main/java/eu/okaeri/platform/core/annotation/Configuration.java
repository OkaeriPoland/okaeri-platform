package eu.okaeri.platform.core.annotation;

import eu.okaeri.configs.configurer.Configurer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {

    String path() default "config.yml";
    Class<? extends Configurer> provider() default DEFAULT.class;
    boolean defaultNotNull() default true;

    abstract class DEFAULT extends Configurer {
    }
}
