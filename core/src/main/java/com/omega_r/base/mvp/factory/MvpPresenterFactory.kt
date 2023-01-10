package com.omega_r.base.mvp.factory

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.omega_r.libs.extensions.list.toArrayList
import com.omegar.libs.omegalaunchers.ActivityLauncher
import com.omegar.libs.omegalaunchers.DialogFragmentLauncher
import com.omegar.libs.omegalaunchers.FragmentLauncher
import com.omegar.libs.omegalaunchers.tools.BundlePair
import com.omegar.libs.omegalaunchers.tools.put
import com.omegar.mvp.MvpAppCompatActivity
import com.omegar.mvp.MvpAppCompatDialogFragment
import com.omegar.mvp.MvpAppCompatFragment
import com.omegar.mvp.MvpBottomSheetDialogFragment
import com.omegar.mvp.MvpDelegateHolder
import com.omegar.mvp.MvpPresenter
import com.omegar.mvp.presenter.PresenterType
import java.io.Serializable
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

    @Suppress("UNCHECKED_CAST")
    protected infix fun <T> Bundle?.get(key: String) = this?.get(key) as T

    protected infix fun <T : Serializable?> String.putS(value: List<T>?) = this put value?.toArrayList()

    protected infix fun <T : Parcelable?> String.putP(value: List<T>?) =
        BundlePair { it.putParcelableArrayList(this, value?.toArrayList()) }

    protected fun <E> Set<E>?.toArrayList() = this?.let { ArrayList(this) }

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

