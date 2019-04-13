package com.omega_r.base.binders.managers

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
open class BindersManager {

    private val autoInitList = mutableListOf<Lazy<*>>()

    fun <V> bind(bindType: BindType = BindType.STATIC, init: () -> V): Lazy<V> {
        val lazy = createLazy(bindType, init)
        if (bindType == BindType.RESETTABLE_WITH_AUTO_INIT) {
            autoInitList += lazy
        }
        return lazy
    }

    protected open fun <V> createLazy(bindType: BindType = BindType.STATIC, init: () -> V): Lazy<V> {
        return lazy(LazyThreadSafetyMode.NONE) { init() }
    }

    fun doAutoInit() {
        autoInitList.forEach { it.value }
    }

    enum class BindType {
        STATIC, RESETTABLE, RESETTABLE_WITH_AUTO_INIT
    }

}