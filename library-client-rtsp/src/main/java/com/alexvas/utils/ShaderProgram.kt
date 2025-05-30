package com.alexvas.utils

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.alexvas.rtsp.R
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ShaderProgram(private val context: Context) {
    private var programId = -1
    private var texMatrixHandle = -1
    private var positionHandle = -1
    private var texCoordHandle = -1
    private var mirrorHandle = -1

    // 初始化着色器
    fun init() {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, R.raw.vertex_shader)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, R.raw.fragment_shader)
        programId = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        // 获取 Uniform/Attribute 句柄
        positionHandle = GLES20.glGetAttribLocation(programId, "vPosition")
        texCoordHandle = GLES20.glGetAttribLocation(programId, "vTexCoord")
        texMatrixHandle = GLES20.glGetUniformLocation(programId, "uTexMatrix")
        mirrorHandle = GLES20.glGetUniformLocation(programId, "uMirror")
    }

    // 绘制纹理（支持镜像）:cite[2]
    fun draw(textureId: Int, texMatrix: FloatArray, isMirror: Boolean) {
        GLES20.glUseProgram(programId)

        // 绑定顶点坐标
        val vertexCoords = floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f)
        val vertexBuffer = ByteBuffer.allocateDirect(vertexCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(vertexCoords); position(0) }

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        // 绑定纹理坐标
        val texCoords = floatArrayOf(0f, 1f, 1f, 1f, 0f, 0f, 1f, 0f)
        val texBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(texCoords); position(0) }

        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texBuffer)

        // 传递矩阵和镜像参数
        GLES20.glUniformMatrix4fv(texMatrixHandle, 1, false, texMatrix, 0)
        GLES20.glUniform1i(mirrorHandle, if (isMirror) 1 else 0)

        // 绑定纹理并绘制
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun loadShader(type: Int, resId: Int): Int {
        val source = context.resources.openRawResource(resId).bufferedReader().use { it.readText() }
        return GLES20.glCreateShader(type).also {
            GLES20.glShaderSource(it, source)
            GLES20.glCompileShader(it)
        }
    }
}