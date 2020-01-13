package com.omega_r.base.simple


import com.omega_r.base.annotations.OmegaRepository
import com.omega_r.base.data.OmegaRepository.Strategy
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

@OmegaRepository
interface InspectionRepository {

    fun getInspectionsChannel(strategy: Strategy, index: Int): ReceiveChannel<String>

    fun getInspections(strategy: Strategy, index: Int): String

//    fun getInspectionsPair(strategy: Strategy, index: Int): Channel<Pair<String, Int>>

    suspend fun getInspections(pair: Pair<String, Int>, second: Boolean)

    fun clear()

    fun pair(): Pair<String, Int>

    fun kek(): Int

}


//interface InspectionSource: Source {
//
//    suspend fun getInspections(index: Int): String
//
//    suspend fun getInspections(): String
//
//}
//
//class OmegaInspectionRepository(errorHandler: ErrorHandler, vararg sources: InspectionSource) : OmegaRepository<InspectionSource>(errorHandler, *sources), InspectionRepository {
//
//    override fun getInspectionsChannel(strategy: Strategy, index: Int): ReceiveChannel<String> {
//        return createChannel(strategy) { getInspections(index) }
//    }
//
//    override suspend fun getInspections(): String {
//        return createChannel(Strategy.REMOTE_ELSE_CACHE) { getInspections() }.receive()
//    }
//
//}