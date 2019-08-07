package com.omega_r.base.binders.managers

import java.util.*

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
class ResettableBindersManager : BindersManager() {

    // we synchronize to make sure the timing of a reset() call and new inits do not collide
    private val managedDelegates = LinkedList<ResettableLazy<*>>()

    private fun ResettableLazy<*>.registerInitedLazy() {
        synchronized(managedDelegates) {
            managedDelegates.add(this)
        }
    }

    override fun <V> createLazy(bindType: BindType, findInit: () -> V, objectInit: (V.() -> Unit)?): Lazy<V> {
        return when (bindType) {
            BindType.RESETTABLE, BindType.RESETTABLE_WITH_AUTO_INIT -> ResettableLazy(findInit, objectInit)
            else -> super.createLazy(bindType, findInit, objectInit)
        }
    }

    fun reset() {
        synchronized(managedDelegates) {
            if (managedDelegates.isNotEmpty()) {
                managedDelegates.forEach { it.reset() }
                managedDelegates.clear()
            }
        }
    }

    inner class ResettableLazy<V>(findObject: () -> V, objectInit: (V.() -> Unit)? = null) :
        BindLazy<V>(findObject, objectInit) {

        override val value: V
            get() {
                if (realValue === UNINITIALIZED_VALUE) {
                    registerInitedLazy()
                    initializer!!().also { value ->
                        realValue = value
                        objectInitilizer?.let {
                            objectInitilizer!!(value)
                        }
                    }
                }
                @Suppress("UNCHECKED_CAST")
                return realValue as V
            }


        internal fun reset() {
            realValue = UNINITIALIZED_VALUE
        }

    }

}