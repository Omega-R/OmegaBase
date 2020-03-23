package com.omega_r.base.processor.extensions

import com.omega_r.base.processor.Constants.Companion.CHANNEL_CLASS_NAME
import com.omega_r.base.processor.Constants.Companion.RECEIVE_CHANNEL_CLASS_NAME
import com.omega_r.base.processor.Constants.Companion.SEND_CHANNEL_CLASS_NAME

fun String.removeChannelSuffix(): String {
    return removeSuffix(SEND_CHANNEL_CLASS_NAME.simpleName)
        .removeSuffix(RECEIVE_CHANNEL_CLASS_NAME.simpleName)
        .removeSuffix(CHANNEL_CLASS_NAME.simpleName)
}