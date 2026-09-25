package com.gtolib.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import kotlin.annotation.AnnotationTarget;
import kotlin.annotation.Target;

@Target(allowedTargets = AnnotationTarget.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SyncedManager {
}
