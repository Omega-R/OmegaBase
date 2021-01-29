package com.omega_r.base.data.sources

/**
 * Created by Anton Knyazev on 2019-05-29.
 */
abstract class OmegaMockSource: Source {

    override val type: Source.Type
        get() = Source.Type.MOCK

}
