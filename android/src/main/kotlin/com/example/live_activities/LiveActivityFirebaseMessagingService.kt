package com.example.live_activities

import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class LiveActivityFirebaseMessagingService : FirebaseMessagingService() {

    private fun jsonDecode(json: String): Map<String, Any> {
        val jsonObject = JSONObject(json)
        val map = mutableMapOf<String, Any>()
        jsonObject.keys().forEach { key ->
            map[key] = jsonObject.get(key)
        }
        return map
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val liveActivityManager = LiveActivityManagerHolder.instance
        if (liveActivityManager == null) {
            Log.e("LiveActivity", "LiveActivityManager is null")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val args = remoteMessage.data

                val event = args["event"]
                val id = args["activity-id"]

                if (event == null || id == null) {
                    Log.e("LiveActivity", "Missing required FCM fields: $args")
                    return@launch
                }

                val timestamp =
                    args["timestamp"]?.toLongOrNull() ?: 0L

                val rawData = args["content-state"]
                val data = try {
                    jsonDecode(rawData ?: "{}")
                } catch (e: Exception) {
                    emptyMap()
                }

                when (event) {
                    "update" -> {
                        liveActivityManager.updateActivity(id, timestamp, data)
                    }

                    "end" -> {
                        liveActivityManager.endActivity(id, data)
                    }

                    else -> {
                        Log.e("LiveActivity", "Unknown event type: $event")
                    }
                }

            } catch (e: Exception) {
                Log.e(
                    "LiveActivityFirebaseMessagingService",
                    "Error while parsing or processing FCM",
                    e
                )
            }
        }
    }


//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
//
//        val liveActivityManager = LiveActivityManagerHolder.instance!!
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val args = remoteMessage.data
//                val event = args["event"] as String
//                val data = jsonDecode(args["content-state"] ?: "{}")
//                val id = args["activity-id"] as String
//                val timestamp =
//                    (args["timestamp"] as? String)?.toLongOrNull() ?: 0L
//
//                when (event) {
//                    "update" -> {
//                        liveActivityManager.updateActivity(id, timestamp, data)
//                    }
//
//                    "end" -> {
//                        liveActivityManager.endActivity(id, data)
//                    }
//
//                    else -> {
//                        throw IllegalArgumentException("Unknown event type: $event")
//                    }
//                }
//
//            } catch (e: Exception) {
//                Log.e(
//                    "LiveActivityFirebaseMessagingService",
//                    "Error while parsing or processing FCM",
//                    e
//                )
//            }
//        }
//    }
}
