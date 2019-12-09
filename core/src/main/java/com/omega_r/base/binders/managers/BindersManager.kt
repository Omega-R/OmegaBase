package com.omega_r.base.binders.managers

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
open class BindersManager {

    private val autoInitList = mutableListOf<Lazy<*>>()

    fun <V> bind(bindType: BindType = BindType.STATIC, findInit: () -> V, objectInit: (V.() -> Unit)? = null): Lazy<V> {
        val lazy = createLazy(bindType, findInit, objectInit)
        if (bindType == BindType.RESETTABLE_WITH_AUTO_INIT) {
            autoInitList += lazy
        }
        return lazy
    }

    protected open fun <V> createLazy(
        bindType: BindType = BindType.STATIC,
        findInit: () -> V,
        objectInit: (V.() -> Unit)? = null
    ): Lazy<V> {
        return BindLazy(findInit, objectInit)
    }

    fun doAutoInit() {
        autoInitList.forEach { it.value }
    }

    enum class BindType {
        STATIC, RESETTABLE, RESETTABLE_WITH_AUTO_INIT
    }


    open class BindLazy<T>(initializer: () -> T, objectInitilizer: (T.() -> Unit)?) : Lazy<T> {
        protected var initializer: (() -> T)? = initializer
        @Suppress("CanBePrimaryConstructorProperty")
        protected var objectInitilizer: (T.() -> Unit)? = objectInitilizer


        protected var realValue: Any? = UNINITIALIZED_VALUE

        override val value: T
            get() {
                if (realValue === UNINITIALIZED_VALUE) {
                    initializer!!().also { value ->
                        realValue = value
                        initializer = null
                        objectInitilizer?.let {
                            objectInitilizer!!(value)
                            objectInitilizer = null
                        }
                    }
                }
                @Suppress("UNCHECKED_CAST")
                return realValue as T
            }

        override fun isInitialized(): Boolean = realValue !== UNINITIALIZED_VALUE

        override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."

        private fun writeReplace(): Any = InitializedLazyImpl(value)

        protected object UNINITIALIZED_VALUE
    }

    internal class InitializedLazyImpl<out T>(override val value: T) : Lazy<T> {

        override fun isInitialized(): Boolean = true

        override fun toString(): String = value.toString()

    }


}