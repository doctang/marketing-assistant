package com.example.marketing.assistant;

import android.app.ActivityManagerNative;
import android.app.IInstrumentationWatcher;
import android.app.UiAutomationConnection;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.InstrumentationInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.marketing.common.Authentication;
import com.example.marketing.plugin.PluginIntent;

public class MainActivity extends AppCompatActivity {

    private Authentication mAuthentication;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAuthentication = Authentication.Stub.asInterface(service);
            boolean allowed = false;
            try {
                allowed = mAuthentication.checkPermission();
            } catch (RemoteException e) {
                // Nothing to do
            }
            if (!allowed) {
                Toast.makeText(MainActivity.this,
                        "This is device is not authenticated.", Toast.LENGTH_LONG).show();
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAuthentication = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
            InstrumentationListFragment list = new InstrumentationListFragment();
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, list).commit();
        }

        Intent intent = new Intent(this, AuthenticationService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(mServiceConnection);
    }

    public static class InstrumentationListFragment extends ListFragment {

        private static final String TAG = "Instrumentation";

        private InstrumentationInfo mInst;

        private IInstrumentationWatcher mWatcher = new IInstrumentationWatcher.Stub() {

            @Override
            public void instrumentationStatus(
                    ComponentName componentName, int i, Bundle bundle) throws RemoteException {
                if (bundle != null) {
                    for (String key : bundle.keySet()) {
                        Log.i(TAG, "INSTRUMENTATION_STATUS_RESULT: " + key + "=" + bundle.get(key));
                    }
                }
                Log.i(TAG, "INSTRUMENTATION_STATUS_CODE: " + i);
            }

            @Override
            public void instrumentationFinished(
                    ComponentName componentName, int i, Bundle bundle) throws RemoteException {
                if (bundle != null) {
                    for (String key : bundle.keySet()) {
                        Log.i(TAG, "INSTRUMENTATION_RESULT: " + key + "=" + bundle.get(key));
                    }
                }
                Log.i(TAG, "INSTRUMENTATION_CODE: " + i);
            }
        };

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setListAdapter(new InstrumentationAdapter(getContext(), null));
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            mInst = ((InstrumentationAdapter) getListAdapter()).getItem(position);
            if (mInst != null) {
                Intent intent = new Intent(PluginIntent.ACTION_SETTINGS);
                intent.setPackage(mInst.packageName);
                startActivityForResult(intent, 0);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == 0 && resultCode == RESULT_OK) {
                final String message = getString(
                        R.string.inst_title_start, mInst.loadLabel(getContext().getPackageManager()));
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                ComponentName className = new ComponentName(mInst.packageName, mInst.name);
                try {
                    ActivityManagerNative.getDefault().startInstrumentation(className, null, 0,
                            new Bundle(), mWatcher, new UiAutomationConnection(),
                            UserHandle.myUserId(), null);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
