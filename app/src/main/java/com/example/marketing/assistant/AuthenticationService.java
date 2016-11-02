package com.example.marketing.assistant;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.TelephonyManager;

import com.example.marketing.common.Authentication;

public class AuthenticationService extends Service {

    private Authentication.Stub mBinder = new Authentication.Stub() {

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
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
