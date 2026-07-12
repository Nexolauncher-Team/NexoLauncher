package com.nexo.launcher.customcontrols.gamepad;

import com.nexo.launcher.GrabListener;

public interface GamepadDataProvider {
    GamepadMap getMenuMap();
    GamepadMap getGameMap();
    boolean isGrabbing();
    void attachGrabListener(GrabListener grabListener);
}

