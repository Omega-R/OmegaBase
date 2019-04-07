package com.omega_r.base.simple

import com.omega_r.base.adapters.OmegaAutoAdapter
import com.omega_r.base.annotations.OmegaContentView
import com.omega_r.base.components.OmegaActivity
import com.omega_r.libs.omegarecyclerview.OmegaRecyclerView
import com.omega_r.libs.omegatypes.Text

@OmegaContentView(R.layout.activity_main)
class MainActivity : OmegaActivity(), OmegaAutoAdapter.Callback<MainActivity.Item> {

    private val adapter = OmegaAutoAdapter.create<Item>(R.layout.item_test) {
        bind(R.id.textview_test, Item::text)
    }.apply {
        callback = this@MainActivity
    }

    private val recyclerView: OmegaRecyclerView by bind(R.id.recyclerview) {
        this@MainActivity.adapter.list = listOf(Item(), Item())
        adapter = this@MainActivity.adapter
    }

    override fun onClickItem(item: Item, position: Int) {
        showToast(Text.from("Click $position"))
    }

    data class Item (val text: String = "123")

}
