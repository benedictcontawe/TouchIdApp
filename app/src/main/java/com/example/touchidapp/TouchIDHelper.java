package com.example.touchidapp;

import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;

public class TouchIDHelper {

    static Boolean isTouchIDAvailable(Context context){
        boolean result = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //Fingerprint API only available on from Android 6.0 (M)
            FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
            if (fingerprintManager.isHardwareDetected() != true){
                //Device doesn't support fingerprint authentication
                result = false;
            }
            else {
                fingerprintManager.hasEnrolledFingerprints();
                result = true;
            }
        }
        return result;
    }
}
