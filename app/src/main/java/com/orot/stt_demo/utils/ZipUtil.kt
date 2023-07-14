package com.orot.stt_demo.utils

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipUtil {

    private val BUFFER = 80000
    private val COMPRESSION_LEVEL = 8
    fun zip(files: String, zipFileName: String?) {
        try {
            var origin: BufferedInputStream? = null
            val dest = FileOutputStream(zipFileName)
            val out = ZipOutputStream(
                BufferedOutputStream(
                    dest
                )
            )
            val data = ByteArray(BUFFER)
            val fi = FileInputStream(files)
            origin = BufferedInputStream(fi, BUFFER)
            val entry = ZipEntry(files.substring(files.lastIndexOf("/") + 1))
            out.putNextEntry(entry)
            out.setLevel(COMPRESSION_LEVEL)
            var count: Int
            while (origin.read(data, 0, BUFFER).also { count = it } != -1) {
                out.write(data, 0, count)
            }
            origin.close()
            out.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}