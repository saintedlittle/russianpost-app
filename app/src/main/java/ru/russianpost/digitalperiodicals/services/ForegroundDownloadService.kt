package ru.russianpost.digitalperiodicals.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.russianpost.digitalperiodicals.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.russianpost.digitalperiodicals.data.resource.Resource
import ru.russianpost.digitalperiodicals.downloadManager.DownloadManager
import javax.inject.Inject

@AndroidEntryPoint
class ForegroundDownloadService: Service() {

    @Inject
    lateinit var downloadManager: DownloadManager

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        val notificationId = 1001
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.download_is_going))
            .setSmallIcon(R.drawable.ic_baseline_download_24)
            .setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false)
            .setSilent(true)

        NotificationManagerCompat.from(this).apply {
            notify(notificationId, notificationBuilder.build())
            CoroutineScope(Dispatchers.IO).launch {

                downloadManager.downloadProgress.collect { filesDownloadProgressStatuses ->
                    Log.v("MY_TAG", "collected in service")
                    var maximumProgress = 0L
                    var currentProgress = 0L
                    filesDownloadProgressStatuses.forEach { entry ->
                        entry.value.let { fileProgressStatus ->
                            when(fileProgressStatus) {
                                is Resource.Loading -> {
                                    maximumProgress += (fileProgressStatus.data?.fileWeight ?: 0)
                                    currentProgress += (fileProgressStatus.data?.currentProgress ?: 0)
                                }
                                is Resource.Success -> {

                                }
                                else -> {

                                }
                            }
                        }
                    }
                    if (downloadManager.areAllLoadingsComplete()) {
                        Log.v("DownloadServiceLogger", "overall download is over")
                        maximumProgress = 0
                        currentProgress = 0
                        notificationBuilder
                            .setContentText(getString(R.string.download_is_over))
                            .setProgress(0, 0, false)
                        delay(1000)
                        notify(notificationId, notificationBuilder.build())
                    }
                    else {
                        var progressInPercentages = 0
                        if (maximumProgress != 0L) {
                            progressInPercentages = (100 * currentProgress / maximumProgress).toInt()
                        }
                        notificationBuilder
                            .setContentText(getString(R.string.download_is_going))
                            .setProgress(PROGRESS_MAX, progressInPercentages, false)
                        notify(notificationId, notificationBuilder.build())
                    }
                }
            }
        }
        startForeground(notificationId, notificationBuilder.build())
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    companion object {
        private const val CHANNEL_ID = "progress_download_channel"
        private const val PROGRESS_MAX = 100
        private const val PROGRESS_CURRENT = 0
    }
}
