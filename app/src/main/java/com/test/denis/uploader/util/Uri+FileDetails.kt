package com.test.denis.uploader.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

fun Uri.filename(context: Context): String? {
    var name: String? = null

    fileInfo(context, arrayOf(MediaStore.Video.Media.DISPLAY_NAME)) {
        name = it.getString(0)
    }

    return name
}

fun Uri.fileSize(context: Context): Long? {
    var size: Long? = null

    fileInfo(context, arrayOf(MediaStore.Video.Media.SIZE)) {
        size = it.getLong(0)
    }

    return size
}

private fun Uri.fileInfo(context: Context, query: Array<String>, getInfo: (Cursor) -> Unit) {
    context.contentResolver.query(this, query, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            getInfo(cursor)
            cursor.close()
        }
    }
}
