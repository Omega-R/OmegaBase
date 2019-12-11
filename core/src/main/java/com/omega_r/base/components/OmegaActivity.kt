package com.omega_r.base.components

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.omega_r.base.R
import com.omega_r.base.annotations.*
import com.omega_r.base.binders.IdHolder
import com.omega_r.base.binders.managers.BindersManager
import com.omega_r.base.clickers.ClickManager
import com.omega_r.base.mvp.model.Action
import com.omega_r.base.mvp.views.findAnnotation
import com.omega_r.base.tools.DialogManager
import com.omega_r.libs.extensions.context.getColorByAttribute
import com.omega_r.libs.omegatypes.Text
import com.omegar.libs.omegalaunchers.ActivityLauncher
import com.omegar.libs.omegalaunchers.BaseIntentLauncher
import com.omegar.libs.omegalaunchers.DialogFragmentLauncher
import com.omegar.libs.omegalaunchers.FragmentLauncher
import com.omegar.mvp.MvpAppCompatActivity

/**
 * Created by Anton Knyazev on 04.04.2019.
 */


abstract class OmegaActivity : MvpAppCompatActivity(), OmegaComponent {

    override val clickManager = ClickManager()

    override val bindersManager = BindersManager()

    protected val dialogManager by lazy { DialogManager(this) }

    override fun getContext(): Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        this::class.findAnnotation<com.omega_r.base.annotations.OmegaWindowFlags>()?.let {
            window?.apply {
                addFlags(it.addFlags)
                clearFlags(it.clearFlags)
            }
        }

        super.onCreate(savedInstanceState)

        this::class.annotations.forEach {
            when (it) {
                is OmegaTheme -> {
                    setTheme(it.resId)
                }
                is OmegaContentView -> {
                    setContentView(it.layoutRes)
                }
                is OmegaClickViews -> {
                    setOnClickListeners(ids = *it.ids, block = this::onClickView)
                }
                is OmegaTitle -> {
                    setTitle(it.resId)
                }
                is OmegaWindowBackground -> {
                    if (it.drawableRes > 0) {
                        window.setBackgroundDrawable(
                            ContextCompat.getDrawable(this, it.drawableRes)
                        )
                    } else if (it.colorAttrRes > 0) {
                        window.setBackgroundDrawable(
                            ColorDrawable(getColorByAttribute(it.colorAttrRes))
                        )
                    }

                }
            }
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
                    supportActionBar.setDisplayHomeAsUpEnabled(
                        !intent.hasCategory(Intent.CATEGORY_LAUNCHER) &&
                                Intent.ACTION_MAIN != intent.action && !isTaskRoot
                    )
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
        dialogManager.setWaiting(waiting, text)
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

    fun ActivityLauncher.DefaultCompanion.launchForResult(
        requestCode: Int,
        option: Bundle? = null
    ) {
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

    override fun launch(launcher: DialogFragmentLauncher) {
        launcher.launch(supportFragmentManager)
    }

    protected open fun onClickView(view: View) {
        // nothing
    }

    override fun showQuery(
        message: Text,
        title: Text?,
        positiveAction: Action,
        negativeAction: Action,
        neutralAction: Action?
    ) {
        createQuery(message, title, positiveAction, negativeAction, neutralAction).apply {
            dialogManager.addDialog(this)
            show()
        }
    }

    override fun hideQueryOrMessage() {
        dialogManager.hideLastDialog()
    }

    override fun showMessage(message: Text, action: Action?) {
        createMessage(message, action).apply {
            dialogManager.addDialog(this)
            show()
        }
    }

    override fun launchForResult(launcher: BaseIntentLauncher, requestCode: Int) {
        launcher.launchForResult(this, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!onLaunchResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun requestPermissions(requestCode: Int, vararg permissions: String) {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (!presenter.onPermissionResult(requestCode, permissions, grantResults)) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        when (fragment) {
            is OmegaFragment -> presenter.attachChildPresenter(fragment.presenter)
            is OmegaDialogFragment -> presenter.attachChildPresenter(fragment.presenter)
            is OmegaBottomSheetDialogFragment -> presenter.attachChildPresenter(fragment.presenter)
        }
    }


    override fun onStart() {
        super.onStart()
        dialogManager.onStart()
    }

    override fun onStop() {
        super.onStop()
        dialogManager.onStop()
    }

    override fun exit() {
        finish()
    }

    protected fun <T: View> bindAndSetClick(@IdRes res: Int, block: () -> Unit): Lazy<T>  {
        return bind(res) {
            setOnClickListener(this, block)
        }
    }

    final override fun <T> bind(init: () -> T) = super.bind(init)

    final override fun <T : View, E> bind(vararg idsPair: Pair<E, Int>) = super.bind<T, E>(*idsPair)

    final override fun <T : View, IH : IdHolder> bind(ids: Array<out IH>): Lazy<Map<IH, T>> =
        super.bind(ids)

    final override fun <T : View> bind(@IdRes res: Int): Lazy<T> = super.bind(res)

    final override fun <T : View> bind(@IdRes vararg ids: Int): Lazy<List<T>> = super.bind(*ids)

    final override fun <T : RecyclerView> bind(@IdRes res: Int, adapter: RecyclerView.Adapter<*>) =
        super.bind<T>(res, adapter)

    final override fun <T : View, E> bind(vararg idsPair: Pair<E, Int>, initBlock: T.(E) -> Unit) =
        super.bind(idsPair = *idsPair, initBlock = initBlock)

    final override fun <T : View, IH : IdHolder> bind(
        ids: Array<out IH>,
        initBlock: T.(IdHolder) -> Unit
    ) = super.bind(ids, initBlock)

    final override fun <T : View> bind(@IdRes res: Int, initBlock: T.() -> Unit) =
        super.bind(res, initBlock)

    final override fun <T : View> bind(@IdRes vararg ids: Int, initBlock: T.() -> Unit) =
        super.bind(ids = *ids, initBlock = initBlock)

    final override fun <T : RecyclerView> bind(
        res: Int,
        adapter: RecyclerView.Adapter<*>,
        initBlock: T.() -> Unit
    ) =
        super.bind(res, adapter, initBlock)

    final override fun bindAnimation(@AnimRes res: Int) = super.bindAnimation(res)

    final override fun bindColor(@ColorRes res: Int) = super.bindColor(res)

    final override fun bindDimen(@DimenRes res: Int) = super.bindDimen(res)

    final override fun bindDimenPixel(@DimenRes res: Int) = super.bindDimenPixel(res)

    final override fun bindDrawable(@DrawableRes res: Int) = super.bindDrawable(res)

    final override fun bindInt(@IntegerRes res: Int) = super.bindInt(res)

    final override fun <T : View> bindOrNull(@IdRes res: Int) = super.bindOrNull<T>(res)

    final override fun <T : View> bindOrNull(@IdRes res: Int, initBlock: T.() -> Unit) =
        super.bindOrNull(res, initBlock)


}