package com.omega_r.base.mvp.factory

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable.Creator
import android.os.SystemClock
import com.omegar.libs.omegalaunchers.ActivityLauncher
import com.omegar.libs.omegalaunchers.tools.BundlePair
import com.omegar.libs.omegalaunchers.tools.bundleOf
import com.omegar.mvp.MvpDelegate
import com.omegar.mvp.MvpPresenter
import com.omegar.mvp.MvpProcessor.generateDelegateTag
import com.omegar.mvp.MvpProcessor.getOrCreateMvpPresenter
import com.omegar.mvp.presenter.PresenterType
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass

class MvpActivityLauncher(
    activityClass: Class<out Activity>,
    private val bundle: Bundle?,
    flags: Int = 0,
    private val presenterType: PresenterType,
    private val presenterClass: KClass<out MvpPresenter<*>>,
) : ActivityLauncher(activityClass, bundle, flags), MvpLauncher {

    private var uniqueKey: Int = generateUniqueKey()

    @Suppress("UNCHECKED_CAST")
    constructor(parcel: Parcel) : this(
        activityClass = parcel.readSerializable() as Class<out Activity>,
        bundle = parcel.readBundle(Bundle::class.java.classLoader),
        flags = parcel.readInt(),
        presenterType = parcel.readSerializable() as PresenterType,
        presenterClass = Reflection.createKotlinClass(parcel.readSerializable() as Class<out MvpPresenter<*>>) as KClass<out MvpPresenter<*>>) {
    }

    constructor(
        activityClass: Class<out Activity>,
        presenterType: PresenterType,
        presenterClass: KClass<out MvpPresenter<*>>,
        vararg extraParams: BundlePair,
        flags: Int = 0,
    ) : this(activityClass, bundleOf(*extraParams), flags, presenterType, presenterClass)

    override fun getIntent(context: Context): Intent {
        val intent = super.getIntent(context)
        intent.putExtra(MvpDelegate.KEY_UNIQUE_KEY, uniqueKey)
        preparePresenter()
        uniqueKey = generateUniqueKey()
        return intent
    }

    override fun preparePresenter(): Boolean {
        if (MvpPresentersFactory.hasFactory(presenterClass)) {
            val delegateTag = generateDelegateTag(Reflection.createKotlinClass(activityClass), MvpDelegate::class, uniqueKey)

            getOrCreateMvpPresenter(delegateTag, presenterType, presenterClass) {
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

    companion object CREATOR : Creator<MvpActivityLauncher> {

        override fun createFromParcel(parcel: Parcel): MvpActivityLauncher {
            return MvpActivityLauncher(parcel)
        }

        override fun newArray(size: Int): Array<MvpActivityLauncher?> {
            return arrayOfNulls(size)
        }
    }
}