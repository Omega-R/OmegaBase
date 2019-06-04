package com.omega_r.base.components

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.annotation.IdRes
import com.omega_r.base.annotations.OmegaClickViews
import com.omega_r.base.annotations.OmegaContentView
import com.omega_r.base.annotations.OmegaMenu
import com.omega_r.base.annotations.OmegaTheme
import com.omega_r.base.binders.managers.ResettableBindersManager
import com.omega_r.base.clickers.ClickManager
import com.omega_r.base.launchers.ActivityLauncher
import com.omega_r.base.launchers.DialogFragmentLauncher
import com.omega_r.base.launchers.FragmentLauncher
import com.omega_r.base.mvp.findAnnotation
import com.omega_r.base.mvp.model.Action
import com.omega_r.libs.omegatypes.Text
import com.omegar.mvp.MvpAppCompatDialogFragment


/**
 * Created by Anton Knyazev on 04.04.2019.
 */
abstract class OmegaDialogFragment : MvpAppCompatDialogFragment(), OmegaComponent {

    private val dialogList = mutableListOf<Dialog>()

    override val clickManager = ClickManager()

    override val bindersManager = ResettableBindersManager()

    override fun <T : View> findViewById(id: Int): T? = view?.findViewById(id)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(this::class.findAnnotation<OmegaMenu>() != null)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val annotation = this::class.findAnnotation<OmegaMenu>()
        if (annotation != null) {
            inflater.inflate(annotation.menuRes, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
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

    override fun getViewForSnackbar() = view!!

    override fun setWaiting(waiting: Boolean, text: Text?) {
        (activity as OmegaActivity).setWaiting(waiting, text)
    }

    fun ActivityLauncher.launch(option: Bundle? = null) {
        launch(context!!, option)
    }

    fun ActivityLauncher.launchForResult(requestCode: Int, option: Bundle? = null) {
        launchForResult(this@OmegaDialogFragment, requestCode, option)
    }

    fun ActivityLauncher.DefaultCompanion.launch(option: Bundle? = null) {
        createLauncher()
            .launch(context!!, option)
    }

    fun ActivityLauncher.DefaultCompanion.launchForResult(requestCode: Int, option: Bundle? = null) {
        createLauncher()
            .launchForResult(this@OmegaDialogFragment, requestCode, option)
    }

    fun FragmentLauncher.replaceFragment(@IdRes containerViewId: Int) {
        replace(this@OmegaDialogFragment, containerViewId)
    }

    fun FragmentLauncher.addFragment(@IdRes containerViewId: Int) {
        add(this@OmegaDialogFragment, containerViewId)
    }

    fun DialogFragmentLauncher.launch(tag: String? = null, requestCode: Int? = null) {
        launch(childFragmentManager, tag, this@OmegaDialogFragment, requestCode)
    }

    fun DialogFragmentLauncher.DefaultCompanion.launch(tag: String? = null, requestCode: Int? = null) {
        launch(childFragmentManager, tag, this@OmegaDialogFragment, requestCode)
    }

    protected open fun onClickView(view: View) {
        // nothing
    }

    override fun showQuery(message: Text, title: Text?, positiveAction: Action, negativeAction: Action, neutralAction: Action?) {
        createQuery(message, title, positiveAction, negativeAction, neutralAction).apply {
            dialogList += this
            show()
        }
    }

    override fun hideQueryOrMessage() {
        dialogList.lastOrNull()?.let {
            it.dismiss()
            dialogList.remove(it)
        }
    }

    override fun showMessage(message: Text, action: Action?) {
        createMessage(message, action).apply {
            dialogList += this
            show()
        }
    }

    override fun launchForResult(launcher: ActivityLauncher, requestCode: Int) {
        launcher.launchForResult(this, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!onLaunchResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onStop() {
        super.onStop()
        dialogList.forEach {
            it.setOnDismissListener(null)
            it.dismiss()
        }
    }
    
    override fun exit() {
        dismiss()
    }

}