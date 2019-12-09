package com.omega_r.base.simple

import com.omega_r.base.annotations.OmegaRepository
import com.omega_r.base.data.sources.Source

@OmegaRepository
interface MainSource : Source {

    val per: String

    fun testMethod()

    fun testMethodReturn(kek: String?): String

}