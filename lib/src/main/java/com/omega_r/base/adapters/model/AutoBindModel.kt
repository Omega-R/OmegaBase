package com.omega_r.base.adapters.model

/**
 * Created by Anton Knyazev on 06.04.2019.
 */
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import com.omega_r.base.R
import com.omega_r.base.adapters.OmegaAutoAdapter
import com.omega_r.libs.omegarecyclerview.OmegaRecyclerView
import com.omega_r.libs.omegarecyclerview.header.HeaderFooterWrapperAdapter
import com.omega_r.libs.omegarecyclerview.pagination.WrapperAdapter
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

        inline fun <M> create(block: Builder<M>.() -> Unit): AutoBindModel<M> {
            val builder = Builder<M>()
            block(builder)
            return builder.build()
        }

    }

    constructor(vararg binder: Binder<*, M>) : this(binder.toList())

    fun bind(view: View, item: M) {
        @Suppress("UNCHECKED_CAST")
        var viewCache = view.getTag(R.id.omega_autobind) as? MutableMap<Int, View>
        if (viewCache == null) {
            viewCache = mutableMapOf()
            view.setTag(R.id.omega_autobind, viewCache)
        }
        list.forEach {
            var bindView = viewCache[it.id]
            if (bindView == null) {
                bindView = view.findViewById(it.id)!!
                it.dispatchOnCreateView(bindView)
                viewCache[it.id] = bindView
            }
            it.dispatchBind(bindView, item)
        }
    }


    class Builder<M>() {
        private val list: MutableList<Binder<*, M>> = mutableListOf()

        fun <V : View> bindCustom(@IdRes id: Int, binder: (view: V, item: M) -> Unit) {
            list += CustomBinder(id, binder)
        }

        fun bind(@IdRes id: Int, property: KProperty<Image?>, placeholderRes: Int = 0): Builder<M> {
            return bindImage(id, property, placeholderRes = placeholderRes)
        }

        fun bindImage(@IdRes id: Int, vararg properties: KProperty<*>, placeholderRes: Int = 0): Builder<M> {
            list += ImageBinder(id, *properties, placeholderResId = placeholderRes)
            return this
        }

        fun bind(@IdRes id: Int, property: KProperty<String?>, formatter: ((Any?) -> String?)? = null): Builder<M> {
            return bindString(id, property, formatter = formatter)
        }

        fun bindString(
            @IdRes id: Int, vararg properties: KProperty<*>,
            formatter: ((Any?) -> String?)? = null
        ): Builder<M> {
            list += StringBinder(id, *properties, formatter = formatter)
            return this
        }

        fun bindCharSequence(@IdRes id: Int, property: KProperty<CharSequence?>): Builder<M> {
            return bindCharSequence(id, property)
        }

        fun bindCharSequence(@IdRes id: Int, vararg properties: KProperty<*>): Builder<M> {
            list += CharSequenceBinder(id, *properties)
            return this
        }

        fun bind(@IdRes id: Int, property: KProperty<Text?>): Builder<M> {
            return bindText(id, property)
        }

        fun bindText(@IdRes id: Int, vararg properties: KProperty<*>): Builder<M> {
            list += TextBinder(id, *properties)
            return this
        }

        fun <SM> bindRecycler(@IdRes id: Int,
                              layoutRes: Int,
                              property: KProperty<List<SM>>,
                              callback: OmegaAutoAdapter.Callback<SM>? = null,
                              block: Builder<SM>.() -> Unit): Builder<M> {
            return bindRecycler(id, layoutRes, properties = *arrayOf(property), block = block, callback = callback)
        }

        fun <SM> bindRecycler(@IdRes id: Int,
                              layoutRes: Int,
                              vararg properties: KProperty<*>,
                              callback: OmegaAutoAdapter.Callback<SM>? = null,
                              block: Builder<SM>.() -> Unit): Builder<M> {
            list += RecyclerBinder(id, *properties, layoutRes = layoutRes, block = block, callback = callback)
            return this
        }

        fun bindBinder(binder: Binder<*, M>): Builder<M> {
            list += binder
            return this
        }

        fun build() = AutoBindModel(list)

    }

    abstract class Binder<V : View, M> {

        abstract val id: Int

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

    class ImageBinder<M>(
        override val id: Int,
        private vararg val properties: KProperty<*>,
        private val placeholderResId: Int = 0
    ) : Binder<ImageView, M>() {

        override fun bind(itemView: ImageView, item: M) {
            itemView.setImage(item.findValue(item, properties), placeholderResId)
        }

    }

    class StringBinder<M>(
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

    class CharSequenceBinder<M>(
        override val id: Int,
        private vararg val properties: KProperty<*>
    ) : Binder<TextView, M>() {

        override fun bind(itemView: TextView, item: M) {
            val charSequence: CharSequence? = item.findValue(item, properties)
            itemView.text = charSequence
        }

    }

    class TextBinder<M>(
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

    class RecyclerBinder<M, SM>(
        override val id: Int,
        private vararg val properties: KProperty<*>,
        private val layoutRes: Int,
        private val callback: OmegaAutoAdapter.Callback<SM>? = null,
        private val block: Builder<SM>.() -> Unit
    ) : Binder<RecyclerView, M>() {

        override fun onCreateView(itemView: RecyclerView) {
            itemView.adapter = OmegaAutoAdapter.create(layoutRes, callback, block)
        }

        @Suppress("UNCHECKED_CAST")
        override fun bind(itemView: RecyclerView, item: M) {
            val list: List<SM>? = item.findValue(item, properties)

            getAdapter(itemView).list = list ?: emptyList()
        }

        @Suppress("UNCHECKED_CAST")
        private fun getAdapter(itemView: RecyclerView): OmegaAutoAdapter<SM> {
            val adapter = when (itemView) {
                is OmegaRecyclerView -> itemView.realAdapter
                else -> itemView.adapter
            }
            return adapter as OmegaAutoAdapter<SM>
        }


    }

    class CustomBinder<V : View, M>(override val id: Int, val binder: (view: V, item: M) -> Unit) : Binder<V, M>() {

        override fun bind(itemView: V, item: M) = binder(itemView, item)

    }


}
