package com.omega_r.base.data.sources

/**
 * Created by Anton Knyazev on 2019-05-28.
 */
interface Source  {

    val type: Type

    enum class Type {
        REMOTE, MEMORY_CACHE, FILE_CACHE, DEFAULT
    }

}
