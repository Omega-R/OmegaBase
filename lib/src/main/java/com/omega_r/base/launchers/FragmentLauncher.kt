package com.omega_r.base.launchers

import android.app.Activity
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.omega_r.base.tools.BundlePair
import com.omega_r.base.tools.bundleOf
import java.io.Serializable

/**
 * Created by Anton Knyazev on 06.04.2019.
 */
data class FragmentLauncher(private val fragmentClass: Class<Fragment>, private val bundle: Bundle? = null): Launcher, Serializable {

    constructor(fragmentClass: Class<Fragment>, vararg extraParams: BundlePair, flags: Int = 0)
            : this(fragmentClass, bundleOf(*extraParams))

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