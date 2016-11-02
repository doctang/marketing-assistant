package com.example.marketing.plugin;

import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiWatcher;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.internal.util.Checks.checkNotNull;

public abstract class Automator implements UiWatcher {

    protected UiDevice mDevice;
    protected AutomatorHelper mHelper;

    @Override
    public boolean checkForCondition() {
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
