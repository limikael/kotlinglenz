import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ByteOrder
import java.nio.ByteBuffer
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.graphics.Color;

fun makeIntBuffer(intArray:IntArray):IntBuffer {
	return ByteBuffer.allocateDirect(intArray.size * 4).run {
		order(ByteOrder.nativeOrder())
		asIntBuffer().apply {
			put(intArray)
			position(0)
		}
	}
}

fun makeFloatBuffer(floatArray:FloatArray):FloatBuffer {
	return ByteBuffer.allocateDirect(floatArray.size * 4).run {
		order(ByteOrder.nativeOrder())
		asFloatBuffer().apply {
			put(floatArray)
			position(0)
		}
	}
}

class GlenzRenderer {
	private var program:Int=0

	private var vertexBuffer:FloatBuffer=makeFloatBuffer(floatArrayOf(
		-1f,-1f,-1f, // 0
		 1f,-1f,-1f, // 1
		-1f, 1f,-1f, // 2
		 1f, 1f,-1f, // 3

		-1f,-1f, 1f, // 4
		 1f,-1f, 1f, // 5
		-1f, 1f, 1f, // 6
		 1f, 1f, 1f, // 7

		-1f, 0f, 0f, // 8
		 1f, 0f, 0f, // 9
		 0f,-1f, 0f, // 10
		 0f, 1f, 0f, // 11
		 0f, 0f,-1f, // 12
		 0f, 0f, 1f, // 13
	))

	private var vertexScaleBuffer=makeFloatBuffer(floatArrayOf(
		0f,0f,0f,0f,
		0f,0f,0f,0f,

		1f,1f,1f,1f,1f,1f
	))

	private var primaryIndices=intArrayOf(
		1,0,12, 2,3,12,
		4,5,13, 7,6,13,

		2,0,8,  4,6,8,
		1,3,9,  7,5,9,

		4,0,10, 1,5,10,
		2,6,11, 7,3,11,
	)

	private var primaryBuffer:IntBuffer=makeIntBuffer(primaryIndices)

	private var secondaryIndices=intArrayOf(
		0,2,12, 3,1,12,
		5,7,13, 6,4,13,

		0,4,8, 6,2,8,
		3,7,9, 5,1,9,

		0,1,10, 5,4,10,
		6,7,11, 3,2,11
	)

	private var secondaryBuffer:IntBuffer=makeIntBuffer(secondaryIndices)

	private var positionHandle:Int=0
	private var colorHandle:Int=0
	private var vPMatrixHandle:Int=0
	private var vertexScaleHandle:Int=0
	private var scaleHandle:Int=0

	constructor() {
		val vertexShader=GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER).also{shader->
			GLES20.glShaderSource(shader,
				"uniform mat4 uMVPMatrix;" +
				"uniform float scale;"+
				"attribute vec4 vPosition;"+
				"attribute float vertexScale;"+
				"void main() {"+
				"  float scl=scale*vertexScale+1.0;"+
				"  vec4 scalev=vec4(scl,scl,scl,1.0);"+
				"  gl_Position=uMVPMatrix*(vPosition*scalev);"+
				"}")
			GLES20.glCompileShader(shader)
		}

		val fragmentShader=GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER).also{shader->
			GLES20.glShaderSource(shader,
				"precision mediump float;" +
				"uniform vec4 vColor;" +
				"void main() {" +
				"  gl_FragColor = vColor;" +
				"}")
			GLES20.glCompileShader(shader)
		}

		program=GLES20.glCreateProgram().also {
			GLES20.glAttachShader(it, vertexShader)
			GLES20.glAttachShader(it, fragmentShader)
			GLES20.glLinkProgram(it)
		}

		positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
		colorHandle = GLES20.glGetUniformLocation(program, "vColor")
		vPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
		vertexScaleHandle = GLES20.glGetAttribLocation(program, "vertexScale")
		scaleHandle = GLES20.glGetUniformLocation(program, "scale")
	}

	fun draw(mvpMatrix:FloatArray, peaky:Float, colorAngle:Float) {
		val c=Color.valueOf(Color.HSVToColor(floatArrayOf(colorAngle%360f,1f,1f)));
		

		GLES20.glUseProgram(program);

	    GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)
	    GLES20.glUniform1f(scaleHandle,peaky)

		GLES20.glEnableVertexAttribArray(positionHandle)
		GLES20.glVertexAttribPointer(
				positionHandle,
				3, // per vertex
				GLES20.GL_FLOAT,
				false,
				3*4, // stride
				vertexBuffer
		)

		GLES20.glEnableVertexAttribArray(vertexScaleHandle)
		GLES20.glVertexAttribPointer(
				vertexScaleHandle,
				1,
				GLES20.GL_FLOAT,
				false,
				4,
				vertexScaleBuffer
		)

		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE_MINUS_SRC_ALPHA);

		GLES20.glCullFace(GLES20.GL_FRONT);
		GLES20.glUniform4fv(colorHandle, 1, floatArrayOf(.5f*c.red(),.5f*c.green(),.5f*c.blue(),.5f), 0)
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, primaryIndices.size, GLES20.GL_UNSIGNED_INT, primaryBuffer)
		GLES20.glUniform4fv(colorHandle, 1, floatArrayOf(1f,1f,1f,.5f), 0)
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, secondaryIndices.size, GLES20.GL_UNSIGNED_INT, secondaryBuffer)

		GLES20.glCullFace(GLES20.GL_BACK);
		GLES20.glUniform4fv(colorHandle, 1, floatArrayOf(.5f*c.red(),.5f*c.green(),.5f*c.blue(),.75f), 0)
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, primaryIndices.size, GLES20.GL_UNSIGNED_INT, primaryBuffer)
		GLES20.glUniform4fv(colorHandle, 1, floatArrayOf(1f,1f,1f,.75f), 0)
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, secondaryIndices.size, GLES20.GL_UNSIGNED_INT, secondaryBuffer)

		GLES20.glDisableVertexAttribArray(positionHandle)
		GLES20.glDisableVertexAttribArray(vertexScaleHandle)
	}
}