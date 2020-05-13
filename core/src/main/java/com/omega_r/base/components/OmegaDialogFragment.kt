package com.omega_r.base.components

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.annotation.*
import androidx.recyclerview.widget.RecyclerView
import com.omega_r.base.annotations.*
import com.omega_r.base.annotations.OmegaWindowBackground.Companion.apply
import com.omega_r.base.binders.IdHolder
import com.omega_r.base.binders.managers.ResettableBindersManager
import com.omega_r.base.clickers.ClickManager
import com.omega_r.base.mvp.model.Action
import com.omega_r.base.mvp.views.findAnnotation
import com.omega_r.libs.omegatypes.Text
import com.omegar.libs.omegalaunchers.ActivityLauncher
import com.omegar.libs.omegalaunchers.BaseIntentLauncher
import com.omegar.libs.omegalaunchers.DialogFragmentLauncher
import com.omegar.libs.omegalaunchers.FragmentLauncher
import com.omegar.mvp.MvpAppCompatDialogFragment
import java.io.Serializable

/**
 * Created by Anton Knyazev on 04.04.2019.
 */

private const val KEY_SAVE_RESULT = "omegaSaveResult"
private const val KEY_SAVE_DATA = "omegaSaveData"
private const val KEY_SAVE_REQUEST_CODE = "omegaRequestCode"

abstract class OmegaDialogFragment : MvpAppCompatDialogFragment(), OmegaComponent {

    private val dialogList = mutableListOf<Dialog>()

    override val clickManager = ClickManager()

    override val bindersManager = ResettableBindersManager()

    private var childPresenterAttached = false

    private var result: Boolean = false
    private var data: Serializable? = null
    private var requestCode: Int = 0

    override fun <T : View> findViewById(id: Int): T? = view?.findViewById(id)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        result = savedInstanceState?.getBoolean(KEY_SAVE_RESULT, result) ?: result
        data = savedInstanceState?.getSerializable(KEY_SAVE_DATA) ?: data
        requestCode = savedInstanceState?.getInt(KEY_SAVE_REQUEST_CODE, targetRequestCode) ?: targetRequestCode

        setHasOptionsMenu(this::class.findAnnotation<OmegaMenu>() != null)
        this::class.findAnnotation<OmegaClickViews>()?.let {
            setOnClickListeners(ids = *it.ids, block = this::onClickView)
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
            childPresenterAttached = false
            (activity as? OmegaActivity)?.presenter?.detachChildPresenter(presenter)
        }
    }

    override fun onStart() {
        super.onStart()
        attachChildPresenter()
    }

    override fun onResume() {
        super.onResume()
        attachChildPresenter()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        detachChildPresenter()
        outState.putBoolean(KEY_SAVE_RESULT, result)
        outState.putSerializable(KEY_SAVE_RESULT, data)
        outState.putInt(KEY_SAVE_REQUEST_CODE, requestCode)
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

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        this::class.findAnnotation<OmegaWindowBackground>()?.apply(dialog.window!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = this::class.findAnnotation<OmegaContentView>()

        val themedInflater = this::class.findAnnotation<OmegaTheme>()?.let {
            inflater.cloneInContext(ContextThemeWrapper(activity, it.resId))
        } ?: inflater

        return if (contentView != null) {
            themedInflater.inflate(contentView.layoutRes, container, false)
        } else {
            super.onCreateView(themedInflater, container, savedInstanceState)
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

    override fun launch(launcher: DialogFragmentLauncher) {
        launcher.launch(childFragmentManager)
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

    override fun launchForResult(launcher: BaseIntentLauncher, requestCode: Int) {
        launcher.launchForResult(this, requestCode)
    }

    override fun launchForResult(launcher: DialogFragmentLauncher, requestCode: Int) {
        launcher.launch(childFragmentManager, requestCode = requestCode)
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

    override fun onStop() {
        super.onStop()
        detachChildPresenter()
        dialogList.forEach {
            it.setOnDismissListener(null)
            it.dismiss()
        }
    }

    override fun exit() {
        dismiss()
    }

    override fun setResult(success: Boolean, data: Serializable?) {
        result = success
        this.data = data
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        result = false
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (isResumed && requestCode != 0) {
            val omegaComponent = (parentFragment as? OmegaComponent) ?: (activity as? OmegaComponent)
            omegaComponent?.presenter?.onLaunchResult(requestCode, result, data)
        }
    }

    final override fun <T> bind(init: () -> T) = super.bind(init)

    final override fun <T : View, E> bind(vararg idsPair: Pair<E, Int>) = super.bind<T, E>(*idsPair)

    final override fun <T : View, IH : IdHolder> bind(ids: Array<out IH>): Lazy<Map<IH, T>> = super.bind(ids)

    final override fun <T : View> bind(@IdRes res: Int): Lazy<T> = super.bind(res)

    final override fun <T : View> bind(@IdRes vararg ids: Int): Lazy<List<T>> = super.bind(*ids)

    final override fun <T : RecyclerView> bind(@IdRes res: Int, adapter: RecyclerView.Adapter<*>) = super.bind<T>(res, adapter)

    final override fun <T : View, E> bind(vararg idsPair: Pair<E, Int>, initBlock: T.(E) -> Unit) =
        super.bind(idsPair = *idsPair, initBlock = initBlock)

    final override fun <T : View, IH : IdHolder> bind(
        ids: Array<out IH>,
        initBlock: T.(IdHolder) -> Unit
    ) = super.bind(ids, initBlock)

    final override fun <T : View> bind(@IdRes res: Int, initBlock: T.() -> Unit) = super.bind(res, initBlock)

    final override fun <T : View> bind(@IdRes vararg ids: Int, initBlock: T.() -> Unit) =
        super.bind(ids = *ids, initBlock = initBlock)

    final override fun <T : RecyclerView> bind(res: Int, adapter: RecyclerView.Adapter<*>, initBlock: T.() -> Unit) =
        super.bind(res, adapter, initBlock)

    final override fun bindAnimation(@AnimRes res: Int) = super.bindAnimation(res)

    final override fun bindColor(@ColorRes res: Int) = super.bindColor(res)

    final override fun bindDimen(@DimenRes res: Int) = super.bindDimen(res)

    final override fun bindDimenPixel(@DimenRes res: Int) = super.bindDimenPixel(res)

    final override fun bindDrawable(@DrawableRes res: Int) = super.bindDrawable(res)

    final override fun bindInt(@IntegerRes res: Int) = super.bindInt(res)

    final override fun <T : View> bindOrNull(@IdRes res: Int) = super.bindOrNull<T>(res)

    final override fun <T : View> bindOrNull(@IdRes res: Int, initBlock: T.() -> Unit) = super.bindOrNull(res, initBlock)

}