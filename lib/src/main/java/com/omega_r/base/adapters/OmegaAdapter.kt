package com.omega_r.base.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.omega_r.base.clickers.ClickManager
import com.omega_r.base.clickers.OmegaClickable
import com.omega_r.libs.omegarecyclerview.BaseListAdapter
import com.omega_r.libs.omegarecyclerview.OmegaRecyclerView

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
abstract class OmegaAdapter<VH : RecyclerView.ViewHolder>: OmegaRecyclerView.Adapter<VH>() {

    open class ViewHolder: OmegaRecyclerView.ViewHolder, OmegaClickable {

        override val clickManager = ClickManager()

        constructor(parent: ViewGroup?, res: Int) : super(parent, res)

        constructor(parent: ViewGroup?, layoutInflater: LayoutInflater?, res: Int) : super(parent, layoutInflater, res)

        constructor(itemView: View?) : super(itemView)

    }

}