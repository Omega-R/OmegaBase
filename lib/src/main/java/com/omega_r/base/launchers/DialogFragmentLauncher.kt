package com.omega_r.base.launchers

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.omega_r.base.tools.BundlePair
import com.omega_r.base.tools.bundleOf
import com.omega_r.base.tools.equalsBundle
import com.omega_r.base.tools.hashCodeBundle
import kotlinx.android.parcel.Parcelize
import java.lang.IllegalArgumentException

/**
 * Created by Anton Knyazev on 30.04.19.
 */
@Parcelize
class DialogFragmentLauncher(private val fragmentClass: Class<DialogFragment>, private val bundle: Bundle? = null) :
    Launcher, Parcelable {

    constructor(fragmentClass: Class<DialogFragment>, vararg extraParams: BundlePair)
            : this(fragmentClass, bundleOf(*extraParams))

    private fun createDialogFragment(): DialogFragment {
        val fragment = fragmentClass.newInstance()
        fragment.arguments = bundle
        return fragment
    }

    override fun launch(context: Context) {
        if (context is AppCompatActivity) {
            launch(context.supportFragmentManager)
        } else {
            throw IllegalArgumentException("Launch support only context as AppCompatActivity")
        }
    }

    fun isOurDialogFragment(fragment: DialogFragment): Boolean {
        return fragmentClass.isInstance(fragment) && fragment.arguments.equalsBundle(bundle)
    }

    fun launch(transaction: FragmentTransaction, tag: String? = null, targetFragment: Fragment? = null, requestCode: Int? = null) {
        createDialogFragment().apply {
            if (targetFragment != null || requestCode != null) {
                setTargetFragment(targetFragment, requestCode ?: 0)
            }
            show(transaction, tag)
        }
    }

    fun launch(fragmentManager: FragmentManager, tag: String? = null, targetFragment: Fragment? = null, requestCode: Int? = null) {
        createDialogFragment().apply {
            if (targetFragment != null || requestCode != null) {
                setTargetFragment(targetFragment, requestCode ?: 0)
            }
            show(fragmentManager, tag)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DialogFragmentLauncher

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

        fun createLauncher(): DialogFragmentLauncher

        fun launch(transaction: FragmentTransaction, tag: String? = null, targetFragment: Fragment? = null, requestCode: Int? = null) {
            return createLauncher()
                .launch(transaction, tag, targetFragment, requestCode)
        }

        fun launch(manager: FragmentManager, tag: String? = null, targetFragment: Fragment? = null, requestCode: Int? = null) {
            return createLauncher().launch(manager, tag, targetFragment, requestCode)
        }

    }

}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> T.createDialogFragmentLauncher(vararg extra: BundlePair): DialogFragmentLauncher {
    val declaringClass = T::class.java.declaringClass
    return DialogFragmentLauncher(declaringClass as Class<DialogFragment>, *extra)
}


@Suppress("UNCHECKED_CAST")
inline fun <reified T> T.createDialogFragmentLauncher(): DialogFragmentLauncher {
    val declaringClass = T::class.java.declaringClass
    return DialogFragmentLauncher(declaringClass as Class<DialogFragment>)
}