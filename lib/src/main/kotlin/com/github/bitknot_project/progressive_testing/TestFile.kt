package com.github.bitknot_project.progressive_testing

import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class TestFile(val value: String)
