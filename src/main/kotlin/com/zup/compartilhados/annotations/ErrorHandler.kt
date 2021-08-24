package com.zup.compartilhados.annotations

import io.micronaut.aop.Around
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(CLASS, FUNCTION)
@Around
annotation class ErrorHandler()
