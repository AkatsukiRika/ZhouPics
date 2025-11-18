package com.tgwgroup.zhoupics.base

import android.app.Activity

object ActivityCollector {
    private val activities = mutableListOf<Activity>()

    fun register(activity: Activity) {
        synchronized(activities) {
            if (!activities.contains(activity)) {
                activities.add(activity)
            }
        }
    }

    fun unregister(activity: Activity) {
        synchronized(activities) {
            activities.remove(activity)
        }
    }

    fun recreateAll() {
        // 复制一份避免并发修改
        val snapshot: List<Activity>
        synchronized(activities) {
            snapshot = activities.toList()
        }
        for (act in snapshot) {
            if (!act.isFinishing) {
                runCatching {
                    act.recreate()
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }
}