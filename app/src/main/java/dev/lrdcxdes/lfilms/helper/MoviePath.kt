package dev.lrdcxdes.lfilms.helper

import android.net.Uri

fun decodePath(encodedPath: String): String {
    return Uri.decode(encodedPath)
}

fun encodePath(path: String): String {
    return Uri.encode(path)
}
