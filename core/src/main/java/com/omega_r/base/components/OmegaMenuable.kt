package com.omega_r.base.components

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.IdRes
import com.omega_r.click.OmegaContextable
import com.omega_r.libs.omegatypes.Text
import com.omega_r.libs.omegatypes.image.Image
import kotlin.reflect.KProperty

interface OmegaMenuable: OmegaContextable {

    val menuItemPropertyList: MutableList<MenuProperty>

    fun bindMenuItem(@IdRes id: Int, initBlock: ((MenuItem) -> Unit)? = null) = ItemMenuProperty(id, initBlock).also {
        menuItemPropertyList += it
    }

    fun bindVisibleMenuItem(@IdRes id: Int) = VisibleMenuProperty(id).also {
        menuItemPropertyList += it
    }

    fun bindTitleMenuItem(@IdRes id: Int) = TitleStringMenuProperty(id).also {
        menuItemPropertyList += it
    }

    fun bindTextMenuItem(@IdRes id: Int) = TitleTextMenuProperty(getContext()!!, id).also {
        menuItemPropertyList += it
    }

    fun bindIconMenuItem(@IdRes id: Int) = IconImageMenuProperty(getContext()!!, id).also {
        menuItemPropertyList += it
    }

    fun onPrepareMenu(menu: Menu) {
        menuItemPropertyList.forEach {
            it.menuItem = menu.findItem(it.id)
        }
    }

    interface MenuProperty {

        var menuItem: MenuItem?
        val id: Int
    }

    open class ItemMenuProperty(@IdRes override val id: Int, private val initBlock: ((MenuItem) -> Unit)?) : MenuProperty {

        override var menuItem: MenuItem? = null
            set(value) {
                field = value
                initBlock?.let { field?.let(initBlock) }
            }

        operator fun getValue(thisRef: Any, property: KProperty<*>): MenuItem? = menuItem
    }

    open class VisibleMenuProperty(@IdRes override val id: Int) : MenuProperty {

        private var visible: Boolean = false
            set(value) {
                field = value
                menuItem?.isVisible = value
            }

        override var menuItem: MenuItem? = null
            set(value) {
                field = value
                value?.isVisible = visible
            }

        operator fun getValue(thisRef: Any, property: KProperty<*>): Boolean = visible

        operator fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
            visible = value
        }
    }

    open class TitleStringMenuProperty(@IdRes override val id: Int) : MenuProperty {

        private var title: String = ""
            set(value) {
                field = value
                menuItem?.title = value
            }

        override var menuItem: MenuItem? = null
            set(value) {
                field = value
                value?.title = title
            }

        operator fun getValue(thisRef: Any, property: KProperty<*>): String = title

        operator fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
            title = value
        }
    }

    open class TitleTextMenuProperty(private val context: Context, @IdRes override val id: Int) : MenuProperty {

        private var title: Text = Text.empty()
            set(value) {
                field = value
                menuItem?.title = value.getCharSequence(context)
            }

        override var menuItem: MenuItem? = null
            set(value) {
                field = value
                value?.title = title.getCharSequence(context)
            }

        operator fun getValue(thisRef: Any, property: KProperty<*>): Text = title

        operator fun setValue(thisRef: Any, property: KProperty<*>, value: Text) {
            title = value
        }
    }


    open class IconImageMenuProperty(private val context: Context, @IdRes override val id: Int) : MenuProperty {

        private var image: Image = Image.from()
            set(value) {
                field = value
                menuItem?.icon = value.getDrawable(context)
            }

        override var menuItem: MenuItem? = null
            set(value) {
                field = value
                value?.icon = image.getDrawable(context)
            }

        operator fun getValue(thisRef: Any, property: KProperty<*>): Image = image

        operator fun setValue(thisRef: Any, property: KProperty<*>, value: Image) {
            image = value
        }
    }

}