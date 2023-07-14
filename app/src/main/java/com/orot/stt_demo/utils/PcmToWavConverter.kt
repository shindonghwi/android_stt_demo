package com.orot.stt_demo.utils

import android.media.AudioFormat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

object PcmToWavConverter {

    /** wav 파일로 작성하는 기능
     * fos 생성 후에 호출한다.
     * */
    fun writeWavHeader(
        out: FileOutputStream,
        channelMask: Int,
        sampleRate: Int,
        encoding: Int
    ) {
        val channels: Short = when (channelMask) {
            AudioFormat.CHANNEL_IN_MONO -> 1
            AudioFormat.CHANNEL_IN_STEREO -> 2
            else -> throw IllegalArgumentException("Unacceptable channel mask")
        }
        val bitDepth: Short = when (encoding) {
            AudioFormat.ENCODING_PCM_8BIT -> 8
            AudioFormat.ENCODING_PCM_16BIT -> 16
            AudioFormat.ENCODING_PCM_FLOAT -> 32
            else -> throw IllegalArgumentException("Unacceptable encoding")
        }
        writeWavHeaderContent(out, channels, sampleRate, bitDepth)
    }

    private fun writeWavHeaderContent(
        out: FileOutputStream,
        channels: Short,
        sampleRate: Int,
        bitDepth: Short
    ) {
        val littleBytes = ByteBuffer
            .allocate(14)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putShort(channels)
            .putInt(sampleRate)
            .putInt(sampleRate * channels * (bitDepth / 8))
            .putShort((channels * (bitDepth / 8)).toShort())
            .putShort(bitDepth)
            .array()

        out.write(
            byteArrayOf( // RIFF header
                'R'.code.toByte(),
                'I'.code.toByte(),
                'F'.code.toByte(),
                'F'.code.toByte(),  // ChunkID
                0,
                0,
                0,
                0,  // ChunkSize (must be updated later)
                'W'.code.toByte(),
                'A'.code.toByte(),
                'V'.code.toByte(),
                'E'.code.toByte(),  // Format
                // fmt subchunk
                'f'.code.toByte(),
                'm'.code.toByte(),
                't'.code.toByte(),
                ' '.code.toByte(),  // Subchunk1ID
                16,
                0,
                0,
                0,  // Subchunk1Size
                1,
                0,  // AudioFormat
                littleBytes[0],
                littleBytes[1],  // NumChannels
                littleBytes[2],
                littleBytes[3],
                littleBytes[4],
                littleBytes[5],  // SampleRate
                littleBytes[6],
                littleBytes[7],
                littleBytes[8],
                littleBytes[9],  // ByteRate
                littleBytes[10],
                littleBytes[11],  // BlockAlign
                littleBytes[12],
                littleBytes[13],  // BitsPerSample
                // data subchunk
                'd'.code.toByte(),
                'a'.code.toByte(),
                't'.code.toByte(),
                'a'.code.toByte(),  // Subchunk2ID
                0,
                0,
                0,
                0
            )
        )
    }

    /** wav 파일로 업데이트 하는 기능
     * fos close 후에 호출한다.
     * */
    fun updateWavHeader(wav: File) {
        val sizes = ByteBuffer
            .allocate(8)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt((wav.length() - 8).toInt()) // ChunkSize
            .putInt((wav.length() - 44).toInt()) // Subchunk2Size
            .array()
        var accessWave: RandomAccessFile? = null
        try {
            accessWave = RandomAccessFile(wav, "rw")
            accessWave.seek(4)
            accessWave.write(sizes, 0, 4)
            accessWave.seek(40)
            accessWave.write(sizes, 4, 4)
        } catch (ex: IOException) {
            throw ex
        } finally {
            if (accessWave != null) {
                try {
                    accessWave.close()
                } catch (ex: IOException) {
                    //
                }
            }
        }
    }


}