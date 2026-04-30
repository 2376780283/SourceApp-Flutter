package zzh.source.launcher.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import java.util.Locale

import zzh.source.launcher.utils.nillerusr.ExtractAssets

class GameBridge {

    companion object {
        const val GAME_MODE_HLS = "hl1"
        const val GAME_MODE_CS = "cstrike"

        private val DISABLE_GAMEPAD_UI_GAMES = setOf(GAME_MODE_HLS, GAME_MODE_CS)
        private val MP_GAMES = setOf(GAME_MODE_CS)

        @JvmStatic external fun setArgs(args: String)

        @JvmStatic external fun setenv(name: String, value: String, overwrite: Int): Int

        @SuppressLint("StaticFieldLeak") private var context: Context? = null

        @JvmStatic
        fun initNatives(context: Context, intent: Intent?) {
            this.context = context
            PreferenceUtil.init(context)
            ExtractAssets.extractAssets(context)
            val prefs = PreferenceUtil
            val argv = prefs.getString(PreferenceUtil.KEY_GAME_ARGV, "-nobackgroundlevel -full")
            val path =
                prefs.getString(PreferenceUtil.KEY_GAME_PATH, "/storage/emulated/0/SourceApp/")
            val gameName = prefs.getString(PreferenceUtil.KEY_SELECTED_GAME, "hl2") ?: "hl2"
            val isHighMode = prefs.getBoolean(PreferenceUtil.KEY_HIGH_SWITCH, false)
            val isConsoleEnabled = prefs.getBoolean(PreferenceUtil.KEY_CONSOLE_SWITCH, false)
            var isGamepaduiEnabled = prefs.getBoolean(PreferenceUtil.KEY_GAMEPAD_UI, false)
            val isAutoScaleUIEnabled = prefs.getBoolean(PreferenceUtil.KEY_SCALE_UI, false)
            val isDevEnabled = prefs.getBoolean(PreferenceUtil.KEY_ENABLE_DEV, false)
            val isFpsEnabled = prefs.getBoolean(PreferenceUtil.KEY_ENABLE_FPS, false)
            val isLogEnabled = prefs.getBoolean(PreferenceUtil.KEY_ENABLE_LOG, false)
            val isFastEnabled = prefs.getBoolean(PreferenceUtil.KEY_ENABLE_FAST, false)

            if (gameName in DISABLE_GAMEPAD_UI_GAMES) {
                false.also { isGamepaduiEnabled = it }
            }

            val fullArgs = buildString {
                append("game.arm -game $gameName ")
                if (isDevEnabled) append("-showdevextra ")
                if (isFastEnabled) append("-nobackgroundlevel ")
                if (isConsoleEnabled) append("-console ")
                if (isAutoScaleUIEnabled) append("-autoscaleui ")
                if (isGamepaduiEnabled) append("-gamepadui ")
                if (isFpsEnabled) append("+cl_showfps 1 ")
                if (!isLogEnabled) append("-nolog ")
                if (isHighMode) append("-high ")
                append(argv)
            }

            val filesDir = context.filesDir.path
            val vpkPath = "$filesDir/${ExtractAssets.VPK_NAME}"

            val vpkPathTouch =
                if (gameName in MP_GAMES) {
                    "$filesDir/${ExtractAssets.VPK_NAMEMPTOUCH}"
                } else {
                    "$filesDir/${ExtractAssets.VPK_NAMETOUCH}"
                }

            val libPath = "$filesDir/runtime/$gameName"
            val libEnginePath = context.applicationInfo.nativeLibraryDir
            setenv("APP_MOD_LIB", libPath, 1)
            setenv("EXTRAS_VPK_PATH", vpkPath, 1)
            setenv("EXTRAS_VPK_PATH_TOUCH", vpkPathTouch, 1)
            setenv("LANG", Locale.getDefault().toString(), 1)
            setenv("APP_DATA_PATH", context.applicationInfo.dataDir, 1)
            setenv("APP_LIB_PATH", libEnginePath, 1)
            setenv("VALVE_GAME_PATH", path, 1)
            setArgs(fullArgs)
        }
    }
}
