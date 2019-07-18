package com.omega_r.base.simple

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.omega_r.base.adapters.OmegaAutoAdapter
import com.omega_r.base.adapters.OmegaListAdapter
import com.omega_r.base.adapters.OmegaViewPagerAdapter
import com.omega_r.base.annotations.OmegaContentView
import com.omega_r.base.binders.IdHolder
import com.omega_r.base.components.OmegaActivity
import com.omega_r.libs.omegatypes.Image
import com.omega_r.libs.omegatypes.Text
import com.omega_r.libs.omegatypes.from
import com.omegar.mvp.presenter.InjectPresenter

@OmegaContentView(R.layout.activity_main)
class MainActivity : OmegaActivity(), MainView {

    @InjectPresenter
    override lateinit var presenter: MainPresenter

    private val adapter = OmegaAutoAdapter.create(R.layout.item_test_3, ::onClickItem) {
        bindImage(R.id.imageview)
    }.apply {
        watcher = OmegaListAdapter.ImagePreloadWatcher(this)
        list = listOf(
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?156"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?256"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?35"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?45"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?56"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?64"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?79"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?85"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?91"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?102"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?113"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?124"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?135"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?146"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?1578"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?169"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?174"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?185"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?1956"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?201"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?212"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?2212"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?231"),
            Image.from("https://i.pinimg.com/originals/d6/68/ab/d668abc72809303852c27275e6a56775.gif?242")
        )
    }

    private val recyclerView: RecyclerView by bind(R.id.recyclerview, adapter)

    private val maps: Map<Field, View> by bind(Field.values()) {
        showToast(Text.from(it.id.toString()))
    }

    private fun onClickItem(item: Image) {
        showToast(Text.from("Click $item"))
    }

    data class Item (val text: String = "123", val list: List<SubItem> = listOf(SubItem(), SubItem(), SubItem(), SubItem(), SubItem(), SubItem(), SubItem(), SubItem(), SubItem(), SubItem()))

    data class SubItem (val text: String = "123")

    enum class Field(override val id: Int) : IdHolder {
        ITEM1(R.id.recyclerview),
        ITEM2(R.id.recyclerview),
    }

}
