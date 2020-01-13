package com.omega_r.base.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

class Constants {

    companion object {

        internal const val UNIT = "kotlin.Unit"
        internal val CLASS_NAME_SOURCE = ClassName.bestGuess("com.omega_r.base.data.sources.Source")
        internal val CLASS_NAME_OMEGA_REPOSITORY = ClassName.bestGuess("com.omega_r.base.data.OmegaRepository")

        internal val CLASS_NAME_STRATEGY = ClassName.bestGuess("com.omega_r.base.data.OmegaRepository.Strategy")

        internal val UNIT_CLASS_NAME = ClassName.bestGuess(UNIT)
        internal val CHANNEL_CLASS_NAME = ClassName.bestGuess("kotlinx.coroutines.channels.Channel")
        internal val SEND_CHANNEL_CLASS_NAME = ClassName.bestGuess("kotlinx.coroutines.channels.SendChannel")
        internal val RECEIVE_CHANNEL_CLASS_NAME = ClassName.bestGuess("kotlinx.coroutines.channels.ReceiveChannel")

        internal val CLASS_NAME_ERROR_HANDLER = ClassName.bestGuess("com.omega_r.base.errors.ErrorHandler")

        internal val REMOTE_ELSE_CACHE = "Strategy.REMOTE_ELSE_CACHE"

        internal val MEMBER_NAME_CONSUME_EACH = MemberName("kotlinx.coroutines.channels", "consumeEach")
        internal val MEMBER_RUN_BLOCKING = MemberName("kotlinx.coroutines", "runBlocking")

    }

}