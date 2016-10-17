package com.meditationtimer;

import android.os.PowerManager;

import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactMethod;

public class WakeLockModule extends ReactContextBaseJavaModule {

    private PowerManager.WakeLock mWakeLock = null;

    private String TAG = "native wakelock";
    
    public WakeLockModule (ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "WakeLock";
    }

    @ReactMethod
    public void acquire (long timeout) {
        ReactContext c = getReactApplicationContext();
        PowerManager p = (PowerManager) c.getSystemService(c.POWER_SERVICE);
        mWakeLock = p.newWakeLock(p.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire(timeout);
    }

    @ReactMethod
    public void release () {
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        } 
    }
}
