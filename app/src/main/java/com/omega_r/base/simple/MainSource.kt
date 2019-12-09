package com.omega_r.base.simple

import com.omega_r.base.annotations.AppOmegaRepository
import com.omega_r.base.annotations.SuspendMethod
import com.omega_r.base.data.sources.Source

@AppOmegaRepository
interface MainSource : Source {

    val per: String

    fun testMethod()

    @SuspendMethod
    fun testMethodReturn(kek: String?): String

}