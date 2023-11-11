package com.omega_r.base.mvp.factory

import android.os.Bundle
import com.omegar.mvp.MvpPresenter
import com.omegar.mvp.presenter.PresenterType
import kotlin.reflect.KClass

object MvpPresentersFactory {

    private val factoryMap = mutableMapOf<KClass<out MvpPresenter<*>>, MvpPresenterFactory<*>>()

    fun hasFactory(presenterClass: KClass<out MvpPresenter<*>>) = factoryMap.containsKey(presenterClass)

    fun <P : MvpPresenter<*>> addFactory(presenterClass: KClass<P>, factory: MvpPresenterFactory<P>) {
        factoryMap[presenterClass] = factory
    }

    fun <P : MvpPresenter<*>> getPresenterType(presenterClass: KClass<P>): PresenterType? =
        factoryMap[presenterClass]?.presenterType

    @Suppress("UNCHECKED_CAST")
    fun <P : MvpPresenter<*>> createPresenter(presenterClass: KClass<P>, bundle: Bundle?): P? =
        factoryMap[presenterClass]?.createPresenter(bundle) as P?
}