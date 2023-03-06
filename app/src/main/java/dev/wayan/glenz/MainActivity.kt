package dev.wayan.glenz

import android.app.Activity;
import android.os.Bundle;
import MyGLSurfaceView;
import android.opengl.GLSurfaceView

class MainActivity : Activity() {

    private lateinit var gLView: GLSurfaceView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        gLView = MyGLSurfaceView(this)
        setContentView(gLView)
    }
}
