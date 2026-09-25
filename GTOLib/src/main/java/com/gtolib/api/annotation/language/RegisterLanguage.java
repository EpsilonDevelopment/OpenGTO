package com.gtolib.api.annotation.language;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import kotlin.annotation.AnnotationTarget;
import kotlin.annotation.Target;

@Target(allowedTargets = AnnotationTarget.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterLanguage {
   String namePrefix() default "";

   String valuePrefix() default "";

   String key() default "";

   String en();

   String cn();
}
