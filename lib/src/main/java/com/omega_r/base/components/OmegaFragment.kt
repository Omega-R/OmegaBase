package com.omega_r.base.components

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.annotation.*
import androidx.recyclerview.widget.RecyclerView
import com.omega_r.base.annotations.OmegaClickViews
import com.omega_r.base.annotations.OmegaContentView
import com.omega_r.base.annotations.OmegaMenu
import com.omega_r.base.annotations.OmegaTheme
import com.omega_r.base.binders.IdHolder
import com.omega_r.base.binders.managers.ResettableBindersManager
import com.omega_r.base.clickers.ClickManager
import com.omega_r.base.mvp.views.findAnnotation
import com.omega_r.base.mvp.model.Action
import com.omega_r.libs.omegatypes.Text
import com.omegar.libs.omegalaunchers.ActivityLauncher
import com.omegar.libs.omegalaunchers.BaseIntentLauncher
import com.omegar.libs.omegalaunchers.DialogFragmentLauncher
import com.omegar.libs.omegalaunchers.FragmentLauncher
import com.omegar.mvp.MvpAppCompatFragment

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
abstract class OmegaFragment : MvpAppCompatFragment(), OmegaComponent {

    private val dialogList = mutableListOf<Dialog>()

    override val clickManager = ClickManager()

    override val bindersManager = ResettableBindersManager()

    open fun getTitle(): Text? = null

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

    override fun getViewForSnackbar() = view!!

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

    fun DialogFragmentLauncher.launch(tag: String? = null, requestCode: Int? = null) {
        launch(childFragmentManager, tag, this@OmegaFragment, requestCode)
    }

    fun DialogFragmentLauncher.DefaultCompanion.launch(tag: String? = null, requestCode: Int? = null) {
        launch(childFragmentManager, tag, this@OmegaFragment, requestCode)
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
        activity!!.finish()
    }

    override fun setResult(resultCode: Int) {
        activity?.setResult(resultCode)
    }

    override fun setResult(resultCode: Int, intent: Intent) {
        activity?.setResult(resultCode, intent)
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

    final override fun <T : View> bind(@IdRes vararg ids: Int, initBlock: T.() -> Unit)=
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