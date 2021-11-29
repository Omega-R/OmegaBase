package com.omega_r.base.components

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.annotation.*
import androidx.recyclerview.widget.RecyclerView
import com.omega_r.base.annotations.OmegaClickViews
import com.omega_r.base.annotations.OmegaContentView
import com.omega_r.base.annotations.OmegaMenu
import com.omega_r.base.annotations.OmegaTheme
import com.omega_r.base.dialogs.DialogCategory
import com.omega_r.base.dialogs.DialogManager
import com.omega_r.base.mvp.model.Action
import com.omega_r.base.mvp.views.findAnnotation
import com.omega_r.bind.delegates.IdHolder
import com.omega_r.bind.delegates.managers.ResettableBindersManager
import com.omega_r.bind.model.BindModel
import com.omega_r.click.ClickManager
import com.omega_r.libs.omegatypes.Text
import com.omegar.libs.omegalaunchers.*
import com.omegar.mvp.MvpAppCompatFragment
import java.io.Serializable

/**
 * Created by Anton Knyazev on 04.04.2019.
 */

private const val INNER_KEY_MENU = "menu"

abstract class OmegaFragment : MvpAppCompatFragment, OmegaComponent {

    protected open val dialogManager = DialogManager()

    override val clickManager = ClickManager()

    override val bindersManager = ResettableBindersManager()

    private var childPresenterAttached = false

    private val innerData: MutableMap<String, Any> = hashMapOf()

    constructor() : super()

    @ContentView
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    open fun getTitle(): Text? = null

    override fun <T : View> findViewById(id: Int) = view?.findViewById<T>(id)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(this::class.findAnnotation<OmegaMenu>() != null)

        this::class.findAnnotation<OmegaClickViews>()?.let {
            setClickListeners(ids = *it.ids, block = this::onClickView)
        }
    }

    private fun attachChildPresenter() {
        if (!childPresenterAttached) {
            childPresenterAttached = true
            (activity as? OmegaActivity)?.presenter?.attachChildPresenter(presenter)
        }
    }

    private fun detachChildPresenter() {
        if (childPresenterAttached) {
            (activity as? OmegaActivity)?.presenter?.detachChildPresenter(presenter)
        }
    }

    override fun onStart() {
        super.onStart()
        attachChildPresenter()
        dialogManager.onStart()
    }

    override fun onStop() {
        super.onStop()
        detachChildPresenter()
        dialogManager.onStop()
    }

    override fun onResume() {
        super.onResume()
        attachChildPresenter()
        dialogManager.onStart()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        detachChildPresenter()
        dialogManager.onStop()
    }

    protected fun setMenu(@MenuRes menuRes: Int, vararg pairs: Pair<Int, () -> Unit>) {
        setHasOptionsMenu(true)
        innerData[INNER_KEY_MENU] = menuRes
        setMenuListener(pairs = * pairs)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val menuRes = innerData[INNER_KEY_MENU] as? Int ?: this::class.findAnnotation<OmegaMenu>()?.menuRes
        menuRes?.let {
            inflater.inflate(menuRes, menu)
            true
        } ?: super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (clickManager.handleMenuClick(item.itemId)) true else super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = this::class.findAnnotation<OmegaContentView>()

        return if (contentView != null) {
            val themedInflater = this::class.findAnnotation<OmegaTheme>()?.let {
                val contextThemeWrapper = ContextThemeWrapper(activity, it.resId)
                inflater.cloneInContext(contextThemeWrapper)
            } ?: inflater

            themedInflater.inflate(contentView.layoutRes, container, false)
        } else {
            super.onCreateView(inflater, container, savedInstanceState)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindersManager.reset()
        bindersManager.doAutoInit()
        clickManager.viewFindable = this
    }

    override fun onDestroyView() {
        super.onDestroyView()
        detachChildPresenter()
        clickManager.viewFindable = null
        dialogManager.onStop()
    }

    override fun getViewForSnackbar() = requireView()

    override fun setWaiting(waiting: Boolean, text: Text?) {
        (activity as OmegaActivity).setWaiting(waiting, text)
    }

    fun ActivityLauncher.launch(option: Bundle? = null) {
        launch(requireContext(), option)
    }

    fun ActivityLauncher.launchForResult(requestCode: Int, option: Bundle? = null) {
        launchForResult(this@OmegaFragment, requestCode, option)
    }

    fun ActivityLauncher.DefaultCompanion.launch(option: Bundle? = null) {
        createLauncher()
            .launch(requireContext(), option)
    }

    fun ActivityLauncher.DefaultCompanion.launchForResult(
        requestCode: Int,
        option: Bundle? = null
    ) {
        createLauncher()
            .launchForResult(this@OmegaFragment, requestCode, option)
    }

    fun FragmentLauncher.replaceFragment(@IdRes containerViewId: Int) {
        replace(this@OmegaFragment, containerViewId)
    }

    fun FragmentLauncher.addFragment(@IdRes containerViewId: Int) {
        add(this@OmegaFragment, containerViewId)
    }

    fun DialogFragmentLauncher.launch(tag: String? = null, requestCode: Int? = null) {
        launch(childFragmentManager, tag, this@OmegaFragment, requestCode)
    }

    fun DialogFragmentLauncher.DefaultCompanion.launch(
        tag: String? = null,
        requestCode: Int? = null
    ) {
        launch(childFragmentManager, tag, this@OmegaFragment, requestCode)
    }

    override fun launch(launcher: DialogFragmentLauncher) {
        launcher.launch(childFragmentManager)
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

    override fun launch(launcher: Launcher) {
        when (launcher) {
            is FragmentLauncher -> {
                launcher.replaceFragment(R.id.layout_container)
            }
            else -> super.launch(launcher)
        }
    }

    override fun launchForResult(launcher: BaseIntentLauncher, requestCode: Int) {
        launcher.launchForResult(this, requestCode)
    }

    override fun launchForResult(launcher: DialogFragmentLauncher, requestCode: Int) {
        launcher.launch(childFragmentManager, requestCode = requestCode)
    }

    override fun setResult(success: Boolean, data: Serializable?) {
        TODO("Not yet implemented")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!onLaunchResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun requestPermissions(requestCode: Int, vararg permissions: String) {
        requestPermissions(permissions, requestCode)
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

    override fun exit() {
        requireActivity().finish()
    }

    @Suppress("UNCHECKED_CAST")
    protected operator fun <T> get(extraKey: String): T? {
        return arguments?.get(extraKey) as T?
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
    final fun <T : View> T.setClickListener(block: () -> Unit) {
        setClickListener(this, block)
    }
}