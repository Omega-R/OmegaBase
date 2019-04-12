package com.omega_r.base.adapters.model

/**
 * Created by Anton Knyazev on 06.04.2019.
 */
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omega_r.base.R
import com.omega_r.base.adapters.OmegaAutoAdapter
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

        fun <V : View> bindCustom(id: Int, binder: (view: V, item: M) -> Unit) {
            list += CustomBinder(id, binder)
        }

        fun bindImage(
            id: Int,
            vararg properties: KProperty<*>,
            placeholderRes: Int = 0
        ): Builder<M> {
            list += ImageBinder(id, *properties, placeholderResId = placeholderRes)
            return this
        }

        fun bindString(
            id: Int,
            vararg properties: KProperty<*>,
            formatter: ((Any?) -> String?)? = null
        ): Builder<M> {
            list += StringBinder(id, *properties, formatter = formatter)
            return this
        }

        fun bindText(
            id: Int,
            vararg properties: KProperty<*>
        ): Builder<M> {
            list += TextBinder(id, *properties)
            return this
        }

        fun <M2> bindRecycler(id: Int,
                              vararg properties: KProperty<*>,
                              layoutRes: Int,
                              block: AutoBindModel.Builder<M2>.() -> Unit): Builder<M> {
            list += RecyclerBinder(id, *properties, layoutRes = layoutRes, block = block)
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

    }

    class ImageBinder<M>(
        override val id: Int,
        private vararg val properties: KProperty<*>,
        private val placeholderResId: Int = 0
    ) : Binder<ImageView, M>() {

        override fun bind(itemView: ImageView, item: M) {
            var obj: Any? = item

            for (property in properties) {
                obj = property.call(obj)
                if (obj == null) {
                    break
                }
            }

            val image: Image? = obj?.let { obj as Image }

            itemView.setImage(image, placeholderResId)
        }

    }

    class StringBinder<M>(
        override val id: Int,
        private vararg val properties: KProperty<*>,
        private val formatter: ((Any?) -> String?)? = null
    ) : Binder<TextView, M>() {

        override fun bind(itemView: TextView, item: M) {
            var obj: Any? = item
            for (property in properties) {
                obj = property.call(obj)
                if (obj == null) {
                    break
                }
            }

            if (formatter == null) {
                itemView.text = obj?.toString()
            } else {
                itemView.text = formatter.invoke(obj)
            }
        }

    }

    class TextBinder<M>(
        override val id: Int,
        private vararg val properties: KProperty<*>
    ) : Binder<TextView, M>() {

        override fun bind(itemView: TextView, item: M) {

            var obj: Any? = item
            for (property in properties) {
                obj = property.call(obj)
                if (obj == null) {
                    break
                }
            }

            if (obj != null) {
                itemView.setText(obj as Text)
            } else {
                itemView.text = null
            }
        }

    }

    class RecyclerBinder<M, M2>(
        override val id: Int,
        private vararg val properties: KProperty<*>,
        private val layoutRes: Int,
        private val block: AutoBindModel.Builder<M2>.() -> Unit
    ) : Binder<RecyclerView, M>() {

        override fun onCreateView(itemView: RecyclerView) {
            itemView.adapter = OmegaAutoAdapter.create(layoutRes, block)
        }

        @Suppress("UNCHECKED_CAST")
        override fun bind(itemView: RecyclerView, item: M) {
            var obj: Any? = item
            for (property in properties) {
                obj = property.call(obj)
                if (obj == null) {
                    break
                }
            }
            if (obj != null) {
                val list = obj as List<M2>
                (itemView.adapter as OmegaAutoAdapter<M2>).list = list
            } else {
                (itemView.adapter as OmegaAutoAdapter<M2>).list = emptyList()
            }
        }

    }

    class CustomBinder<V : View, M>(override val id: Int, val binder: (view: V, item: M) -> Unit) : Binder<V, M>() {
        override fun bind(itemView: V, item: M) = binder(itemView, item)
    }

}
