package com.omega_r.base.simple

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.omega_r.base.adapters.OmegaAutoAdapter
import com.omega_r.base.annotations.OmegaContentView
import com.omega_r.base.binders.IdHolder
import com.omega_r.base.components.OmegaActivity
import com.omega_r.libs.omegatypes.Text

@OmegaContentView(R.layout.activity_main)
class MainActivity : OmegaActivity(), OmegaAutoAdapter.Callback<MainActivity.Item> {

    private val adapter = OmegaAutoAdapter.create(R.layout.item_test, this) {
        bind(R.id.textview_test, Item::text)
        bindRecycler(R.id.recyclerview, R.layout.item_test, Item::list) {
            bind(R.id.textview_test, SubItem::text)
        }
    }.apply {
        list = listOf(Item(), Item())
    }

    private val recyclerView: RecyclerView by bind(R.id.recyclerview, adapter)

    private val maps: Map<Field, View> by bind(Field.values()) {
        showToast(Text.from(it.id.toString()))
    }

    override fun onClickItem(item: Item, position: Int) {
        showToast(Text.from("Click $position"))
    }

    data class Item (val text: String = "123", val list: List<SubItem> = listOf(SubItem(), SubItem(), SubItem(), SubItem(), SubItem(), SubItem(), SubItem(), SubItem(), SubItem(), SubItem()))

    data class SubItem (val text: String = "123")

    enum class Field(override val id: Int) : IdHolder {
        ITEM1(R.id.recyclerview),
        ITEM2(R.id.recyclerview),
    }

}
