package com.omega_r.base.components

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.omega_r.base.annotations.OmegaClickViews
import com.omega_r.base.annotations.OmegaContentView
import com.omega_r.base.binders.IdHolder
import com.omega_r.base.binders.OmegaBindable
import com.omega_r.base.binders.managers.BindersManager
import com.omega_r.base.clickers.ClickManager
import com.omega_r.base.clickers.OmegaClickable
import com.omega_r.base.mvp.views.findAnnotation

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
open class OmegaDialog : Dialog, OmegaBindable, OmegaClickable {

    override val clickManager = ClickManager()

    override val bindersManager = BindersManager()

    constructor(context: Context) : super(context)

    constructor(context: Context, themeResId: Int) : super(context, themeResId)

    constructor(
        context: Context,
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(context, cancelable, cancelListener)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentView = this::class.findAnnotation<OmegaContentView>()
        if (contentView != null) {
            setContentView(contentView.layoutRes)
        }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        bindersManager.doAutoInit()
        setClickListenerFromAnnotation()
    }

    override fun setContentView(view: View) {
        super.setContentView(view)
        bindersManager.doAutoInit()
        setClickListenerFromAnnotation()
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        bindersManager.doAutoInit()
        setClickListenerFromAnnotation()
    }

    private fun setClickListenerFromAnnotation() {
        clickManager.viewFindable = this

        this::class.findAnnotation<OmegaClickViews>()?.let {
            setOnClickListeners(ids = *it.ids, block = this::onClickView)
        }
    }

    protected open fun onClickView(view: View) {
        // nothing
    }

    final override fun <T> bind(init: () -> T) = super.bind(init)

    final override fun <T : View, E> bind(vararg idsPair: Pair<E, Int>) = super.bind<T, E>(*idsPair)

    final override fun <T : View, IH : IdHolder> bind(ids: Array<out IH>): Lazy<Map<IH, T>> = super.bind(ids)

    final override fun <T : View> bind(res: Int): Lazy<T> = super.bind(res)

    final override fun <T : View> bind(vararg ids: Int): Lazy<List<T>> = super.bind(*ids)

    final override fun <T : RecyclerView> bind(res: Int, adapter: RecyclerView.Adapter<*>) = super.bind<T>(res, adapter)

    final override fun <T : View, E> bind(vararg idsPair: Pair<E, Int>, initBlock: T.(E) -> Unit) =
        super.bind(idsPair = *idsPair, initBlock = initBlock)

    final override fun <T : View, IH : IdHolder> bind(
        ids: Array<out IH>,
        initBlock: T.(IdHolder) -> Unit
    ) = super.bind(ids, initBlock)

    final override fun <T : View> bind(res: Int, initBlock: T.() -> Unit) = super.bind(res, initBlock)

    final override fun <T : View> bind(vararg ids: Int, initBlock: T.() -> Unit)=
        super.bind(ids = *ids, initBlock = initBlock)

    final override fun <T : RecyclerView> bind(res: Int, adapter: RecyclerView.Adapter<*>, initBlock: T.() -> Unit) =
        super.bind(res, adapter, initBlock)

    final override fun bindAnimation(res: Int) = super.bindAnimation(res)

    final override fun bindColor(res: Int) = super.bindColor(res)

    final override fun bindDimen(res: Int) = super.bindDimen(res)

    final override fun bindDimenPixel(res: Int) = super.bindDimenPixel(res)

    final override fun bindDrawable(res: Int) = super.bindDrawable(res)

    final override fun bindInt(res: Int) = super.bindInt(res)

    final override fun <T : View> bindOrNull(res: Int) = super.bindOrNull<T>(res)

    final override fun <T : View> bindOrNull(res: Int, initBlock: T.() -> Unit) = super.bindOrNull(res, initBlock)

}