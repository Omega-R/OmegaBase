package com.omega_r.base.data.sources

import com.omega_r.base.errors.AppException

/**
 * Created by Anton Knyazev on 2019-05-28.
 */
interface Source  {

    val type: Type

    enum class Type {
        REMOTE, MEMORY_CACHE, FILE_CACHE, DEFAULT
    }

}
