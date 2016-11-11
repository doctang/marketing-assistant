package com.example.marketing.assistant;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.example.marketing.common.CommonInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommonService extends Service {

    private CommonInterface.Stub mBinder = new CommonInterface.Stub() {

        @Override
        public boolean checkPermission() throws RemoteException {
            ApplicationInfo info = null;
            try {
                info = getPackageManager().getApplicationInfo(getPackageName(),
                        PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                // Nothing to do
            }
            if (info != null) {
                String serialNo = info.metaData.getString("SERIAL_NO", "SN_").substring(3);
                TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                String deviceId = tm.getDeviceId();
                return deviceId.equals(serialNo);
            }
            return false;
        }

        @Override
        public List<String> queryPhoneNumber() throws RemoteException {
            List<String> list = new ArrayList<>();
            File file = new File(Environment.getExternalStorageDirectory(), "phonenumber.txt");
            BufferedReader br = null;
            String line;
            try {
                br = new BufferedReader(new FileReader(file));
                while (!TextUtils.isEmpty((line = br.readLine()))) {
                    list.add(line);
                }
            } catch (IOException e) {
                // Nothing to do
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        // Nothing to do
                    }
                }
            }
            return list;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
