package com.omega_r.base.binders.managers

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
open class BindersManager {

    open fun <V> bind(bindType: BindType = BindType.STATIC, init: () -> V) = lazy(LazyThreadSafetyMode.NONE) { init() }

    enum class BindType {
        RESETTABLE, STATIC
    }

}