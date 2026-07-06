package com.kira.superspm.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

object ApkDownloader {
    private const val TAG = "ApkDownloader"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun downloadAndInstall(context: Context, url: String, versionName: String): Result<Unit> {
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
                        return@use Result.failure<Unit>(Exception("下载失败: ${response.code}"))
                    }

                    val body = response.body ?: throw Exception("响应体为空")
                    val inputStream = body.byteStream()
                    val outputStream = apkFile.outputStream()

                    inputStream.use { input ->
                        outputStream.use { output ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                            }
                            output.flush()
                        }
                    }
                }

                if (apkFile.exists() && apkFile.length() > 0) {
                    withContext(Dispatchers.Main) {
                        installApk(context, apkFile)
                    }
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("APK 文件下载失败"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "下载失败", e)
                Result.failure(e)
            }
        }
    }

    private fun installApk(context: Context, apkFile: File) {
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
            Log.e(TAG, "安装失败", e)
        }
    }
}
