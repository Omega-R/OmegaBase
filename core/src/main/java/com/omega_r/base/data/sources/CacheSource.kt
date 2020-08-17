package com.omega_r.base.data.sources

/**
 * Created by Anton Knyazev on 12.04.19.
 */
interface CacheSource : Source {

    fun updateItem(data: Any?)

    fun updateItems(data: List<*>?) {
        data?.forEach { updateItem(it) }
    }

    suspend fun update(data: Any?) {
        when (data) {
            is Unit -> {
                // nothing
            }
            is List<*> -> updateItems(data)
            else -> updateItem(data)
        }
    }

    fun clear()

}