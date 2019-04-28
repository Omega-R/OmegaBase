package com.omega_r.base.adapters

/**
 * Created by Anton Knyazev on 28.04.2019.
 */
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.omega_r.base.components.OmegaFragment
import com.omega_r.base.launchers.FragmentLauncher

/**
 * Created by Anton Knyazev on 27.04.2019.
 */
class ViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    var list: List<FragmentLauncher> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private lateinit var container: ViewGroup

    constructor(fm: FragmentManager, vararg fragmentLauncher: FragmentLauncher) : this(fm) {
        list = fragmentLauncher.toList()
    }

    override fun startUpdate(container: ViewGroup) {
        super.startUpdate(container)
        if (!this::container.isInitialized) {
            this.container = container
            // WORKAROUND: If container not initialized then getPageTitle return null.
            // This is force update pageTitle
            container.post {
                notifyDataSetChanged()
            }
        }
    }

    override fun getItem(position: Int) = list[position].createFragment()

    override fun getCount(): Int = list.size

    override fun getPageTitle(position: Int): CharSequence? {
        if (::container.isInitialized) {
            return (getCurrentFragment(position) as? OmegaFragment)?.getTitle()?.getCharSequence(container.context)
        }
        return null
    }

    fun getCurrentFragment(position: Int) = instantiateItem(container, position) as Fragment

}