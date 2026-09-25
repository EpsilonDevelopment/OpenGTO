package com.gtolib.api.annotation.dynamic;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import kotlin.annotation.AnnotationTarget;
import kotlin.annotation.Target;

@Target(allowedTargets = AnnotationTarget.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicInitialValue {
   String key();

   String typeKey() default "default";

   String en();

   String cn();

   String enComment() default "";

   String cnComment() default "";

   String easyValue();

   String normalValue();

   String expertValue();
}
