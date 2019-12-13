package com.omega_r.base.data

import com.omega_r.base.data.sources.Source
import kotlinx.coroutines.CoroutineScope

/**
 * Created by Anton Knyazev on 2019-12-13.
 */
class RepositoryList<T, S : Source>(
    items: List<T>,
    private val repository: OmegaRepository<S>,
    private val block: suspend S.(item: T) -> Unit,
    private val scope: CoroutineScope
) : MutableList<T> {

    private val wrapperList = ArrayList<RepositoryProperty<T, CoroutineScope, S>>(map {
        it.toProperty()
    })

    override val size: Int
        get() = wrapperList.size

    override fun contains(element: T): Boolean {
        return indexOf(element) >= 0
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return elements.firstOrNull { !contains(it) } == null
    }

    override fun get(index: Int): T = wrapperList[index].value

    override fun indexOf(element: T): Int {
        return wrapperList.indexOfFirst { it.value == element }
    }

    override fun isEmpty(): Boolean = wrapperList.isEmpty()

    override fun iterator(): MutableIterator<T> = wrapperList.map { it.value }.toMutableList().iterator()

    override fun lastIndexOf(element: T): Int = wrapperList.indexOfLast { it.value == element }

    override fun add(element: T): Boolean {
        return wrapperList.add(element.toProperty())
    }

    override fun add(index: Int, element: T) {
        return wrapperList.add(index, element.toProperty())
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        return wrapperList.addAll(index, elements.map { it.toProperty() })
    }

    override fun addAll(elements: Collection<T>): Boolean {
        return wrapperList.addAll(elements.map { it.toProperty() })
    }

    override fun clear() {
        wrapperList.clear()
    }

    override fun listIterator(): MutableListIterator<T> {
        return wrapperList.map { it.value }.toMutableList().listIterator()
    }

    override fun listIterator(index: Int): MutableListIterator<T> {
        return wrapperList.map { it.value }.toMutableList().listIterator(index)
    }

    override fun remove(element: T): Boolean {
        val index = indexOf(element)
        if (index >= 0) {
            wrapperList.removeAt(index)
            return true
        }
        return false
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        return elements.fold(false, { prev: Boolean, item: T ->
            remove(item) || prev
        })
    }

    override fun removeAt(index: Int): T {
        return wrapperList.removeAt(index).value

    }

    override fun retainAll(elements: Collection<T>): Boolean {
        return wrapperList.fold(false, { prev: Boolean, item ->
            (if (item.value !in elements) wrapperList.remove(item) else false) || prev
        })
    }

    override fun set(index: Int, element: T): T {
        return wrapperList.set(index, element.toProperty()).value
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
        return RepositoryList(wrapperList.subList(fromIndex, toIndex).map { it.value }, repository, block, scope)
    }

    private fun T.toProperty(): RepositoryProperty<T, CoroutineScope, S> {
        return RepositoryProperty(repository = repository, block = block, value = this)
    }

    private val RepositoryProperty<T, CoroutineScope, S>.value: T
        get() {
            return getValue(scope, ::size)
        }

}
