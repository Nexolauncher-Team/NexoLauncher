package com.nexo.launcher;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.nexo.launcher.utils.ZHTools.getVersionCode;
import static com.nexo.launcher.utils.ZHTools.getVersionName;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.google.firebase.FirebaseApp;
import com.nexo.launcher.InfoDistributor;
import com.nexo.launcher.context.ContextExecutor;
import com.nexo.launcher.context.LocaleHelper;
import com.nexo.launcher.firebase.FirebaseBackupManager;
import com.nexo.launcher.feature.log.Logging;
import com.nexo.launcher.setting.AllSettings;
import com.nexo.launcher.ui.activity.ErrorActivity;
import com.nexo.launcher.utils.path.PathManager;
import com.nexo.launcher.utils.ZHTools;

import com.nexo.launcher.utils.FileUtils;

import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class PojavApplication extends Application {
	public static final String CRASH_REPORT_TAG = "NexoCrashReport";

	@Override
	public void onCreate() {
		PathManager.initContextConstants(this);
		ContextExecutor.setApplication(this);

		Thread.setDefaultUncaughtExceptionHandler((thread, th) -> {
			boolean storagePermAllowed = (Build.VERSION.SDK_INT >= 29 || ActivityCompat.checkSelfPermission(PojavApplication.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && Tools.checkStorageRoot();
			File crashFile = new File(storagePermAllowed ? PathManager.DIR_LAUNCHER_LOG : PathManager.DIR_DATA, "latestcrash.txt");
			try {
				// Write to file, since some devices may not able to show error
				FileUtils.ensureParentDirectory(crashFile);
				PrintStream crashStream = new PrintStream(crashFile);
				crashStream.append(InfoDistributor.APP_NAME + " crash report\n");
				crashStream.append(" - Time: ").append(DateFormat.getDateTimeInstance().format(new Date())).append("\n");
				crashStream.append(" - Device: ").append(Build.PRODUCT).append(" ").append(Build.MODEL).append("\n");
				crashStream.append(" - Android version: ").append(Build.VERSION.RELEASE).append("\n");
				crashStream.append(" - Launcher version: ").append(getVersionName()).append(" (").append(String.valueOf(getVersionCode())).append(")").append("\n");
				crashStream.append(" - Crash stack trace:\n");
				crashStream.append(Log.getStackTraceString(th));
				crashStream.close();
			} catch (Throwable throwable) {
				Logging.e(CRASH_REPORT_TAG, " - Exception attempt saving crash stack trace:", throwable);
				Logging.e(CRASH_REPORT_TAG, " - The crash stack trace was:", th);
			}

			ErrorActivity.showLauncherCrash(PojavApplication.this, crashFile.getAbsolutePath(), th);
			ZHTools.killProcess();
		});
		
		try {
			super.onCreate();
			File filesDir = getDir("files", MODE_PRIVATE);
			String dataPath = filesDir != null ? filesDir.getParent() : getFilesDir().getParent();
			PathManager.DIR_DATA = dataPath != null ? dataPath : getFilesDir().getAbsolutePath();
			PathManager.DIR_CACHE = getCacheDir();
			PathManager.DIR_ACCOUNT_NEW = PathManager.DIR_DATA + "/accounts";
			Tools.DEVICE_ARCHITECTURE = Architecture.getDeviceArchitecture();
			//Force x86 lib directory for Asus x86 based zenfones
			if(Architecture.isx86Device() && Architecture.is32BitsDevice()){
				String originalJNIDirectory = getApplicationInfo().nativeLibraryDir;
				getApplicationInfo().nativeLibraryDir = originalJNIDirectory.substring(0,
												originalJNIDirectory.lastIndexOf("/"))
												.concat("/x86");
			}
		} catch (Throwable throwable) {
			Intent ferrorIntent = new Intent(this, ErrorActivity.class);
			ferrorIntent.putExtra("throwable", throwable);
			ferrorIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
			startActivity(ferrorIntent);
		}

		//è®¾ç½®ä¸»é¢˜
		String launcherTheme = AllSettings.getLauncherTheme().getValue();
		if (!Objects.equals(launcherTheme, "system")) {
			switch (launcherTheme) {
				case "light" :
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
					break;
				case "dark" :
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
					break;
			}
		}

		if (isMainProcess(this)) {
			try {
				Logging.i(CRASH_REPORT_TAG, "Initializing Firebase in launcher process...");
				FirebaseApp.initializeApp(this);
				FirebaseBackupManager.Companion.init(this);
			} catch (Exception e) {
				Logging.e(CRASH_REPORT_TAG, "Firebase init failed", e);
			}
		} else {
			Logging.i(CRASH_REPORT_TAG, "Skipping Firebase init in non-launcher process: " + getProcessName(this));
		}
	}

	private boolean isMainProcess(Context context) {
		String processName = getProcessName(context);
		if (processName == null) return false;
		return processName.equals(context.getPackageName()) || processName.endsWith(":launcher");
	}

	private String getProcessName(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			return Application.getProcessName();
		}
		int pid = android.os.Process.myPid();
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
		if (infos != null) {
			for (ActivityManager.RunningAppProcessInfo processInfo : infos) {
				if (processInfo.pid == pid) {
					return processInfo.processName;
				}
			}
		}
		return null;
	}

	@Override
	protected void attachBaseContext(Context base) {
		ContextExecutor.setApplication(this);
		PathManager.initContextConstants(base);
		super.attachBaseContext(LocaleHelper.Companion.setLocale(base));
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		ContextExecutor.clearApplication();
	}

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
		ContextExecutor.setApplication(this);
		LocaleHelper.Companion.setLocale(this);
    }
}

