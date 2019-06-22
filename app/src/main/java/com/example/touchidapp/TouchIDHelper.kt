package com.example.touchidapp

import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build

class TouchIDHelper {

    companion object {

        fun IsTouchIdAvailable(context: Context): Boolean {
            var result = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //Fingerprint API only available on from Android 6.0 (M)
                val fingerprintManager = context.getSystemService(Context.FINGERPRINT_SERVICE) as? FingerprintManager
                result = if (fingerprintManager?.isHardwareDetected != true) {
                    //Device doesn't support fingerprint authentication
                    false
                } else fingerprintManager?.hasEnrolledFingerprints()
            }
            return result
        }
    }
}