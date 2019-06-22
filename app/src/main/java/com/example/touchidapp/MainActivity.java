package com.example.touchidapp;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
//import java.security.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

//import com.psbank.psbank.View.Dashboard.DashboardOldActivity;
//import com.psbank.psbank.View.OwnAccounts.MyAccountsOldActivity;

public class MainActivity extends AppCompatActivity {

    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private String KEY_NAME = "OverrideAndroid";
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;

    ImageView fingerPrint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fingerPrint = (ImageView)findViewById(R.id.ic_fingerprint);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (TouchIDHelper.isTouchIDAvailable(this)) {
            Toast.makeText(this, "Touch ID is supported in this device", Toast.LENGTH_SHORT).show();
            startAuthentication();
        }
        else {
            Toast.makeText(this, "Touch ID is not supported in this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void startAuthentication(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkFingerPrintSensor()) {
                FingerprintManager.AuthenticationCallback authenticationCallback = new FingerprintManager.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(getBaseContext(), "onAuthenticationSucceeded", Toast.LENGTH_SHORT).show();
                        fingerPrint.setImageResource(R.drawable.ic_touch_id_success_white);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(getBaseContext(), "onAuthenticationFailed", Toast.LENGTH_SHORT).show();
                        fingerPrint.setImageResource(R.drawable.ic_touch_id_error_white);
                        startAuthentication();
                    }
                };

                generateKey();
                Cipher cipher = generateCipher();
                if (cipher != null) {
                    FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                    CancellationSignal cancellationSignal = new CancellationSignal();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, authenticationCallback, null);
                }
            }
        }
    }

    private Boolean checkFingerPrintSensor(){
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);

            try{
                if (fingerprintManager.isHardwareDetected()) {


                }
                if (fingerprintManager.hasEnrolledFingerprints()) {


                }
                if (keyguardManager.isKeyguardSecure()) {


                }
            }
            catch (SecurityException se){
                se.printStackTrace();
                throw (se);
            }
        }

        return true;
    }

    private void generateKey() {
        Cipher cipher = null;

        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        try {
            keyStore.load(null);
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        }
        catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        catch (NoSuchProviderException e){
            e.printStackTrace();
        }
        catch (CertificateException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build();

            try {
                keyGenerator.init(keyGenParameterSpec);

                keyGenerator.generateKey();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
        }
    }

    private Cipher generateCipher(){
        Cipher cipher = null;

        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/" +
                            KeyProperties.BLOCK_MODE_CBC + "/" +
                            KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }


        try {
            keyStore.load(null);
            Key key = keyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cipher;
    }
}
