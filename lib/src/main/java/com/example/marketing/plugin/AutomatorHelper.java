package com.example.marketing.plugin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.widget.TextView;

import java.io.IOException;

import static android.support.test.InstrumentationRegistry.getContext;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class AutomatorHelper {

    public static final int WAIT_TIME = 5000;

    private UiDevice mDevice;

    public AutomatorHelper(UiDevice device) {
        mDevice = device;
    }

    public void longClick(int x, int y) {
        mDevice.drag(x, y, x, y, 5);
    }

    public void longClick(UiObject obj) {
        Rect r = null;
        try {
            r = obj.getVisibleBounds();
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        if (r != null) {
            longClick(r.centerX(), r.centerY());
        }
    }

    public void longClick(UiObject2 obj) {
        Point p = obj.getVisibleCenter();
        longClick(p.x, p.y);
    }

    public Bitmap getBitmap(Rect r) {
        Bitmap source = getInstrumentation().getUiAutomation().takeScreenshot();
        return Bitmap.createBitmap(source, r.left, r.top, r.width(), r.height());
    }

    public Bitmap getBitmap(UiObject obj) {
        Bitmap target = null;
        try {
            target = getBitmap(obj.getVisibleBounds());
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        return target;
    }

    public Bitmap getBitmap(UiObject2 obj) {
        return getBitmap(obj.getVisibleBounds());
    }

    public void launchApp(String packageName, boolean forceStop) {
        if (forceStop) {
            try {
                mDevice.executeShellCommand("am force-stop " + packageName);
            } catch (IOException e) {
                // Nothing to do
            }
        }

        // Launch the app
        Context context = getContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(packageName).depth(0)), WAIT_TIME);
    }

    @SuppressWarnings("deprecation")
    public void openMenu(String title) {
        UiScrollable list = new UiScrollable(new UiSelector().scrollable(true));
        list.setAsVerticalList();
        UiObject menu = new UiObject(new UiSelector().className(TextView.class).text(title));
        if (menu.exists()) {
            boolean success = false;
            try {
                success = menu.clickAndWaitForNewWindow();
            } catch (UiObjectNotFoundException e) {
                // Nothing to do
            }
            if (!success) {
                try {
                    list.flingForward();
                    menu.clickAndWaitForNewWindow();
                } catch (UiObjectNotFoundException e) {
                    // Nothing to do
                }
            }
        } else {
            try {
                menu = list.getChildByText(new UiSelector().className(TextView.class), title);
            } catch (UiObjectNotFoundException e) {
                // Nothing to do
            }
            assertThat(String.format("没有找到%s菜单", title), menu, notNullValue());
            try {
                if (!menu.clickAndWaitForNewWindow()) {
                    list.scrollToEnd(5);
                    openMenu(title);
                }
            } catch (UiObjectNotFoundException e) {
                // Nothing to do
            }
        }
    }
}
