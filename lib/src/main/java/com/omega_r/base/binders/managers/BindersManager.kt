package com.omega_r.base.binders.managers

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
open class BindersManager {

    private val autoInitList = mutableListOf<Lazy<*>>()

    open fun <V> bind(bindType: BindType = BindType.STATIC, init: () -> V): Lazy<V> {
        val lazy = lazy(LazyThreadSafetyMode.NONE) { init() }
        if (bindType == BindType.RESETTABLE_WITH_AUTO_INIT) {
            autoInitList += lazy
        }
        return lazy
    }

    fun doAutoInit() {
        autoInitList.forEach { it.value }
    }

    enum class BindType {
        STATIC, RESETTABLE, RESETTABLE_WITH_AUTO_INIT
    }

}