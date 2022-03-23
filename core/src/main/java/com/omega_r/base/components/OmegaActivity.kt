package com.omega_r.base.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.omega_r.base.R
import com.omega_r.base.annotations.*
import com.omega_r.base.annotations.OmegaWindowBackground.Companion.apply
import com.omega_r.base.dialogs.DialogCategory
import com.omega_r.base.mvp.model.Action
import com.omega_r.base.mvp.views.findAnnotation
import com.omega_r.base.dialogs.DialogManager
import com.omega_r.base.dialogs.WaitingController
import com.omega_r.base.mvp.presenters.OmegaPresenter
import com.omega_r.bind.delegates.IdHolder
import com.omega_r.bind.delegates.managers.BindersManager
import com.omega_r.bind.model.BindModel
import com.omega_r.click.ClickManager
import com.omega_r.libs.extensions.common.ifNull
import com.omega_r.libs.omegatypes.Text
import com.omegar.libs.omegalaunchers.ActivityLauncher
import com.omegar.libs.omegalaunchers.BaseIntentLauncher
import com.omegar.libs.omegalaunchers.DialogFragmentLauncher
import com.omegar.libs.omegalaunchers.FragmentLauncher
import com.omegar.mvp.MvpAppCompatActivity
import java.io.Serializable

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
private const val INNER_KEY_MENU = "menu"

abstract class OmegaActivity : MvpAppCompatActivity, OmegaComponent {

    override val clickManager = ClickManager()

    override val bindersManager = BindersManager()

    protected open val dialogManager =  DialogManager()

    protected open val waitingController by lazy { WaitingController(this, dialogManager) }

    private val innerData: MutableMap<String, Any> = hashMapOf()

    constructor() : super()

    @ContentView
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    override fun getContext(): Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        OmegaPresenter.isDebuggable.ifNull {
            OmegaPresenter.isDebuggable = 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        }

        this::class.findAnnotation<OmegaWindowFlags>()?.let {
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
                    setClickListeners(ids = *it.ids, block = this::onClickView)
                }
                is OmegaTitle -> {
                    setTitle(it.resId)
                }
                is OmegaWindowBackground -> {
                    it.apply(window)
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
        clickManager.viewFindable = this
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        waitingController.saveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        waitingController.onRestoreInstanceState(savedInstanceState)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        onViewCreated()
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        onViewCreated()
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        onViewCreated()
    }

    @CallSuper
    protected open fun onViewCreated() {
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

    protected fun setMenu(@MenuRes menuRes: Int, vararg pairs: Pair<Int, () -> Unit>) {
        innerData[INNER_KEY_MENU] = menuRes
        setMenuListener(pairs = * pairs)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuRes = innerData[INNER_KEY_MENU] as? Int ?: this::class.findAnnotation<OmegaMenu>()?.menuRes
        return menuRes?.let {
            innerData.remove(INNER_KEY_MENU)
            menuInflater.inflate(menuRes, menu)
            true
        } ?: super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return clickManager.handleMenuClick(item.itemId) || super.onOptionsItemSelected(item)
    }

    override fun setWaiting(waiting: Boolean, text: Text?) {
        waitingController.setWaiting(waiting, text)
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
        createQuery(message, title, positiveAction, negativeAction, neutralAction)
            .apply(dialogManager::showMessageDialog)
    }

    override fun hideQueryOrMessage() {
        dialogManager.dismissLastDialog(DialogCategory.MESSAGE)
    }

    override fun showMessage(message: Text, title: Text?, action: Action?) {
        createMessage(message, title, action)
            .apply(dialogManager::showMessageDialog)
    }

    override fun launchForResult(launcher: BaseIntentLauncher, requestCode: Int) {
        launcher.launchForResult(this, requestCode)
    }

    override fun launchForResult(launcher: DialogFragmentLauncher, requestCode: Int) {
        launcher.launch(supportFragmentManager, requestCode = requestCode)
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

    override fun onStart() {
        super.onStart()
        dialogManager.onStart()
    }

    override fun onStop() {
        super.onStop()
        dialogManager.onStop()
    }

    override fun setResult(success: Boolean, data: Serializable?) {
        val resultCode = if (success) Activity.RESULT_OK else Activity.RESULT_CANCELED
        if (data != null) {
            setResult(resultCode, Intent().putExtra(KEY_RESULT, data))
        } else {
            setResult(resultCode)
        }
    }

    override fun exit() {
        finish()
    }

    @Suppress("UNCHECKED_CAST")
    protected operator fun <T> get(extraKey: String): T? {
        return intent.extras?.get(extraKey) as T?
    }

    final override fun <T : View> bindAndSetClick(@IdRes res: Int, block: () -> Unit): Lazy<T> {
        return super.bindAndSetClick<T>(res, block)
    }

    final override fun <T> bind(init: () -> T) = super.bind(init)

    final override fun <T : View, E> bind(vararg idsPair: Pair<E, Int>) = super.bind<T, E>(*idsPair)

    final override fun <T : View, IH : IdHolder> bind(ids: Array<out IH>): Lazy<Map<IH, T>> =
        super.bind(ids)

    final override fun <T : View> bind(@IdRes res: Int): Lazy<T> = super.bind(res)

    final override fun <T : View> bind(@IdRes vararg ids: Int): Lazy<List<T>> = super.bind(*ids)

    final override fun <T : RecyclerView> bind(@IdRes res: Int, adapter: RecyclerView.Adapter<*>) =
        super.bind<T>(res, adapter)

    final override fun <T : RecyclerView, M> bind(
        res: Int,
        layoutRes: Int,
        parentModel: BindModel<M>?,
        callback: ((M) -> Unit)?,
        builder: BindModel.Builder<M>.() -> Unit
    ): Lazy<T> {
        return super.bind(res, layoutRes, parentModel, callback, builder)
    }

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

    @JvmName("setClickFunction")
    fun <T : View> T.setClickListener(block: () -> Unit) {
        setClickListener(this, block)
    }
}