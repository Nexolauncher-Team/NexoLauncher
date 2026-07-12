package com.nexo.launcher.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.nexo.launcher.context.ContextExecutor;
import com.nexo.launcher.context.LocaleHelper;
import com.nexo.launcher.event.single.LauncherIgnoreNotchEvent;
import com.nexo.launcher.feature.accounts.AccountsManager;
import com.nexo.launcher.feature.customprofilepath.ProfilePathManager;
import com.nexo.launcher.plugins.PluginLoader;
import com.nexo.launcher.renderer.Renderers;
import com.nexo.launcher.setting.AllSettings;
import com.nexo.launcher.utils.StoragePermissionsUtils;

import com.nexo.launcher.MissingStorageActivity;
import com.nexo.launcher.Tools;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.Companion.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.Companion.setLocale(this);
        Tools.setFullscreen(this);
        Tools.updateWindowSize(this);

        checkStoragePermissions();
        //åŠ è½½æ¸²æŸ“å™¨
        Renderers.INSTANCE.init(false);
        //åŠ è½½æ’ä»¶
        PluginLoader.loadAllPlugins(this, false);
        //åˆ·æ–°æ¸¸æˆè·¯å¾„
        ProfilePathManager.INSTANCE.refreshPath();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ContextExecutor.setActivity(this);
        if (!Tools.checkStorageRoot()) {
            startActivity(new Intent(this, MissingStorageActivity.class));
            finish();
        }

        checkStoragePermissions();

        AccountsManager.INSTANCE.reload();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Tools.setFullscreen(this);
        Tools.ignoreNotch(shouldIgnoreNotch(),this);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Tools.getDisplayMetrics(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void event(LauncherIgnoreNotchEvent event) {
        Tools.ignoreNotch(shouldIgnoreNotch(),this);
    }

    /** @return Whether or not the notch should be ignored */
    public boolean shouldIgnoreNotch() {
        return AllSettings.getIgnoreNotchLauncher().getValue();
    }

    private void checkStoragePermissions() {
        //æ£€æŸ¥æ‰€æœ‰æ–‡ä»¶ç®¡ç†æƒé™
        StoragePermissionsUtils.checkPermissions(this);
    }
}

