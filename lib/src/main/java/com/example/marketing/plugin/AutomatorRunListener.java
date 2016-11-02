package com.example.marketing.plugin;

import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.test.internal.runner.listener.InstrumentationRunListener;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

import static android.support.test.InstrumentationRegistry.getContext;

public class AutomatorRunListener extends InstrumentationRunListener {

    @SuppressWarnings("deprecation")
    private KeyguardManager.KeyguardLock mLock;

    @Override
    public void testRunStarted(Description description) throws Exception {
        KeyguardManager km = (KeyguardManager) getContext().getSystemService(Context.KEYGUARD_SERVICE);
        //noinspection deprecation
        mLock = km.newKeyguardLock("cts");
        mLock.disableKeyguard();
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        // Save trace log
        File log = new File(getContext().getExternalFilesDir(null), "fail.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(log));
        bw.write(failure.getTrace());
        bw.flush();
        bw.close();

        // Capture screen snapshot
        File snapshot = new File(getContext().getExternalFilesDir(null), "fail.jpg");
        FileOutputStream out = new FileOutputStream(snapshot);
        Bitmap bitmap = getInstrumentation().getUiAutomation().takeScreenshot();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 5, out);
        bitmap.recycle();
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        mLock.reenableKeyguard();
    }
}
