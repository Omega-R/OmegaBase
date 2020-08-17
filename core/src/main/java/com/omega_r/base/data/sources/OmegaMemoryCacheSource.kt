package com.omega_r.base.data.sources

/**
 * Created by Anton Knyazev on 2019-05-29.
 */
abstract class OmegaMemoryCacheSource : CacheSource {

    override val type: Source.Type
        get() = Source.Type.MEMORY_CACHE

}