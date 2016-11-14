package com.example.marketing.plugin;

import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiWatcher;
import android.support.test.uiautomator.Until;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.internal.util.Checks.checkNotNull;
import static com.example.marketing.plugin.AutomatorHelper.WAIT_TIME;

public abstract class Automator implements UiWatcher {

    protected UiDevice mDevice;
    protected AutomatorHelper mHelper;

    @Override
    public boolean checkForCondition() {
        BySelector cancelSelector = By.res("com.android.systemui", "send_button");
        UiObject2 cancel = mDevice.findObject(cancelSelector);
        if (cancel != null) {
            cancel.click();
            return mDevice.wait(Until.gone(cancelSelector), WAIT_TIME);
        }
        return false;
    }

    @Before
    public void setUp() {
        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(getInstrumentation());
        checkNotNull(mDevice);
        mHelper = new AutomatorHelper(mDevice);

        // Register UiWatcher
        mDevice.registerWatcher("auto", this);

        // Start from the home screen
        mDevice.pressHome();
    }

    @Test
    public abstract void run();

    @After
    public void tearDown() {
        mDevice.removeWatcher("auto");
    }
}
