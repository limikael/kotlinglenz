import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import java.nio.FloatBuffer
import java.nio.ByteOrder
import java.nio.ByteBuffer
import java.lang.Math
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock

class MyGLRenderer : GLSurfaceView.Renderer {

	lateinit var glenzRenderer:GlenzRenderer;

	private val vPMatrix = FloatArray(16)
	private val projectionMatrix = FloatArray(16)
	private val viewMatrix = FloatArray(16)
	private val rotationMatrix = FloatArray(16)
	private val scratch = FloatArray(16)

	override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
		// Set the background frame color
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

		glenzRenderer=GlenzRenderer()
	}

	override fun onDrawFrame(unused: GL10) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

	    Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 5f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
	    Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

	    val angle = (0.1f*SystemClock.uptimeMillis())
	    val angleRad = Math.toRadians(angle.toDouble())

	    Matrix.setRotateM(rotationMatrix, 0, (.3f*angle) % 360f, 1f, 0f, 0f)
		Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0)

	    Matrix.setRotateM(rotationMatrix, 0, (1.0f*angle) % 360f, 0f, 1f, 0f)
		Matrix.multiplyMM(scratch, 0, scratch, 0, rotationMatrix, 0)

	    Matrix.setRotateM(rotationMatrix, 0, (1.5f*angle) % 360f, 0f, 0f, 1f)
		Matrix.multiplyMM(scratch, 0, scratch, 0, rotationMatrix, 0)

		Matrix.scaleM(scratch,0,1f,.75f+.25f*Math.sin(.35f*angleRad).toFloat(),1f)

		glenzRenderer.draw(
			scratch,
			.25f+.25f*Math.sin(.2f*angleRad).toFloat(),
			0.1f*angle
		);
	}

	override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
		GLES20.glViewport(0, 0, width, height)
	    val ratio: Float = width.toFloat() / height.toFloat()
	    Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 2f, 7f)
	}
}