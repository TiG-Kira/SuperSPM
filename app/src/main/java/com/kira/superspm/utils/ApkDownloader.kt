package com.kira.superspm.utils

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.kira.superspm.R
import java.io.File
import java.util.concurrent.TimeUnit

object ApkDownloader {
    private const val TAG = "ApkDownloader"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    fun isChineseRom(): Boolean {
        val manufacturers = listOf(
            "Xiaomi".uppercase(),
            "Redmi".uppercase(),
            "POCO".uppercase(),
            "OPPO".uppercase(),
            "OnePlus".uppercase(),
            "realme".uppercase(),
            "vivo".uppercase(),
            "iQOO".uppercase(),
            "Huawei".uppercase(),
            "Honor".uppercase(),
            "Meizu".uppercase(),
            "Smartisan".uppercase(),
            "Nubia".uppercase(),
            "ZTE".uppercase()
        )
        val brand = Build.BRAND?.uppercase() ?: ""
        val manufacturer = Build.MANUFACTURER?.uppercase() ?: ""
        return manufacturers.any { it in brand || it in manufacturer }
    }

    fun hasInstallPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun requestInstallPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, context.getString(R.string.request_install_permission_failed), e)
            }
        }
    }

    fun tryInstallApkSilent(context: Context, apkFile: File): Boolean {
        return try {
            installApk(context, apkFile)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun downloadAndInstall(
        context: Context,
        url: String,
        versionName: String,
        onProgress: ((Int, Long, Long) -> Unit)? = null
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val apkFile = File(context.getExternalFilesDir(null), "SuperSPM_debug_${versionName}.apk")

                if (apkFile.exists()) {
                    apkFile.delete()
                }

                val request = Request.Builder()
                    .url(url)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@use Result.failure<Unit>(Exception(context.getString(R.string.download_failed_with_code, response.code)))
                    }

                    val body = response.body ?: throw Exception(context.getString(R.string.response_body_empty))
                    val contentLength = body.contentLength()
                    val inputStream = body.byteStream()
                    val outputStream = apkFile.outputStream()

                    var bytesDownloaded: Long = 0

                    inputStream.use { input ->
                        outputStream.use { output ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                bytesDownloaded += bytesRead
                                if (contentLength > 0 && onProgress != null) {
                                    val progress = ((bytesDownloaded * 100) / contentLength).toInt()
                                    onProgress(progress, bytesDownloaded, contentLength)
                                }
                            }
                            output.flush()
                        }
                    }
                }

                if (apkFile.exists() && apkFile.length() > 0) {
                    withContext(Dispatchers.Main) {
                        if (isChineseRom()) {
                            try {
                                installApk(context, apkFile)
                            } catch (e: Exception) {
                                requestInstallPermission(context)
                            }
                        } else {
                            if (hasInstallPermission(context)) {
                                installApk(context, apkFile)
                            } else {
                                requestInstallPermission(context)
                            }
                        }
                    }
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(context.getString(R.string.apk_download_failed)))
                }
            } catch (e: Exception) {
                Log.e(TAG, context.getString(R.string.download_failed), e)
                Result.failure(e)
            }
        }
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val apkUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
            } else {
                Uri.fromFile(apkFile)
            }

            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, context.getString(R.string.install_failed), e)
            throw e
        }
    }

    fun getDownloadedApkFile(context: Context, versionName: String): File {
        return File(context.getExternalFilesDir(null), "SuperSPM_debug_${versionName}.apk")
    }
}
