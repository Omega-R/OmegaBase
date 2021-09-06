package com.omega_r.base.crash

import android.graphics.Bitmap


class CrashReport(
    val info: Map<String, Map<String, String>>,
    val stacktrace: String,
    val screenshot: Bitmap?
)