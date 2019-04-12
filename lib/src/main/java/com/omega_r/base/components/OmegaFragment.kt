package com.omega_r.base.components

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.annotation.IdRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.omega_r.base.annotations.OmegaClickViews
import com.omega_r.base.annotations.OmegaContentView
import com.omega_r.base.annotations.OmegaMenu
import com.omega_r.base.annotations.OmegaTheme
import com.omega_r.base.binders.OmegaBindable
import com.omega_r.base.binders.managers.ResettableBindersManager
import com.omega_r.base.clickers.ClickManager
import com.omega_r.base.clickers.OmegaClickable
import com.omega_r.base.launchers.ActivityLauncher
import com.omega_r.base.launchers.FragmentLauncher
import com.omega_r.libs.omegatypes.Text
import com.omegar.mvp.MvpAppCompatFragment

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
open class OmegaFragment : MvpAppCompatFragment(), OmegaBindable, OmegaClickable, OmegaView {

    override val clickManager = ClickManager()

    override val bindersManager = ResettableBindersManager()

    override fun <T : View> findViewById(id: Int) = view?.findViewById<T>(id)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(this::class.findAnnotation<OmegaMenu>() != null)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val annotation = this::class.findAnnotation<OmegaMenu>()
        if (annotation != null) {
            inflater.inflate(annotation.menuRes, menu)
        } else {
            super.onCreateOptionsMenu(menu, inflater)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (clickManager.handleMenuClick(item.itemId)) {
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = this::class.findAnnotation<OmegaContentView>()
        val view = if (contentView != null) {
            var themedInflater = inflater
            val theme = this::class.findAnnotation<OmegaTheme>()
            theme?.let {
                val contextThemeWrapper = ContextThemeWrapper(activity, theme.resId)
                themedInflater = inflater.cloneInContext(contextThemeWrapper)
            }

            themedInflater.inflate(contentView.layoutRes, container, false)
        } else {
            super.onCreateView(inflater, container, savedInstanceState)
        }

        this::class.findAnnotation<OmegaClickViews>()?.let {
            setOnClickListeners(ids = *it.ids, block = this::onClickView)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindersManager.reset()
        bindersManager.doAutoInit()
    }

    override fun showMessage(message: Text) {
        MaterialAlertDialogBuilder(context)
            .setMessage(message.getString(resources))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    protected open fun getViewForSnackbar() = view!!

    override fun showBottomMessage(message: Text, action: Text?, actionListener: (() -> Unit)?) {
        Snackbar.make(getViewForSnackbar(), message.getString(resources)!!, Snackbar.LENGTH_LONG).apply {
            if (action != null) {
                setAction(action.getString(resources)!!) {
                    actionListener?.invoke()
                }
            }
        }.show()
    }

    override fun showToast(message: Text) {
        Toast.makeText(context, message.getString(resources), Toast.LENGTH_LONG).show()
    }

    override fun setWaiting(waiting: Boolean, text: Text?) {
        (activity as OmegaActivity).setWaiting(waiting, text)
    }

    fun ActivityLauncher.launch(option: Bundle? = null) {
        launch(context!!, option)
    }

    fun ActivityLauncher.launchForResult(requestCode: Int, option: Bundle? = null) {
        launchForResult(this@OmegaFragment, requestCode, option)
    }

    fun ActivityLauncher.DefaultCompanion.launch(option: Bundle? = null) {
        createLauncher()
            .launch(context!!, option)
    }

    fun ActivityLauncher.DefaultCompanion.launchForResult(requestCode: Int, option: Bundle? = null) {
        createLauncher()
            .launchForResult(this@OmegaFragment, requestCode, option)
    }

    fun FragmentLauncher.replaceFragment(@IdRes containerViewId: Int) {
        replace(this@OmegaFragment, containerViewId)
    }

    fun FragmentLauncher.addFragment(@IdRes containerViewId: Int) {
        add(this@OmegaFragment, containerViewId)
    }

    protected open fun onClickView(view: View) {
        // nothing
    }

}