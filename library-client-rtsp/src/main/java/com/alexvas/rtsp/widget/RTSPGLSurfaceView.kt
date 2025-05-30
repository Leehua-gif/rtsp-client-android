package com.alexvas.rtsp.widget

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.Surface
import com.alexvas.utils.MediaCodecHelper
import com.alexvas.utils.ShaderProgram
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class RTSPGLSurfaceView : GLSurfaceView {
    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        setEGLContextClientVersion(2)
        setRenderer(MirrorRenderer(context))
        renderMode = RENDERMODE_CONTINUOUSLY // 连续渲染模式
    }


    inner class MirrorRenderer(private val context: Context) : GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
        private lateinit var shaderProgram: ShaderProgram
        private var oesTextureId = -1
        private lateinit var surfaceTexture: SurfaceTexture
        private lateinit var mediaCodecHelper: MediaCodecHelper
        private val texMatrix = FloatArray(16)
        private var isMirrorEnabled = false
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            // 1. 创建 OES 纹理
            oesTextureId = createOESTexture()

            // 2. 初始化着色器程序
            shaderProgram = ShaderProgram(context)
            shaderProgram.init()

            // 3. 绑定 SurfaceTexture 到 MediaCodec
            surfaceTexture = SurfaceTexture(oesTextureId)
            surfaceTexture.setOnFrameAvailableListener(this)
            mediaCodecHelper = MediaCodecHelper(Surface(surfaceTexture)) // 传递 Surface 给解码器

            // 4. 启动 MediaCodec 解码线程
            mediaCodecHelper.startDecoding()
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        }

        override fun onDrawFrame(gl: GL10?) {
        }


        // 实现镜像开关
        fun toggleMirrorEffect() {
            isMirrorEnabled = !isMirrorEnabled
        }

        // SurfaceTexture 帧可用回调
        override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
            synchronized(this) {
                frameAvailable = true
            }
        }

        private var frameAvailable = false

        // 创建 OES 纹理
        private fun createOESTexture(): Int {
            val texIds = IntArray(1)
            GLES20.glGenTextures(1, texIds, 0)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texIds[0])
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            return texIds[0]
        }
    }
}