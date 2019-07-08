package com.omega_r.base.launchers

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.omega_r.base.tools.BundlePair
import com.omega_r.base.tools.bundleOf
import com.omega_r.base.tools.equalsBundle
import com.omega_r.base.tools.hashCodeBundle
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anton Knyazev on 06.04.2019.
 */
@Parcelize
class FragmentLauncher(private val fragmentClass: Class<Fragment>,
                       private val bundle: Bundle? = null): Launcher, Parcelable {

    constructor(fragmentClass: Class<Fragment>, vararg extraParams: BundlePair)
            : this(fragmentClass, bundleOf(*extraParams))

    override fun launch(context: Context) {
        throw IllegalAccessException("No supported")
    }

    fun createFragment(): Fragment {
        val fragment = fragmentClass.newInstance()
        fragment.arguments = bundle
        return fragment
    }

    fun replace(fragmentManager: FragmentManager, @IdRes containerViewId: Int) {
        val fragment = createFragment()
        fragmentManager.beginTransaction()
            .replace(containerViewId, fragment)
            .commitAllowingStateLoss()
    }

    fun add(fragmentManager: FragmentManager, @IdRes containerViewId: Int) {
        val fragment = createFragment()
        fragmentManager.beginTransaction()
            .add(containerViewId, fragment)
            .commitAllowingStateLoss()
    }

    fun replace(activity: AppCompatActivity, @IdRes containerViewId: Int) {
        replace(activity.supportFragmentManager, containerViewId)
    }

    fun replace(fragment: Fragment, @IdRes containerViewId: Int) {
        replace(fragment.childFragmentManager, containerViewId)
    }

    fun add(activity: AppCompatActivity, @IdRes containerViewId: Int) {
        add(activity.supportFragmentManager, containerViewId)
    }

    fun add(fragment: Fragment, @IdRes containerViewId: Int) {
        add(fragment.childFragmentManager, containerViewId)
    }

    fun isOurFragment(fragment: Fragment): Boolean {
        return fragmentClass.isInstance(fragment) && fragment.arguments.equalsBundle(bundle)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FragmentLauncher

        if (fragmentClass != other.fragmentClass) return false
        if (!bundle.equalsBundle(other.bundle)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fragmentClass.hashCode()
        result = 31 * result + (bundle?.hashCodeBundle() ?: 0)
        return result
    }

    interface DefaultCompanion {

        fun createLauncher(): FragmentLauncher

        fun createFragment(): Fragment {
            return createLauncher()
                .createFragment()
        }

    }


}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> T.createFragmentLauncher(vararg extra: BundlePair): FragmentLauncher {
    val declaringClass = T::class.java.declaringClass
    return FragmentLauncher(declaringClass as Class<Fragment>, *extra)
}


@Suppress("UNCHECKED_CAST")
inline fun <reified T> T.createFragmentLauncher(): FragmentLauncher {
    val declaringClass = T::class.java.declaringClass
    return FragmentLauncher(declaringClass as Class<Fragment>)
}