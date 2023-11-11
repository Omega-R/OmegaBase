package com.omega_r.base.mvp.factory

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.omegar.mvp.MvpDelegateHolder
import com.omegar.mvp.MvpPresenter
import com.omegar.mvp.presenter.PresenterField
import com.omegar.mvp.presenter.PresenterType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

abstract class MvpBasePresenterField<P : MvpPresenter<*>, D : Any>(
    presenterClass: KClass<P>,
) : PresenterField<D, P>(presenterClass.presenterType, presenterClass) {

    companion object {

        private fun <T> T?.orThrow(presenterClass: KClass<*>) =
            this ?: throw NullPointerException("Presenter ${presenterClass.simpleName} not provided")

        private val <P : MvpPresenter<*>> KClass<P>.presenterType: PresenterType
            get() = MvpPresentersFactory.getPresenterType(this).orThrow(this)
    }

    private var presenter: P? = null

    protected abstract val D.bundle: Bundle?

    override fun bind(container: D, presenter: P) {
        this.presenter = presenter
    }

    override fun providePresenter(delegated: D): P {
        return MvpPresentersFactory.createPresenter(presenterClass, delegated.bundle).orThrow(presenterClass)
    }

    operator fun getValue(thisRef: D, property: KProperty<*>): P = presenter.orThrow(presenterClass)
}

class MvpActivityPresenterField<P : MvpPresenter<*>, D : Activity>(
    presenterClass: KClass<P>,
) : MvpBasePresenterField<P, D>(presenterClass) {

    override val D.bundle: Bundle?
        get() = intent.extras
}

class MvpFragmentPresenterField<P : MvpPresenter<*>, D : Fragment>(
    presenterClass: KClass<P>,
) : MvpBasePresenterField<P, D>(presenterClass) {

    override val D.bundle: Bundle?
        get() = arguments
}
