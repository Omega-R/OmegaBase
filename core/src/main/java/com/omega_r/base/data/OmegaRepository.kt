package com.omega_r.base.data

interface OmegaRepository {

    var mockMode: Boolean

    fun clearCache()

    enum class Strategy {

        ONLY_REMOTE,
        REMOTE_ELSE_CACHE,
        CACHE_ELSE_REMOTE,
        CACHE_AND_REMOTE,
        MEMORY_ELSE_CACHE_AND_REMOTE,
        ONLY_CACHE

    }

}