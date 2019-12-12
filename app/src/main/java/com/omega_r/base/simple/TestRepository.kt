package com.omega_r.base.simple

import com.omega_r.base.data.OmegaRepository
import com.omega_r.base.errors.ErrorHandler
import kotlinx.coroutines.channels.ReceiveChannel

class TestRepository(errorHandler: ErrorHandler, vararg sources: MainSource) : OmegaRepository<MainSource>(errorHandler) {

    fun testMethodReturn(strategy: OmegaRepository.Strategy, kek: String): ReceiveChannel<String> {
        return createChannel(strategy) { testMethodReturn(kek) }
    }

}