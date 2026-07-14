package com.nexo.launcher.plugins;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.nexo.launcher.feature.log.Logging;

import java.io.File;

public class FFmpegPlugin {
    public static boolean isAvailable = false;
    public static String libraryPath;
    public static String executablePath;
    public static void discover(Context context) {
        PackageManager manager = context.getPackageManager();
        String[] possiblePackages = {
                "com.nexo.launcher.ffmpeg"
        };

        for (String pkg : possiblePackages) {
            try {
                PackageInfo ffmpegPluginInfo = manager.getPackageInfo(pkg, PackageManager.GET_SHARED_LIBRARY_FILES);
                libraryPath = ffmpegPluginInfo.applicationInfo.nativeLibraryDir;
                File ffmpegExecutable = new File(libraryPath, "libffmpeg.so");
                executablePath = ffmpegExecutable.getAbsolutePath();
                isAvailable = ffmpegExecutable.exists();
                if (isAvailable) {
                    Logging.i("FFmpegPlugin", "Discovered plugin in: " + pkg);
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        Logging.w("FFmpegPlugin", "Failed to discover FFmpeg plugin in any known package.");
    }
}

