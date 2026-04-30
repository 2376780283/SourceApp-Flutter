package com.source.flutter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;

import org.libsdl.app.SDLActivity;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

import androidx.appcompat.app.AppCompatActivity;

public class IntermediaryActivity extends Activity {
    private static final int SDL_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        runGame();
    }

    public void runGame() {
        Intent intent = new Intent(this, SDLActivity.class);
        startActivityForResult(intent, SDL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SDL_REQUEST_CODE) {
            finish();
        }
    }
}
