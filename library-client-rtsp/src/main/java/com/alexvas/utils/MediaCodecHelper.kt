package com.alexvas.utils

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.view.Surface
import com.alexvas.rtsp.R

class MediaCodecHelper(private val outputSurface: Surface) {
    private lateinit var mediaCodec: MediaCodec
    private var isRunning = false

    fun startDecoding() {
        Thread {
            // 1. 初始化 MediaExtractor（示例：从 raw 资源读取）
            val extractor = MediaExtractor()
            context.resources.openRawResourceFd(R.raw.sample).use {
                extractor.setDataSource(it)
            }
            val videoTrack = selectVideoTrack(extractor)

            // 2. 配置 MediaCodec 解码器 :cite[8]
            val format = extractor.getTrackFormat(videoTrack)
            mediaCodec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
            mediaCodec.configure(format, outputSurface, null, 0)
            mediaCodec.start()

            // 3. 解码循环
            isRunning = true
            val bufferInfo = MediaCodec.BufferInfo()
            while (isRunning) {
                val inputIndex = mediaCodec.dequeueInputBuffer(10_000)
                if (inputIndex >= 0) {
                    val buffer = mediaCodec.getInputBuffer(inputIndex)!!
                    val sampleSize = extractor.readSampleData(buffer, 0)
                    if (sampleSize > 0) {
                        mediaCodec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, 0)
                        extractor.advance()
                    } else {
                        mediaCodec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    }
                }

                val outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10_000)
                if (outputIndex >= 0) {
                    mediaCodec.releaseOutputBuffer(outputIndex, true)
                } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // 处理格式变化
                }
            }
            // 4. 释放资源
            mediaCodec.stop()
            extractor.release()
        }.start()
    }

    fun stopDecoding() {
        isRunning = false
    }

    private fun selectVideoTrack(extractor: MediaExtractor): Int {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            if (format.getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true) {
                extractor.selectTrack(i)
                return i
            }
        }
        throw IllegalStateException("No video track found")
    }
}