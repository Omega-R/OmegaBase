package com.omega_r.base.annotations

import java.lang.annotation.Inherited

/**
 * Created by Anton Knyazev on 10.04.19.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class OmegaWindowFlags(val addFlags: Int = 0, val clearFlags: Int = 0)