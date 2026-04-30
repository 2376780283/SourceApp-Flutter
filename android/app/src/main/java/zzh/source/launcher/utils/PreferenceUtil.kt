package zzh.source.launcher.utils

import android.content.Context
import android.content.SharedPreferences

object PreferenceUtil {
    private const val PREF_NAME = "game_settings"

    const val KEY_GAME_ARGV = "game_argv"
    const val KEY_GAME_PATH = "game_path"
    const val KEY_SELECTED_GAME = "selected_game"
    const val KEY_HIGH_SWITCH = "high_switch"
    const val KEY_CONSOLE_SWITCH = "console_switch"
    const val KEY_GAMEPAD_UI = "gamepad_ui"
    const val KEY_SCALE_UI = "scale_ui"
    const val KEY_ENABLE_DEV = "enable_dev"
    const val KEY_ENABLE_FPS = "enable_fps"
    const val KEY_ENABLE_LOG = "enable_log"
    const val KEY_ENABLE_FAST = "enable_fast"

    private var sharedPreferences: SharedPreferences? = null

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences?.getString(key, defaultValue) ?: defaultValue
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences?.getBoolean(key, defaultValue) ?: defaultValue
    }

    fun setString(key: String, value: String) {
        sharedPreferences?.edit()?.putString(key, value)?.apply()
    }

    fun setBoolean(key: String, value: Boolean) {
        sharedPreferences?.edit()?.putBoolean(key, value)?.apply()
    }
}
