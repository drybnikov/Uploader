package com.test.denis.uploader.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import com.yalantis.ucrop.util.FileUtils

class FilenameUtils {
    companion object {
        fun removeExtension(filename: String) =
            if (filename.contains(".")) filename.substring(0, filename.lastIndexOf('.')) else filename

        fun extension(filename: String) =
            if (filename.contains(".")) filename.substring(filename.lastIndexOf(".") + 1, filename.length) else ""

        fun filenameWithoutExtension(context: Context, path: String) =
            removeExtension(Uri.parse(path).filename(context) ?: "")

        fun getRealPath(context: Context, uri: Uri): String? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && FileUtils.isDownloadsDocument(uri)) {
                val fileName = uri.filename(context)
                if (fileName != null) {
                    val uriToReturn = Uri.withAppendedPath(
                        Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath),
                        fileName
                    ).toString()

                    if (uriToReturn.isNotEmpty()) {
                        return uriToReturn
                    }
                }
            }
            return FileUtils.getPath(context, uri)
        }
    }
}
