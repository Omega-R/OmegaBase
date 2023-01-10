package com.omega_r.base.mvp.factory

import android.app.Activity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable.Creator
import android.os.SystemClock
import androidx.fragment.app.Fragment
import com.omegar.libs.omegalaunchers.FragmentLauncher
import com.omegar.libs.omegalaunchers.tools.BundlePair
import com.omegar.libs.omegalaunchers.tools.bundleOf
import com.omegar.mvp.MvpDelegate
import com.omegar.mvp.MvpPresenter
import com.omegar.mvp.MvpProcessor
import com.omegar.mvp.presenter.PresenterType
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass

class MvpFragmentLauncher(
    fragmentClass: Class<out Fragment>,
    private val bundle: Bundle?,
    private val presenterType: PresenterType,
    private val presenterClass: KClass<out MvpPresenter<*>>,
) : FragmentLauncher(fragmentClass, bundle), MvpLauncher {

    private var uniqueKey: Int = generateUniqueKey()

    constructor(
        fragmentClass: Class<out Fragment>,
        presenterType: PresenterType,
        presenterClass: KClass<out MvpPresenter<*>>,
        vararg extraParams: BundlePair,
    ) : this(fragmentClass, bundleOf(*extraParams), presenterType, presenterClass)

    @Suppress("UNCHECKED_CAST")
    constructor(parcel: Parcel) : this(
        fragmentClass = parcel.readSerializable() as Class<out Fragment>,
        bundle = parcel.readBundle(Bundle::class.java.classLoader),
        presenterType = parcel.readSerializable() as PresenterType,
        presenterClass = Reflection.createKotlinClass(parcel.readSerializable() as Class<out MvpPresenter<*>>) as KClass<out MvpPresenter<*>>) {
    }

    override fun createFragment(): Fragment {
        val fragment = super.createFragment()

        if (preparePresenter()) {
            val bundle = fragment.arguments ?: Bundle()
            bundle.putInt(MvpDelegate.KEY_UNIQUE_KEY, uniqueKey)
            fragment.arguments = bundle
            uniqueKey = generateUniqueKey()
        }

        return fragment
    }

    override fun preparePresenter(): Boolean {
        if (MvpPresentersFactory.hasFactory(presenterClass)) {
            val delegateTag = MvpProcessor.generateDelegateTag(
                Reflection.createKotlinClass(fragmentClass),
                MvpDelegate::class,
                uniqueKey
            )

            MvpProcessor.getOrCreateMvpPresenter(delegateTag, presenterType, presenterClass) {
                MvpPresentersFactory.createPresenter(presenterClass, bundle)!!
            }
            return true
        }
        return false
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeSerializable(presenterType)
        parcel.writeSerializable(presenterClass.java)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<MvpFragmentLauncher> {

        override fun createFromParcel(parcel: Parcel): MvpFragmentLauncher {
            return MvpFragmentLauncher(parcel)
        }

        override fun newArray(size: Int): Array<MvpFragmentLauncher?> {
            return arrayOfNulls(size)
        }
    }
}