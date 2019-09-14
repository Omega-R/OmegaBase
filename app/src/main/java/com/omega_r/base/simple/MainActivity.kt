package com.omega_r.base.simple

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.omega_r.base.adapters.OmegaAutoAdapter
import com.omega_r.base.adapters.OmegaListAdapter
import com.omega_r.base.annotations.OmegaContentView
import com.omega_r.base.binders.IdHolder
import com.omega_r.base.components.OmegaActivity
import com.omega_r.libs.omegatypes.Image
import com.omega_r.libs.omegatypes.Text
import com.omega_r.libs.omegatypes.from
import com.omegar.libs.omegalaunchers.createActivityLauncher
import com.omegar.libs.omegalaunchers.tools.put
import com.omegar.mvp.presenter.InjectPresenter

@OmegaContentView(R.layout.activity_main)
class MainActivity : OmegaActivity(), MainView {

    companion object {

        private const val EXTRA_TITLE = "title"

        fun createLauncher(title: String) = createActivityLauncher(
            EXTRA_TITLE put title
        )

    }

    @InjectPresenter
    override lateinit var presenter: MainPresenter

    private val images = listOf(
        Image.from("https://images.wallpaperscraft.ru/image/gora_vershina_pik_146078_3840x2400.jpg"),
        Image.from("https://hubblesite.org/uploads/image_file/image_attachment/31803/STSCI-H-p1935b-m-2000x1827.png"),
        Image.from("https://hubblesite.org/uploads/image_file/image_attachment/31726/STSCI-H-p1918a-f-2000x2000.png"),
        Image.from("https://images.wallpaperscraft.ru/image/basketbolnoe_koltso_shchitok_koltso_146103_3840x2160.jpg"),
        Image.from("https://images.wallpaperscraft.ru/image/kot_okno_vzgliad_146100_3840x2400.jpg"),
        Image.from("https://images.wallpaperscraft.ru/image/reka_obryv_skaly_146093_3840x2400.jpg"),
        Image.from("https://images.wallpaperscraft.ru/image/ozero_bereg_kamni_146091_3840x2400.jpg"),
        Image.from("https://images.wallpaperscraft.ru/image/gory_skaly_zasnezhennyj_146085_3840x2400.jpg"),
        Image.from("https://images.wallpaperscraft.ru/image/piatna_kraska_rzhavchina_146084_3840x2400.jpg"),
        Image.from("https://images.wallpaperscraft.ru/image/zdanie_arhitektura_minimalizm_146082_3840x2400.jpg"),
        Image.from("https://images.wallpaperscraft.ru/image/gora_vershina_pik_146078_3840x2400.jpg"),
        Image.from("https://images.wallpaperscraft.ru/image/setchatyj_struktura_relef_146075_3840x2400.jpg"),
        Image.from("https://images.wallpaperscraft.ru/image/zontiki_raznotsvetnyj_dekoratsiia_146072_3840x2400.jpg"),
        Image.from("https://images.wallpaperscraft.ru/image/tsvety_fioletovyj_buket_146070_3840x2400.jpg"),
        Image.from("https://images.wallpaperscraft.ru/image/okno_steklo_mokryj_146068_3840x2400.jpg"),
        Image.from("https://images.wallpaperscraft.ru/image/limon_dolki_pattern_146063_3840x2400.jpg"),
        Image.from("https://images.wallpaperscraft.ru/image/siluet_temnyj_zakat_146060_3840x2400.jpg"),
        Image.from("https://images.wallpaperscraft.ru/image/tsitata_chtenie_um_146059_3840x2400.jpg"),
        Image.from("https://images.wallpaperscraft.ru/image/devushka_siluet_solntse_146058_3840x2400.jpg"),
        Image.from("https://images.wallpaperscraft.ru/image/zdanie_arhitektura_sovremennyj_146056_3840x2400.jpg"),
        Image.from("https://images.wallpaperscraft.ru/image/mercedes_mashina_chernyj_146054_3840x2400.jpg"),
        Image.from("https://images.wallpaperscraft.ru/image/kamen_skala_sneg_146052_3840x2400.jpg"),
        Image.from("https://hubblesite.org/uploads/image_file/image_attachment/31803/STSCI-H-p1935b-m-2000x1827.png")
    )

    private val adapter = OmegaAutoAdapter.create(R.layout.item_test_3, ::onClickItem) {
        bindImage(R.id.imageview)
    }.apply {
        watcher = OmegaListAdapter.ImagePreloadWatcher(this)
        list = images
    }

    private val recyclerView: RecyclerView by bind(R.id.recyclerview, adapter) {
        recyclerView.setHasFixedSize(true)
    }

    private val maps: Map<Field, View> by bind(Field.values()) {
        showToast(Text.from(it.id.toString()))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = intent.getStringExtra(EXTRA_TITLE)
    }

    private fun onClickItem(item: Image) {
        showToast(Text.from("Click $item"))

//        ActivityLauncher.launch(this, null, createLauncher("1"), createLauncher("2"))

        createLauncher("1").launch(this, createLauncher("2"))
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
