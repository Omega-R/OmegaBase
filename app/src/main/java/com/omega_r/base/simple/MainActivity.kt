package com.omega_r.base.simple

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.omega_r.adapters.OmegaListAdapter
import com.omega_r.base.components.OmegaActivity
import com.omega_r.bind.adapters.OmegaAutoAdapter
import com.omega_r.bind.delegates.IdHolder
import com.omega_r.bind.model.binders.bindImage
import com.omega_r.libs.omegatypes.Text
import com.omega_r.libs.omegatypes.image.Image
import com.omega_r.libs.omegatypes.image.from

class MainActivity : OmegaActivity(R.layout.activity_main), MainView {

    override val presenter: MainPresenter by providePresenter()

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

    private val recyclerView: RecyclerView by bind(R.id.recyclerview, adapter) {
        recyclerView.setHasFixedSize(true)
    }

    private val maps: Map<Field, View> by bind(Field.values()) {
        //showToast(Text.from(it.id.toString()))
    }

    override var list: String = ""

    override var enabled: Boolean
        get() = false
        set(value) {
            println("YES")
        }

    private val menuItem: MenuItem? by bindMenuItem(R.id.action_test)

    private var imageMenuItem: Image by bindIconMenuItem(R.id.action_test)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        title = this[EXTRA_TITLE]
        setClickListener(R.id.button) {
            showToast(Text.from("Test"))
        }
        setMenu(R.menu.menu_main, R.id.action_test to { showToast(Text.from("Test")) })
        imageMenuItem = Image.from(R.drawable.ic_error_omega)
        val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        val metaData = applicationInfo.metaData
        val email = metaData?.getString("CRASH_SENDER_EMAIL")
        Log.v("Test123", email.toString())
    }

    private fun onClickItem(item: Image) {
        showToast(Text.from("Click $item"))
        throw NullPointerException()
//        ActivityLauncher.launch(this, null, createLauncher("1"), createLauncher("2"))

//        createLauncher("1").launch(this, createLauncher("2"))
    }

    data class Item(
        val text: String = "123",
        val list: List<SubItem> = listOf(
            SubItem(),
            SubItem(),
            SubItem(),
            SubItem(),
            SubItem(),
            SubItem(),
            SubItem(),
            SubItem(),
            SubItem(),
            SubItem()
        )
    )

    data class SubItem(val text: String = "123")

    enum class Field(override val id: Int) : IdHolder {
        ITEM1(R.id.recyclerview),
        ITEM2(R.id.recyclerview),
    }


}
