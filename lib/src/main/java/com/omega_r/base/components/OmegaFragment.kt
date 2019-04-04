package com.omega_r.base.components

import android.os.Bundle
import android.view.*
import com.omega_r.base.annotations.OmegaContentView
import com.omega_r.base.annotations.OmegaMenu
import com.omega_r.base.binders.OmegaBindable
import com.omega_r.base.binders.managers.ResettableBindersManager
import com.omega_r.base.clickers.ClickManager
import com.omega_r.base.clickers.OmegaClickable
import com.omegar.mvp.MvpAppCompatFragment
import kotlin.reflect.full.findAnnotation

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
open class OmegaFragment: MvpAppCompatFragment(), OmegaBindable, OmegaClickable {

    override val clickManager = ClickManager()

    override val bindersManager = ResettableBindersManager()

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = this::class.findAnnotation<OmegaContentView>()
        return if (contentView !=  null) {
            inflater.inflate(contentView.layoutRes, container, false)
        } else {
            super.onCreateView(inflater, container, savedInstanceState)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindersManager.reset()
    }

}