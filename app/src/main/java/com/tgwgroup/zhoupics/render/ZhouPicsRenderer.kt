package com.tgwgroup.zhoupics.render

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.Surface
import android.view.WindowManager
import com.tgwgroup.baselib.utils.LogUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ZhouPicsRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private var textureID: Int = -1
    private var programHandle: Int = 0
    private var positionHandle: Int = 0
    private var textureCoordHandle: Int = 0
    private var textureSamplerHandle: Int = 0

    private var textureData: ByteBuffer? = null
    private var textureWidth: Int = 0
    private var textureHeight: Int = 0
    private var rotation: Int = 0 // Image rotation angle
    @Volatile // Ensure visibility across threads for textureNeedsUpdate
    private var textureNeedsUpdate = false
    private val lock = Any() // Use Any() in Kotlin instead of Object()

    // Use lateinit for buffers guaranteed to be initialized in onSurfaceCreated
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var texCoordBuffer: FloatBuffer

    // View dimension properties
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0

    // Current texture coordinates (mutable reference)
    private var currentTextureCoords: FloatArray = TEXTURE_COORDS_0

    companion object {
        private const val TAG = "ZhouPicsRenderer" // Define TAG for logging

        // Vertex coordinates
        private val VERTICES = floatArrayOf(
            -1.0f, -1.0f, // Bottom left
            1.0f, -1.0f, // Bottom right
            -1.0f,  1.0f, // Top left
            1.0f,  1.0f  // Top right
        )

        // Texture coordinates - default (0° rotation)
        private val TEXTURE_COORDS_0 = floatArrayOf(
            0.0f, 1.0f, // Bottom left
            1.0f, 1.0f, // Bottom right
            0.0f, 0.0f, // Top left
            1.0f, 0.0f  // Top right
        )

        // Texture coordinates - 90° clockwise rotation
        private val TEXTURE_COORDS_90 = floatArrayOf(
            0.0f, 0.0f, // Top Left (original) -> Bottom Left (rotated)
            0.0f, 1.0f, // Bottom Left (original) -> Bottom Right (rotated)
            1.0f, 0.0f, // Top Right (original) -> Top Left (rotated)
            1.0f, 1.0f  // Bottom Right (original) -> Top Right (rotated)
            // Note: Original Java comments might be misleading after rotation logic.
            // The mapping describes the texture coordinates for the *rotated* vertex positions.
        )

        // Texture coordinates - 180° clockwise rotation
        private val TEXTURE_COORDS_180 = floatArrayOf(
            1.0f, 0.0f, // Top Right (original) -> Bottom Left (rotated)
            0.0f, 0.0f, // Top Left (original) -> Bottom Right (rotated)
            1.0f, 1.0f, // Bottom Right (original) -> Top Left (rotated)
            0.0f, 1.0f  // Bottom Left (original) -> Top Right (rotated)
        )

        // Texture coordinates - 270° clockwise rotation
        private val TEXTURE_COORDS_270 = floatArrayOf(
            1.0f, 1.0f, // Bottom Right (original) -> Bottom Left (rotated)
            1.0f, 0.0f, // Top Right (original) -> Bottom Right (rotated)
            0.0f, 1.0f, // Bottom Left (original) -> Top Left (rotated)
            0.0f, 0.0f  // Top Left (original) -> Top Right (rotated)
        )

        // Texture coordinates for front camera mirroring - default (0° rotation)
        // Note: This was defined but not used in the provided Java code's logic.
        // Keep it if needed, otherwise it can be removed.
        private val TEXTURE_COORDS_MIRROR_0 = floatArrayOf(
            1.0f, 1.0f, // Bottom left
            0.0f, 1.0f, // Bottom right
            1.0f, 0.0f, // Top left
            0.0f, 0.0f  // Top right
        )

        // Vertex Shader Code using raw string
        private const val VERTEX_SHADER_CODE = """
            attribute vec2 aPosition;
            attribute vec2 aTextureCoord;
            varying vec2 vTextureCoord;
            void main() {
              gl_Position = vec4(aPosition, 0.0, 1.0);
              vTextureCoord = aTextureCoord;
            }
        """

        // Fragment Shader Code using raw string
        private const val FRAGMENT_SHADER_CODE = """
            precision mediump float;
            varying vec2 vTextureCoord;
            uniform sampler2D uTexture;
            void main() {
              gl_FragColor = texture2D(uTexture, vTextureCoord);
            }
        """

        // Helper function to create and load a FloatBuffer
        private fun createFloatBuffer(data: FloatArray): FloatBuffer {
            return ByteBuffer.allocateDirect(data.size * Float.SIZE_BYTES) // Use Float.SIZE_BYTES
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .apply { // Use apply scope function for configuration
                    put(data)
                    position(0)
                }
        }
    } // End companion object

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // Initialize vertex buffer
        vertexBuffer = createFloatBuffer(VERTICES)

        // Initialize texture coordinate buffer with default (0 degrees)
        updateTexCoordBuffer(rotation) // Use initial rotation

        // Compile shaders
        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)

        // Create and link program
        programHandle = GLES20.glCreateProgram().also { handle ->
            GLES20.glAttachShader(handle, vertexShader)
            GLES20.glAttachShader(handle, fragmentShader)
            GLES20.glLinkProgram(handle)
            // Note: Consider adding glGetProgramiv(handle, GL_LINK_STATUS, ...) check
        }

        // Get shader attribute/uniform locations
        positionHandle = GLES20.glGetAttribLocation(programHandle, "aPosition")
        textureCoordHandle = GLES20.glGetAttribLocation(programHandle, "aTextureCoord")
        textureSamplerHandle = GLES20.glGetUniformLocation(programHandle, "uTexture")

        // Create texture
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureID = textures[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID)

        // Set texture parameters
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        // Detach and delete shaders after linking (optional but good practice)
        GLES20.glDetachShader(programHandle, vertexShader)
        GLES20.glDetachShader(programHandle, fragmentShader)
        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)
    }

    // Update texture coordinates based on rotation angle
    private fun updateTexCoordBuffer(newRotation: Int) {
        // Select appropriate texture coordinates using 'when' expression
        currentTextureCoords = when (newRotation) {
            90 -> TEXTURE_COORDS_90
            180 -> TEXTURE_COORDS_180
            270 -> TEXTURE_COORDS_270
            else -> TEXTURE_COORDS_0 // 0 degrees or default
        }
        // Update texture coordinate buffer
        texCoordBuffer = createFloatBuffer(currentTextureCoords)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        viewWidth = width
        viewHeight = height

        // Update vertex coordinates if texture dimensions are already known
        if (textureWidth > 0 && textureHeight > 0) {
            updateVertexCoordinates()
        }
    }

    // Update vertex coordinates to maintain video aspect ratio
    private fun updateVertexCoordinates() {
        if (viewWidth <= 0 || viewHeight <= 0 || textureWidth <= 0 || textureHeight <= 0) {
            return
        }

        val viewAspectRatio = viewWidth.toFloat() / viewHeight.toFloat()

        // Calculate texture aspect ratio considering rotation
        val rotatedTextureWidth = if (rotation == 90 || rotation == 270) textureHeight else textureWidth
        val rotatedTextureHeight = if (rotation == 90 || rotation == 270) textureWidth else textureHeight
        val textureAspectRatio = rotatedTextureWidth.toFloat() / rotatedTextureHeight.toFloat()

        var scaleX = 1.0f
        var scaleY = 1.0f

        if (textureAspectRatio > viewAspectRatio) {
            // Texture is wider than the view (letterbox)
            scaleY = viewAspectRatio / textureAspectRatio
        } else {
            // Texture is taller than the view (pillarbox)
            scaleX = textureAspectRatio / viewAspectRatio
        }

        // Adjust vertices based on the calculated scale factors
        val adjustedVertices = floatArrayOf(
            VERTICES[0] * scaleX, VERTICES[1] * scaleY, // Bottom left
            VERTICES[2] * scaleX, VERTICES[3] * scaleY, // Bottom right
            VERTICES[4] * scaleX, VERTICES[5] * scaleY, // Top left
            VERTICES[6] * scaleX, VERTICES[7] * scaleY  // Top right
        )

        // Update vertex buffer
        vertexBuffer = createFloatBuffer(adjustedVertices)

        LogUtil.d(TAG, "Aspect ratio updated: View=$viewAspectRatio, Texture=$textureAspectRatio (Rotated), Rotation=$rotation, ScaleX=$scaleX, ScaleY=$scaleY")
    }


    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Update texture if needed (synchronized block)
        synchronized(lock) {
            if (textureNeedsUpdate && textureData != null) {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID)
                // Use rewind() before glTexImage2D if the buffer position might have changed elsewhere
                textureData?.rewind()
                GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, textureWidth,
                    textureHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                    textureData
                )
                textureNeedsUpdate = false // Reset flag after update
            }
        }

        // Use shader program
        GLES20.glUseProgram(programHandle)

        // Set vertex coordinates
        vertexBuffer.position(0) // Ensure buffer position is reset before reading
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(positionHandle)

        // Set texture coordinates
        texCoordBuffer.position(0) // Ensure buffer position is reset before reading
        GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer)
        GLES20.glEnableVertexAttribArray(textureCoordHandle)

        // Set texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID)
        GLES20.glUniform1i(textureSamplerHandle, 0) // Tell sampler to use texture unit 0

        // Draw the quad (using triangle strip)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(textureCoordHandle)

        // Unbind texture (optional, good practice)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        // Unbind program (optional, good practice)
        GLES20.glUseProgram(0)
    }

    // Update texture data and handle rotation
    fun updateTextureData(data: ByteArray, width: Int, height: Int, sensorOrientation: Int) {
        synchronized(lock) {
            val sizeChanged = (textureWidth != width || textureHeight != height)

            // Allocate or reallocate buffer if size changes or it's the first time
            if (textureData == null || sizeChanged) {
                textureWidth = width
                textureHeight = height
                textureData = ByteBuffer.allocateDirect(width * height * 4) // RGBA format assumed (4 bytes/pixel)
                    .order(ByteOrder.nativeOrder())

                LogUtil.d(TAG, "Texture buffer allocated/resized: ${width}x${height}")
            }

            // Put new data into the buffer
            textureData?.rewind() // Rewind before putting data
            textureData?.put(data)
            textureData?.position(0) // Set position to beginning for reading by GL
            textureNeedsUpdate = true

            // Calculate new rotation and check if it changed
            val newRotation = getRotationDegrees(sensorOrientation)
            val rotationChanged = (newRotation != rotation)

            if (rotationChanged) {
                LogUtil.d(TAG, "Rotation changed from $rotation to $newRotation")
                rotation = newRotation
                updateTexCoordBuffer(rotation) // Update texture coords if rotation changed
            }

            // Update vertex coordinates if size or rotation changed to maintain aspect ratio
            if (sizeChanged || rotationChanged) {
                updateVertexCoordinates()
            }
        }
    }

    // Get appropriate rotation angle based on sensor orientation and device rotation
    // Assumes this class is an inner class of an Activity or has access to getWindowManager()
    private fun getRotationDegrees(sensorOrientation: Int): Int {
        // Requires context access, assuming inner class of Activity here.
        // If not, pass Context via constructor.
        val windowManager = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        val deviceRotation = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            context.display.rotation
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.rotation
        }

        val deviceOrientationDegrees = when (deviceRotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }

        // Formula for camera preview rotation relative to display orientation
        // See: https://developer.android.com/reference/android/hardware/Camera.CameraInfo#orientation
        val rotationDegrees = (sensorOrientation - deviceOrientationDegrees + 360) % 360

        LogUtil.v(TAG, "Sensor Orientation: $sensorOrientation, Device Orientation: $deviceOrientationDegrees, Calculated Rotation: $rotationDegrees")
        return rotationDegrees
    }


    // Compile shader helper function
    private fun compileShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        // Check compilation status
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

        if (compileStatus[0] == 0) {
            val errorInfo = GLES20.glGetShaderInfoLog(shader)
            LogUtil.e(TAG, "Error compiling shader (type $type): $errorInfo")
            GLES20.glDeleteShader(shader)
            // Consider throwing an exception here instead of returning 0
            // throw RuntimeException("Error compiling shader: $errorInfo")
            return 0 // Return 0 indicates failure
        }
        LogUtil.v(TAG, "Shader compiled successfully (type $type, handle $shader)")
        return shader
    }
}