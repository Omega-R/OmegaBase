package com.omega_r.base.simple

import com.omega_r.base.data.OmegaRepository
import kotlinx.coroutines.channels.ReceiveChannel

class TestRepository(vararg sources: MainSource) : OmegaRepository<MainSource>() {

    fun testMethodReturn(strategy: OmegaRepository.Strategy, kek: String): ReceiveChannel<String> {
        return createChannel(strategy) { testMethodReturn(kek) }
    }

}