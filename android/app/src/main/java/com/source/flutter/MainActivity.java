package com.source.flutter;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.libsdl.app.SDLActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import zzh.source.launcher.utils.PreferenceUtil;

public class MainActivity extends FlutterActivity {
    private static final String channel = "runIntermediaryActivity";
    private static final String permissionChannel = "requestPermissions";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] REQUIRED_PERMISSIONS = {
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        PreferenceUtil.INSTANCE.init(this);

        new MethodChannel(getFlutterEngine().getDartExecutor().getBinaryMessenger(), channel).setMethodCallHandler(
                new MethodChannel.MethodCallHandler() {
                    @Override
                    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
                        if (methodCall.method.equals("runGame")) {
                            Map<String, Object> args = (Map<String, Object>) methodCall.arguments;
                            saveSettings(args);
                            runIntermediaryActivity();
                            result.success(null);
                        } else {
                            result.notImplemented();
                        }
                    }
                }
        );

        new MethodChannel(getFlutterEngine().getDartExecutor().getBinaryMessenger(), permissionChannel).setMethodCallHandler(
                new MethodChannel.MethodCallHandler() {
                    @Override
                    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
                        if (methodCall.method.equals("checkPermissions")) {
                            result.success(hasAllPermissions());
                        } else if (methodCall.method.equals("requestPermissions")) {
                            requestPermissions();
                            result.success(null);
                        } else {
                            result.notImplemented();
                        }
                    }
                }
        );
    }

    private boolean hasAllPermissions() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            return true;
        }
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
        }
    }

    private void saveSettings(Map<String, Object> args) {
        if (args == null) return;

        if (args.containsKey("game_path")) PreferenceUtil.INSTANCE.setString(PreferenceUtil.KEY_GAME_PATH, (String) args.get("game_path"));
        if (args.containsKey("game_argv")) PreferenceUtil.INSTANCE.setString(PreferenceUtil.KEY_GAME_ARGV, (String) args.get("game_argv"));
        if (args.containsKey("selected_game")) PreferenceUtil.INSTANCE.setString(PreferenceUtil.KEY_SELECTED_GAME, (String) args.get("selected_game"));
        
        if (args.containsKey("enable_dev")) PreferenceUtil.INSTANCE.setBoolean(PreferenceUtil.KEY_ENABLE_DEV, (Boolean) args.get("enable_dev"));
        if (args.containsKey("console_switch")) PreferenceUtil.INSTANCE.setBoolean(PreferenceUtil.KEY_CONSOLE_SWITCH, (Boolean) args.get("console_switch"));
        if (args.containsKey("gamepad_ui")) PreferenceUtil.INSTANCE.setBoolean(PreferenceUtil.KEY_GAMEPAD_UI, (Boolean) args.get("gamepad_ui"));
        if (args.containsKey("enable_fps")) PreferenceUtil.INSTANCE.setBoolean(PreferenceUtil.KEY_ENABLE_FPS, (Boolean) args.get("enable_fps"));
    }

    public void runIntermediaryActivity() {
        Intent intent = new Intent(this, IntermediaryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
