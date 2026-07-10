package com.example.tubetogether.updater

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val version: String,
    val releaseNotes: String,
    val downloadUrl: String
)

object UpdateManager {
    // Placeholder repo, user can change this
    private const val GITHUB_OWNER = "sas-deviq-iq"
    private const val GITHUB_REPO = "TubeTogether"
    
    suspend fun checkForUpdates(context: Context): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://raw.githubusercontent.com/$GITHUB_OWNER/$GITHUB_REPO/main/update.json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                val latestVersionStr = json.getString("version")
                val releaseNotes = json.getString("releaseNotes")
                val downloadUrl = json.getString("downloadUrl")
                
                val currentVersionStr = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
                
                // Compare versions (simple string compare for now, assuming standard semantic versioning)
                if (isNewerVersion(currentVersionStr, latestVersionStr)) {
                    return@withContext UpdateInfo(latestVersionStr, releaseNotes, downloadUrl)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
    
    private fun isNewerVersion(current: String, latest: String): Boolean {
        val currParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
        val length = maxOf(currParts.size, latestParts.size)
        for (i in 0 until length) {
            val curr = currParts.getOrElse(i) { 0 }
            val lat = latestParts.getOrElse(i) { 0 }
            if (lat > curr) return true
            if (lat < curr) return false
        }
        return false
    }

    fun downloadAndInstall(context: Context, downloadUrl: String) {
        try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "update.apk")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val request = DownloadManager.Request(Uri.parse(downloadUrl))
        request.setTitle("تحميل تحديث TubeTogether")
        request.setDescription("جاري تنزيل التحديث الجديد...")
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "update.apk")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    installApk(context)
                    context.unregisterReceiver(this)
                }
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
        
        Toast.makeText(context, "بدأ التحميل في الخلفية...", Toast.LENGTH_SHORT).show()
    }

    private fun installApk(context: Context) {
        try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "update.apk")
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "application/vnd.android.package-archive")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "فشل في تشغيل التثبيت", Toast.LENGTH_SHORT).show()
        }
    }
}
