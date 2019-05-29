package com.omega_r.base.data.sources

/**
 * Created by Anton Knyazev on 12.04.19.
 */
interface CacheSource: Source {

    fun update(data: Any?)

    fun clear()

}