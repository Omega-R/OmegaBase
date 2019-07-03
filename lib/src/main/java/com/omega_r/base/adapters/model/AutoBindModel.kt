package com.omega_r.base.adapters.model

/**
 * Created by Anton Knyazev on 06.04.2019.
 */
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.SparseArray
import android.view.View
import android.widget.*
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import com.omega_r.base.R
import com.omega_r.base.adapters.OmegaAutoAdapter
import com.omega_r.base.adapters.OmegaSpinnerAdapter
import com.omega_r.base.clickers.ClickManager
import com.omega_r.libs.omegarecyclerview.OmegaRecyclerView
import com.omega_r.libs.omegatypes.Image
import com.omega_r.libs.omegatypes.Text
import com.omega_r.libs.omegatypes.setImage
import com.omega_r.libs.omegatypes.setText
import kotlin.reflect.KProperty

/**
 * Created by Anton Knyazev on 27.02.2019.
 */

class AutoBindModel<M>(private val list: List<Binder<*, M>>) {

    companion object {

        inline fun <M> create(parentModel: AutoBindModel<M>? = null, block: Builder<M>.() -> Unit): AutoBindModel<M> {
            return Builder(parentModel)
                .apply(block)
                .build()
        }

        inline fun <M> create(block: Builder<M>.() -> Unit): AutoBindModel<M> {
            return create(null, block)
        }

        fun <M> create(model1: AutoBindModel<M>, model2: AutoBindModel<M>): AutoBindModel<M> {
            return AutoBindModel(model1, model2.list)
        }

    }

    constructor(parentModel: AutoBindModel<M>? = null, list: List<Binder<*, M>>) : this(
        list + (parentModel?.list ?: emptyList<Binder<*, M>>())
    )

    constructor(vararg binder: Binder<*, M>) : this(binder.toList())


    fun bind(view: View, item: M) {
        @Suppress("UNCHECKED_CAST")
        var viewCache = view.getTag(R.id.omega_autobind) as? SparseArray<View>
        if (viewCache == null) {
            viewCache = SparseArray()
            view.setTag(R.id.omega_autobind, viewCache)
        }

        view.getTag(R.id.omega_autobind) as? Set<View>

        var optionallySet = view.getTag(R.id.omega_optionally_id) as? MutableSet<Int>

        list.forEach { binder ->
            when (binder) {
                is MultiViewBinder -> {
                    val sparseArray = SparseArray<View>(binder.ids.size)

                    binder.ids.forEach { id ->
                        sparseArray.put(id, findView(viewCache, id, view, binder))
                    }

                    binder.dispatchBind(sparseArray, item)
                }
                else -> {
                    if (optionallySet?.contains(binder.id) != true) {
                        val childView = findView(viewCache, binder.id, view, binder)
                        if (childView == null) {
                            optionallySet = addOptionallyBinder(view, binder.id, optionallySet)
                        } else {
                            binder.dispatchBind(childView, item)
                        }
                    }
                }
            }
        }
    }

    private fun addOptionallyBinder(view: View, viewId: Int, optinallySet: MutableSet<Int>?): MutableSet<Int> {
        var viewOptionally = optinallySet
        if (viewOptionally == null) {
            viewOptionally = HashSet()
            view.setTag(R.id.omega_optionally_id, viewOptionally)
        }

        viewOptionally.add(viewId)
        return viewOptionally
    }

    private fun findView(
        viewCache: SparseArray<View>,
        id: Int,
        view: View,
        binder: Binder<*, M>
    ): View? {
        var bindView = viewCache[id]
        if (bindView == null) {
            bindView = view.findViewById(id) ?: if (binder.viewOptionally) return null else throw IllegalStateException(
                "View with R.id.${view.context.resources.getResourceEntryName(id)} not found"
            )
            list.forEach {
                when (it) {
                    is MultiViewBinder<*, *> ->
                        if (it.ids.contains(id)) {
                            it.dispatchOnCreateView(bindView)
                        }
                    else -> if (it.id == id) it.dispatchOnCreateView(bindView)
                }
            }
            binder.dispatchOnCreateView(bindView)
            viewCache.put(id, bindView)
        }
        return bindView
    }


    class Builder<M>(private val parentModel: AutoBindModel<M>? = null) {

        private val list: MutableList<Binder<*, M>> = mutableListOf()

        fun <V : View> bindCustom(
            @IdRes id: Int,
            binder: (view: V, item: M) -> Unit
        ) = bindBinder(CustomBinder(id, binder))

        fun bind(@IdRes id: Int, property: KProperty<Image?>, placeholderRes: Int = 0): Builder<M> {
            return bindImage(id, property, placeholderRes = placeholderRes)
        }

        fun bindImage(@IdRes id: Int, vararg properties: KProperty<*>, placeholderRes: Int = 0) = apply {
            list += ImageBinder(id, *properties, placeholderResId = placeholderRes)
        }

        fun bind(@IdRes id: Int, property: KProperty<String?>, formatter: ((Any?) -> String?)? = null): Builder<M> {
            return bindString(id, property, formatter = formatter)
        }

        fun bindString(
            @IdRes id: Int,
            vararg properties: KProperty<*>,
            formatter: ((Any?) -> String?)? = null
        ) = bindBinder(StringBinder(id, *properties, formatter = formatter))


        fun bindStringRes(@IdRes id: Int, property: KProperty<Int?>): Builder<M> {
            return bindStringRes(id, *arrayOf(property))
        }

        fun bindStringRes(@IdRes id: Int, vararg properties: KProperty<*>) = apply {
            list += StringResBinder(id, *properties)
        }

        fun bindCharSequence(@IdRes id: Int, property: KProperty<CharSequence?>): Builder<M> {
            return bindCharSequence(id, *arrayOf(property))
        }

        fun bindCharSequence(
            @IdRes id: Int,
            vararg properties: KProperty<*>
        ) = bindBinder(CharSequenceBinder(id, *properties))

        fun bind(@IdRes id: Int, property: KProperty<Text?>): Builder<M> {
            return bindText(id, property)
        }

        fun bindText(@IdRes id: Int, vararg properties: KProperty<*>) = bindBinder(TextBinder(id, *properties))

        fun <SM> bindList(
            @IdRes id: Int,
            layoutRes: Int,
            property: KProperty<List<SM>>,
            callback: ((SM) -> Unit)? = null,
            block: Builder<SM>.() -> Unit
        ): Builder<M> {
            return bindList(id, layoutRes, properties = *arrayOf(property), block = block, callback = callback)
        }

        fun <SM> bindList(
            @IdRes id: Int,
            layoutRes: Int,
            vararg properties: KProperty<*>,
            callback: ((SM) -> Unit)? = null,
            parentModel: AutoBindModel<SM>? = null,

            block: Builder<SM>.() -> Unit
        ): Builder<M> {
            list += RecyclerViewListBinder(
                id,
                *properties,
                layoutRes = layoutRes,
                block = block,
                parentModel = parentModel,
                callback = callback
            )
            return this
        }

        fun <SM> bindList(
            id: Int,
            layoutRes: Int = android.R.layout.simple_spinner_item,
            vararg properties: KProperty<*>,
            nonSelectedItem: SM? = null,
            callback: ((M, SM?, Int) -> Unit)? = null,
            selector: (M) -> SM?,
            converter: (Context, SM) -> CharSequence
        ) = bindBinder(
            SpinnerListBinder(
                id,
                layoutRes,
                *properties,
                nonSelectedItem = nonSelectedItem,
                callback = callback,
                selector = selector,
                converter = converter
            )
        )

        fun bindBinder(binder: Binder<*, M>) = apply {
            list += binder
        }

        fun bindVisible(
            @IdRes id: Int,
            trueVisibility: Int = View.VISIBLE,
            falseVisibility: Int = View.GONE,
            nullVisibility: Int = View.GONE,
            property: KProperty<Boolean?>
        ) = bindVisible(id, trueVisibility, falseVisibility, nullVisibility, *arrayOf(property))

        fun bindAnyVisible(
            @IdRes id: Int,
            trueVisibility: Int = View.VISIBLE,
            falseVisibility: Int = View.GONE,
            nullVisibility: Int = View.GONE,
            property: KProperty<*>
        ) = bindVisible(id, trueVisibility, falseVisibility, nullVisibility, *arrayOf(property))


        fun bindVisible(
            @IdRes id: Int,
            trueVisibility: Int = View.VISIBLE,
            falseVisibility: Int = View.GONE,
            nullVisibility: Int = View.GONE,
            vararg properties: KProperty<*>
        ) = bindBinder(
            VisibleBinder(id, trueVisibility, falseVisibility, nullVisibility, *properties)
        )

        fun bindClick(@IdRes id: Int, block: (M) -> Unit) = bindBinder(ClickBinder(id, block))

        fun bindViewState(
            id: Int,
            viewStateFunction: (View, Boolean) -> Unit,
            selector: (M) -> Boolean
        ) = bindBinder(ViewStateBinder(id, viewStateFunction, selector))

        fun bindChecked(id: Int, callback: ((M, Boolean) -> Unit)?, vararg properties: KProperty<*>) =
            bindBinder(CompoundBinder(id, *properties, block = callback))

        fun bindTextChanged(id: Int, textChangedBlock: ((M, String) -> Unit)) =
            bindBinder(TextChangedBinder(id, textChangedBlock))

        fun optionally() = apply {
            list.last().viewOptionally = true
        }

        fun build() = AutoBindModel(parentModel, list)

    }

    abstract class Binder<V : View, M> {

        abstract val id: Int

        var viewOptionally: Boolean = false

        internal fun dispatchOnCreateView(view: View) {
            @Suppress("UNCHECKED_CAST")
            onCreateView(view as V)
        }

        protected open fun onCreateView(itemView: V) {
            // nothing
        }

        internal fun dispatchBind(view: View, item: M) {
            @Suppress("UNCHECKED_CAST")
            bind(view as V, item)
        }

        abstract fun bind(itemView: V, item: M)

        @Suppress("UNCHECKED_CAST")
        protected fun <T> Any?.findValue(item: Any?, properties: Array<out KProperty<*>>): T? {
            var obj: Any? = item
            for (property in properties) {
                obj = property.call(obj)
                if (obj == null) {
                    break
                }
            }
            return obj?.let { it as T }
        }


    }

    abstract class MultiViewBinder<V : View, M>(override val id: Int, vararg ids: Int) : Binder<V, M>() {

        val ids = listOf(id, *ids.toTypedArray())


        @Suppress("UNCHECKED_CAST")
        fun dispatchBind(views: SparseArray<View>, item: M) {
            bind(views[id] as V, item)
            bind(views as SparseArray<V>, item)
        }

        override fun bind(itemView: V, item: M) {
            // nothing
        }

        abstract fun bind(views: SparseArray<V>, item: M)

    }

    open class ImageBinder<M>(
        override val id: Int,
        private vararg val properties: KProperty<*>,
        private val placeholderResId: Int = 0
    ) : Binder<ImageView, M>() {

        override fun bind(itemView: ImageView, item: M) {
            itemView.setImage(item.findValue(item, properties), placeholderResId)
        }

    }

    open class StringBinder<M>(
        override val id: Int,
        private vararg val properties: KProperty<*>,
        private val formatter: ((Any?) -> String?)? = null
    ) : Binder<TextView, M>() {

        override fun bind(itemView: TextView, item: M) {
            val obj: Any? = item.findValue(item, properties)

            if (formatter == null) {
                itemView.text = obj?.toString()
            } else {
                itemView.text = formatter.invoke(obj)
            }
        }
    }

    open class StringResBinder<M>(
        override val id: Int,
        private vararg val properties: KProperty<*>
    ) : Binder<TextView, M>() {

        override fun bind(itemView: TextView, item: M) {
            val obj: Int? = item.findValue(item, properties)

            if (obj != null) itemView.setText(obj) else itemView.text = null
        }
    }

    open class CharSequenceBinder<M>(
        override val id: Int,
        private vararg val properties: KProperty<*>
    ) : Binder<TextView, M>() {

        override fun bind(itemView: TextView, item: M) {
            val charSequence: CharSequence? = item.findValue(item, properties)
            itemView.text = charSequence
        }

    }

    open class TextBinder<M>(
        override val id: Int,
        private vararg val properties: KProperty<*>
    ) : Binder<TextView, M>() {

        override fun bind(itemView: TextView, item: M) {
            val text: Text? = item.findValue(item, properties)
            if (text != null) {
                itemView.setText(text)
            } else {
                itemView.text = null
            }
        }
    }

    open class VisibleBinder<M>(
        override val id: Int,
        private val trueVisibility: Int = View.VISIBLE,
        private val falseVisibility: Int = View.GONE,
        private val nullVisibility: Int = View.GONE,
        private vararg val properties: KProperty<*>
    ) : Binder<View, M>() {

        override fun bind(itemView: View, item: M) {
            val obj: Any? = item.findValue(item, properties)
            itemView.visibility = when (obj) {
                true -> trueVisibility
                false -> falseVisibility
                null -> nullVisibility
                else -> trueVisibility
            }
        }
    }

    open class ClickBinder<M>(
        override val id: Int,
        private val block: (M) -> Unit
    ) : Binder<View, M>() {

        override fun onCreateView(itemView: View) {
            val tag = itemView.getTag(R.id.omega_click_bind) as? ClickManager
            if (tag == null) {
                itemView.setTag(R.id.omega_click_bind, ClickManager())
            }
        }

        override fun bind(itemView: View, item: M) {
            val clickManager = itemView.getTag(R.id.omega_click_bind) as ClickManager
            itemView.setOnClickListener(clickManager.wrap(id, fun() { block(item) }))
        }
    }

    open class RecyclerViewListBinder<M, SM>(
        override val id: Int,
        private vararg val properties: KProperty<*>,
        private val layoutRes: Int,
        private val callback: ((SM) -> Unit)? = null,
        private val parentModel: AutoBindModel<SM>? = null,
        private val block: Builder<SM>.() -> Unit
    ) : Binder<RecyclerView, M>() {

        override fun onCreateView(itemView: RecyclerView) {
            itemView.adapter = OmegaAutoAdapter.create(layoutRes, callback, parentModel, block)
        }

        @Suppress("UNCHECKED_CAST")
        override fun bind(itemView: RecyclerView, item: M) {
            val list: List<SM>? = item.findValue(item, properties)

            getAdapter(itemView).list = list ?: emptyList()
        }

        @Suppress("UNCHECKED_CAST")
        private fun getAdapter(itemView: RecyclerView): OmegaAutoAdapter<SM, *> {
            val adapter = when (itemView) {
                is OmegaRecyclerView -> itemView.realAdapter
                else -> itemView.adapter
            }
            return adapter as OmegaAutoAdapter<SM, *>
        }
    }

    open class CustomBinder<V : View, M>(override val id: Int, val binder: (view: V, item: M) -> Unit) :
        Binder<V, M>() {

        override fun bind(itemView: V, item: M) = binder(itemView, item)

    }

    open class ViewStateBinder<E>(
        override val id: Int,
        private val viewStateFunction: (View, Boolean) -> Unit,
        private val selector: (E) -> Boolean

    ) : AutoBindModel.Binder<View, E>() {

        override fun bind(itemView: View, item: E) {
            viewStateFunction(itemView, selector(item))
        }

    }

    open class CompoundBinder<E>(
        override val id: Int,
        private vararg val properties: KProperty<*>,
        private val block: ((E, Boolean) -> Unit)? = null
    ) : AutoBindModel.Binder<CompoundButton, E>() {

        override fun bind(itemView: CompoundButton, item: E) {
            val checked: Boolean? = item.findValue(item, properties)
            block?.let { itemView.setOnCheckedChangeListener(null) }

            itemView.isChecked = checked ?: false
            block?.let {
                itemView.setOnCheckedChangeListener { _, checked: Boolean ->
                    it(item, checked)
                }
            }

        }
    }

    open class TextChangedBinder<E>(override val id: Int, private val block: (E, String) -> Unit) :
        AutoBindModel.Binder<TextView, E>() {

        @Suppress("UNCHECKED_CAST")
        private fun getTextWatcher(view: View): BinderTextWatcher<E> {
            return view.getTag(R.id.omega_text_watcher) as? BinderTextWatcher<E> ?: let {
                BinderTextWatcher<E>().also {
                    view.setTag(R.id.omega_text_watcher, it)
                }
            }
        }

        override fun onCreateView(itemView: TextView) {
            getTextWatcher(itemView).callbacks.add(block)
        }

        override fun bind(itemView: TextView, item: E) {
            getTextWatcher(itemView).item = item
        }

        private class BinderTextWatcher<E> : TextWatcher {
            var item: E? = null
            val callbacks: MutableList<(E, String) -> Unit> = mutableListOf()

            override fun afterTextChanged(s: Editable?) {
                // nothing
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                item?.let { item ->
                    callbacks.forEach { it(item, s.toString()) }
                }
            }

        }

    }

    open class SpinnerListBinder<M, SM>(
        override val id: Int,
        private val layoutRes: Int,
        private vararg val properties: KProperty<*>,
        private val nonSelectedItem: SM? = null,
        private val callback: ((M, SM?, Int) -> Unit)? = null,
        private val selector: (M) -> SM?,
        private val converter: (Context, SM) -> CharSequence
    ) : Binder<Spinner, M>() {

        override fun onCreateView(itemView: Spinner) {
            itemView.adapter = OmegaSpinnerAdapter.Custom(itemView.context, layoutRes, converter).also {
                it.nonSelectedItem = nonSelectedItem
            }
        }

        @Suppress("UNCHECKED_CAST", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun bind(spinner: Spinner, item: M) {
            val list: List<SM>? = item.findValue(item, properties)

            val adapter = spinner.adapter as OmegaSpinnerAdapter.Custom<SM>

            adapter.list = list ?: emptyList()

            if (callback != null) {
                spinner.onItemSelectedListener = null
            }

            adapter.setSelection(spinner, selector(item))

            if (callback != null) {

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // nothing
                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        callback.invoke(item, adapter.getSelection(spinner), adapter.getSelectionPosition(spinner))
                    }

                }
            }
        }

    }

}