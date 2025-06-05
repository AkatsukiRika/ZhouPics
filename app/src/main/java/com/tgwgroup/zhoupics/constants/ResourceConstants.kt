package com.tgwgroup.zhoupics.constants

import android.content.Context
import java.io.File

const val HOSTING_BASE_URL = "https://zhoupics-114514.web.app/"

const val ELIMINATE_MODEL_NAME = "migan_pipeline_v2.mnn"

fun getModelDir(context: Context): File {
    return File(context.filesDir, "model")
}