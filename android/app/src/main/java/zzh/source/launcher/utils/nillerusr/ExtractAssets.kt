package zzh.source.launcher.utils.nillerusr

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object ExtractAssets {
    const val TAG: String = "ExtractAssets"
    var mPref: SharedPreferences? = null

    const val VPK_NAME: String = "extras_dir.vpk"
    const val VPK_NAMETOUCH: String = "extratouch_dir.vpk"
    const val VPK_NAMEMPTOUCH: String = "extratouch_mp_dir.vpk"

    const val PAK_VERSION: Int = 16

    /* ================= chmod (best-effort) ================= */
    private fun chmod(path: String, mode: Int): Int {
        try {
            val f = File(path)

            val ownerRead = (mode and 256) != 0
            val ownerWrite = (mode and 128) != 0
            val ownerExec = (mode and 64) != 0

            f.setReadable(ownerRead, true)
            f.setWritable(ownerWrite, true)
            f.setExecutable(ownerExec, true)

            try {
                val octal = Integer.toOctalString(mode)
                val p = Runtime.getRuntime().exec(arrayOf<String>("chmod", octal, path))
                p.waitFor()
            } catch (ignored: Throwable) {}

            try {
                val fu = Class.forName("android.os.FileUtils")
                val m =
                    fu.getMethod(
                        "setPermissions",
                        String::class.java,
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                    )
                return (m.invoke(null, path, mode, -1, -1) as kotlin.Int?)!!
            } catch (ignored: Throwable) {}
        } catch (ignored: Throwable) {}
        return -1
    }

    /* ================= Public entry ================= */
    fun extractAssets(context: Context) {
        try {
            val chmod = chmod(context.getApplicationInfo().dataDir, 511)
            chmod(path = context.getFilesDir().getPath(), mode = 511)
        } catch (ignored: Throwable) {}

        extractVPKs(context)

        // fonts（保持原逻辑）
        extractAsset(context, "DroidSansFallback.ttf", false)
        extractAsset(context, "LiberationMono-Regular.ttf", false)
        extractAsset(context, "dejavusans-boldoblique.ttf", false)
        extractAsset(context, "dejavusans-bold.ttf", false)
        extractAsset(context, "dejavusans-oblique.ttf", false)
        extractAsset(context, "dejavusans.ttf", false)
        extractAsset(context, "Itim-Regular.otf", false)
        extractAsset(context, "jf-openhuninn-2.0.ttf", false)
    }

    /* ================= VPK extraction ================= */
    private fun extractVPKs(context: Context) {
        if (mPref == null) {
            mPref = context.getSharedPreferences("mod", 0)
        }

        val savedVer = mPref!!.getInt("pakversion", 0)
        // 核心逻辑：版本号只要变，force 就设为 true
        var force = (savedVer != PAK_VERSION)

        val vpkList: MutableList<String> = ArrayList<String>()
        vpkList.add(VPK_NAME)
        vpkList.add(VPK_NAMETOUCH)
        vpkList.add(VPK_NAMEMPTOUCH)

        // 严谨校验：即便版本号对，但如果文件不存在，也要强制解压
        if (!force) {
            for (name in vpkList) {
                val f = File(context.getFilesDir(), name)
                if (!f.exists() || f.length() == 0L) {
                    force = true
                    Log.d(TAG, "File missing or empty: " + name + ", triggering force extraction.")
                    break
                }
            }
        }

        if (!force) {
            Log.d(TAG, "Version and files are healthy, skipping.")
            return
        }

        Log.d(TAG, "Starting mandatory extraction. Target version: " + PAK_VERSION)
        var allOk = true

        for (name in vpkList) {
            // 传入 force，确保 extractAsset 内部执行删除旧文件并重新写入
            val ok = extractAsset(context, name, true)
            allOk = allOk and ok
        }

        if (allOk) {
            mPref!!.edit(commit = true) { putInt("pakversion", PAK_VERSION) }
        }
    }

    /* ================= Single asset extraction ================= */
    private fun extractAsset(context: Context, name: String, force: Boolean): Boolean {
        val am = context.getAssets()
        val target = File(context.getFilesDir(), name)
        val exists = target.exists()

        if (!force && exists) {
            return true
        }

        val tmp = File(context.getFilesDir(), name + ".tmp")

        Log.d(TAG, "extractAsset: " + name + " force=" + force)

        try {
            am.open(name).use { `in` ->
                FileOutputStream(tmp).use { out ->
                    val buf = ByteArray(8192)
                    var len: Int
                    while ((`in`.read(buf).also { len = it }) > 0) {
                        out.write(buf, 0, len)
                    }
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to read asset: " + name, e)
            tmp.delete()
            return false
        }

        if (exists && !target.delete()) {
            Log.e(TAG, "Failed to delete old file: " + target)
            tmp.delete()
            return false
        }

        if (!tmp.renameTo(target)) {
            try {
                FileInputStream(tmp).use { fis ->
                    FileOutputStream(target).use { fos ->
                        val buf = ByteArray(8192)
                        var len: Int
                        while ((fis.read(buf).also { len = it }) > 0) {
                            fos.write(buf, 0, len)
                        }
                        tmp.delete()
                    }
                }
            } catch (ioe: IOException) {
                Log.e(TAG, "Fallback copy failed: " + name, ioe)
                tmp.delete()
                return false
            }
        }

        chmod(target.getPath(), 511)
        Log.d(TAG, "extractAsset success: " + name)
        return true
    }
}
