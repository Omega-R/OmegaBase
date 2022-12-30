package com.omega_r.base.mvp.factory

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.omegar.libs.omegalaunchers.ActivityLauncher
import com.omegar.libs.omegalaunchers.DialogFragmentLauncher
import com.omegar.libs.omegalaunchers.FragmentLauncher
import com.omegar.libs.omegalaunchers.tools.BundlePair
import com.omegar.mvp.MvpAppCompatActivity
import com.omegar.mvp.MvpAppCompatDialogFragment
import com.omegar.mvp.MvpAppCompatFragment
import com.omegar.mvp.MvpBottomSheetDialogFragment
import com.omegar.mvp.MvpDelegateHolder
import com.omegar.mvp.MvpPresenter
import com.omegar.mvp.presenter.PresenterType
import kotlin.reflect.KClass

abstract class MvpPresenterFactory<Presenter : MvpPresenter<*>>(
    internal val presenterType: PresenterType,
    private val presenterClass: KClass<Presenter>,
) {

    init {
        @Suppress("LeakingThis")
        MvpPresentersFactory.addFactory(presenterClass, this)
    }

    abstract fun createPresenter(bundle: Bundle?): Presenter

    protected fun <T : Activity> createLauncher(
        clazz: KClass<T>,
        vararg extraParams: BundlePair,
    ): ActivityLauncher = MvpActivityLauncher(clazz.java, presenterType, presenterClass, *extraParams)

    protected fun <T : DialogFragment> createLauncher(
        clazz: KClass<T>,
        vararg extraParams: BundlePair,
    ): DialogFragmentLauncher =
        MvpDialogFragmentLauncher(clazz.java, presenterType, presenterClass, *extraParams)


    protected fun <T : Fragment> createLauncher(
        clazz: KClass<T>,
        vararg extraParams: BundlePair,
    ): FragmentLauncher =
        MvpFragmentLauncher(clazz.java, presenterType, presenterClass, *extraParams)

    protected fun MvpAppCompatActivity.createPresenterField(): MvpBasePresenterField<Presenter, MvpAppCompatActivity> {
        return createPresenterField {
            MvpActivityPresenterField(presenterClass)
        }
    }

    protected fun MvpAppCompatFragment.createPresenterField(): MvpBasePresenterField<Presenter, MvpAppCompatFragment> {
        return createPresenterField {
            MvpFragmentPresenterField(presenterClass)
        }
    }

    protected fun MvpAppCompatDialogFragment.createPresenterField(): MvpBasePresenterField<Presenter, MvpAppCompatDialogFragment> {
        return createPresenterField {
            MvpFragmentPresenterField(presenterClass)
        }
    }

    protected fun MvpBottomSheetDialogFragment.createPresenterField(): MvpBasePresenterField<Presenter, MvpBottomSheetDialogFragment> {
        return createPresenterField {
            MvpFragmentPresenterField(presenterClass)
        }
    }

    private inline fun <D : MvpDelegateHolder<D>> D.createPresenterField(
        factory: () -> MvpBasePresenterField<Presenter, D>,
    ): MvpBasePresenterField<Presenter, D> = factory().apply {
        mvpDelegate.addCustomPresenterFields(this)
    }

}