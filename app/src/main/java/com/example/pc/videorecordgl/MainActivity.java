package com.example.pc.videorecordgl;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GLSurfaceView surfaceView = findViewById(R.id.surface);
        surfaceView.setRenderer(new MyGlSurfaceRender());
    }
}
