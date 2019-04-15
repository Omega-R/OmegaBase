package com.omega_r.base.binders.managers

import java.util.*
import kotlin.reflect.KProperty

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
class ResettableBindersManager: BindersManager() {

    // we synchronize to make sure the timing of a reset() call and new inits do not collide
    private val managedDelegates = LinkedList<ResettableLazy<*>>()

    private fun register(managed: ResettableLazy<*>) {
        synchronized(managedDelegates) {
            managedDelegates.add(managed)
        }
    }

    override fun <V> createLazy(bindType: BindType, init: () -> V): Lazy<V> {
        return when (bindType) {
            BindType.RESETTABLE, BindType.RESETTABLE_WITH_AUTO_INIT -> ResettableLazy(init)
            else -> super.createLazy(bindType, init)
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

    inner class ResettableLazy<V>(val init: () -> V): Lazy<V>  {

        override val value: V by this

        @Volatile
        private var lazyHolder = makeInitBlock()

        operator fun getValue(thisRef: Any?, property: KProperty<*>): V {
            return lazyHolder.value
        }

        override fun isInitialized(): Boolean = lazyHolder.isInitialized()

        internal fun reset() {
            lazyHolder = makeInitBlock()
        }

        private fun makeInitBlock(): Lazy<V> {
            return lazy(LazyThreadSafetyMode.NONE) {
                register(this)
                init()
            }
        }
    }

}