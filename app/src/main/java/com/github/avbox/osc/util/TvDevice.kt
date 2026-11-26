package com.github.avbox.osc.util

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration

fun isTvDevice(context: Context): Boolean {
    val uiModeType = context.resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK
    if (uiModeType == Configuration.UI_MODE_TYPE_TELEVISION) return true
    val pm = context.packageManager
    return pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK) ||
        !pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
}
