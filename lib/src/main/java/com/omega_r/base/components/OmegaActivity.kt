package com.omega_r.base.components

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.omega_r.base.R
import com.omega_r.base.annotations.*
import com.omega_r.base.binders.managers.BindersManager
import com.omega_r.base.clickers.ClickManager
import com.omega_r.base.launchers.ActivityLauncher
import com.omega_r.base.launchers.DialogFragmentLauncher
import com.omega_r.base.launchers.FragmentLauncher
import com.omega_r.base.mvp.OmegaPresenter
import com.omega_r.base.mvp.OmegaView
import com.omega_r.base.mvp.findAnnotation
import com.omega_r.base.mvp.model.Action
import com.omega_r.base.tools.WaitingDialog
import com.omega_r.libs.omegatypes.Text
import com.omegar.mvp.MvpAppCompatActivity

/**
 * Created by Anton Knyazev on 04.04.2019.
 */

const val DELAY_SHOW_WAITING = 555L

abstract class OmegaActivity : MvpAppCompatActivity(), OmegaComponent {

    private val dialogList = mutableListOf<Dialog>()

    override val clickManager = ClickManager()

    override val bindersManager = BindersManager()

    private var waitingDialog: WaitingDialog? = null

    override fun getContext(): Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        this::class.findAnnotation<OmegaWindowFlags>()?.let {
            window?.apply {
                addFlags(it.addFlags)
                clearFlags(it.clearFlags)
            }
        }

        super.onCreate(savedInstanceState)

        this::class.findAnnotation<OmegaTheme>()?.let {
            setTheme(it.resId)
        }
        this::class.findAnnotation<OmegaContentView>()?.let {
            setContentView(it.layoutRes)
        }

        this::class.findAnnotation<OmegaClickViews>()?.let {
            setOnClickListeners(ids = *it.ids, block = this::onClickView)
        }
    }

    override fun getViewForSnackbar(): View {
        return findViewById(android.R.id.content)!!
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        bindersManager.doAutoInit()
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        initToolbar()
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        initToolbar()
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        initToolbar()
    }

    private fun initToolbar() {
        findViewById<Toolbar>(R.id.toolbar)?.let { toolbar ->
            setSupportActionBar(toolbar)

            supportActionBar?.let { supportActionBar ->
                val homeIndicator = this::class.findAnnotation<OmegaHomeIndicator>()
                if (homeIndicator == null) {
                    supportActionBar.setDisplayHomeAsUpEnabled(!intent.hasCategory(Intent.CATEGORY_LAUNCHER) &&
                            Intent.ACTION_MAIN != intent.action && !isTaskRoot)
                } else {
                    val iconRes = homeIndicator.iconRes
                    if (iconRes != -1) {
                        val drawable = ContextCompat.getDrawable(this, iconRes)
                        supportActionBar.setHomeAsUpIndicator(drawable)
                    }
                    supportActionBar.setDisplayHomeAsUpEnabled(homeIndicator.isVisible)
                }
            }

            toolbar.setNavigationOnClickListener {
                onHomePressed()
            }
        }
    }

    protected open fun onHomePressed() {
        onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val annotation = this::class.findAnnotation<OmegaMenu>()
        return if (annotation != null) {
            menuInflater.inflate(annotation.menuRes, menu)
            true
        } else {
            super.onCreateOptionsMenu(menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (clickManager.handleMenuClick(item.itemId)) {
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    override fun setWaiting(waiting: Boolean, text: Text?) {
        if (waiting) {
            if (waitingDialog == null) {
                waitingDialog = WaitingDialog(this)
                text?.let { waitingDialog!!.text = it }
                waitingDialog!!.postShow(DELAY_SHOW_WAITING)
            }
        } else {
            if (waitingDialog != null) {
                waitingDialog!!.dismiss()
                waitingDialog = null
            }
        }
    }

    fun ActivityLauncher.launch(option: Bundle? = null) {
        launch(this@OmegaActivity, option)
    }

    fun ActivityLauncher.launchForResult(requestCode: Int, option: Bundle? = null) {
        launchForResult(this@OmegaActivity, requestCode, option)
    }

    fun ActivityLauncher.DefaultCompanion.launch(option: Bundle? = null) {
        createLauncher()
            .launch(this@OmegaActivity, option)
    }

    fun ActivityLauncher.DefaultCompanion.launchForResult(requestCode: Int, option: Bundle? = null) {
        createLauncher()
            .launchForResult(this@OmegaActivity, requestCode, option)
    }

    fun FragmentLauncher.replaceFragment(@IdRes containerViewId: Int) {
        replace(this@OmegaActivity, containerViewId)
    }

    fun FragmentLauncher.addFragment(@IdRes containerViewId: Int) {
        add(this@OmegaActivity, containerViewId)
    }

    fun FragmentLauncher.DefaultCompanion.replaceFragment(@IdRes containerViewId: Int) {
        createLauncher()
            .replace(this@OmegaActivity, containerViewId)
    }

    fun FragmentLauncher.DefaultCompanion.addFragment(@IdRes containerViewId: Int) {
        createLauncher()
            .add(this@OmegaActivity, containerViewId)
    }

    fun DialogFragmentLauncher.launch(tag: String? = null) {
        launch(supportFragmentManager, tag)
    }

    fun DialogFragmentLauncher.DefaultCompanion.launch(tag: String? = null) {
        launch(supportFragmentManager, tag)
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

    override fun exit() {
        finish()
    }

    override fun onStop() {
        super.onStop()
        waitingDialog?.dismiss()
        dialogList.forEach {
            it.setOnDismissListener(null)
            it.dismiss()
        }
    }

}