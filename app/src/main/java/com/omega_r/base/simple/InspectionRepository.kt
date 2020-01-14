package com.omega_r.base.simple


import com.omega_r.base.annotations.AppOmegaRepository
import com.omega_r.base.data.OmegaRepository.Strategy
import kotlinx.coroutines.channels.ReceiveChannel

@AppOmegaRepository
interface InspectionRepository {

    val isAuth: Boolean?
    var age: Int

    suspend fun getInspectionsChannel(strategy: Strategy, index: Int): ReceiveChannel<String>

    suspend fun getInspections(strategy: Strategy, index: Int): String

    suspend fun getInspections(pair: Pair<String, Int>, second: Boolean)

    suspend fun pair(): Pair<String?, Int>?

    suspend fun returnInt(): Int?

}