package com.omega_r.base.mvp.factory

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable.Creator
import android.os.SystemClock
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.omegar.libs.omegalaunchers.DialogFragmentLauncher
import com.omegar.libs.omegalaunchers.tools.BundlePair
import com.omegar.libs.omegalaunchers.tools.bundleOf
import com.omegar.mvp.MvpDelegate
import com.omegar.mvp.MvpPresenter
import com.omegar.mvp.MvpProcessor
import com.omegar.mvp.presenter.PresenterType
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass

class MvpDialogFragmentLauncher(
    fragmentClass: Class<out DialogFragment>,
    bundle: Bundle?,
    private val presenterType: PresenterType,
    private val presenterClass: KClass<out MvpPresenter<*>>,
) : DialogFragmentLauncher(fragmentClass, bundle) {

    constructor(
        fragmentClass: Class<out DialogFragment>,
        presenterType: PresenterType,
        presenterClass: KClass<out MvpPresenter<*>>,
        vararg extraParams: BundlePair,
    ) : this(fragmentClass, bundleOf(*extraParams), presenterType, presenterClass)

    @Suppress("UNCHECKED_CAST")
    constructor(parcel: Parcel) : this(
        fragmentClass = parcel.readSerializable() as Class<out DialogFragment>,
        bundle = parcel.readBundle(Bundle::class.java.classLoader),
        presenterType = parcel.readSerializable() as PresenterType,
        presenterClass = Reflection.createKotlinClass(parcel.readSerializable() as Class<out MvpPresenter<*>>) as KClass<out MvpPresenter<*>>) {
    }

    override fun createDialogFragment(): DialogFragment {
        val dialogFragment = super.createDialogFragment()

        if (MvpPresentersFactory.hasFactory(presenterClass)) {
            dialogFragment.arguments = dialogFragment.arguments?.let {
                Bundle(it).apply {
                    val uniqueKey = getIntOrPut(MvpDelegate.KEY_UNIQUE_KEY, SystemClock.elapsedRealtime()::toInt)

                    val delegateTag = MvpProcessor.generateDelegateTag(
                        Reflection.createKotlinClass(fragmentClass),
                        MvpDelegate::class,
                        uniqueKey
                    )

                    MvpProcessor.getOrCreateMvpPresenter(delegateTag, presenterType, presenterClass) {
                        MvpPresentersFactory.createPresenter(presenterClass, this)!!
                    }
                }
            }
        }

        return dialogFragment
    }

    private inline fun Bundle.getIntOrPut(key: String, factory: () -> Int): Int {
        return if (!containsKey(key)) {
            val newUniqueKey = factory()
            putInt(key, newUniqueKey)
            newUniqueKey
        } else {
            getInt(key)
        }
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeSerializable(presenterType)
        parcel.writeSerializable(presenterClass.java)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<MvpDialogFragmentLauncher> {

        override fun createFromParcel(parcel: Parcel): MvpDialogFragmentLauncher {
            return MvpDialogFragmentLauncher(parcel)
        }

        override fun newArray(size: Int): Array<MvpDialogFragmentLauncher?> {
            return arrayOfNulls(size)
        }
    }

}