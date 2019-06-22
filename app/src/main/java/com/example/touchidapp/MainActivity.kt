package com.example.touchidapp

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.Toast

import java.io.IOException
//import java.security.*;
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.Key
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var keyStore: KeyStore? = null
    private var keyGenerator: KeyGenerator? = null
    private val KEY_NAME = "OverrideAndroid"
    private var fingerprintManager: FingerprintManager? = null
    private var keyguardManager: KeyguardManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        startAuthentication()
    }

    private fun startAuthentication() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkFingerPrintSensor()) {

                var authenticationCallback = object : FingerprintManager.AuthenticationCallback() {

                    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Toast.makeText(this@MainActivity,"onAuthenticationSucceeded()",Toast.LENGTH_SHORT)
                        ic_fingerprint.setImageResource(R.drawable.ic_touch_id_success_white)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(this@MainActivity,"onAuthenticationFailed()",Toast.LENGTH_SHORT)
                        ic_fingerprint.setImageResource(R.drawable.ic_touch_id_error_white)
                        startAuthentication()
                    }
                }

                generateKey()
                val cipher = generateCipher()
                if (cipher != null) {

                    val cryptoObject = FingerprintManager.CryptoObject(cipher)
                    val cancellationSignal = CancellationSignal()
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                        return
                    }
                    fingerprintManager!!.authenticate(cryptoObject, cancellationSignal, 0, authenticationCallback, null)
                }
            }


        }
    }

    private fun checkFingerPrintSensor(): Boolean {


        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager

            try {

                if (!fingerprintManager!!.isHardwareDetected) {


                }
                if (!fingerprintManager!!.hasEnrolledFingerprints()) {


                }
                if (!keyguardManager.isKeyguardSecure) {


                }

            } catch (se: SecurityException) {
                se.printStackTrace()
                throw (se)
            }
        }

        return true

    }

    private fun generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
        } catch (e: KeyStoreException) {
            e.printStackTrace()
            throw (e)
        }

        try {
            keyStore!!.load(null)
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            throw (e)
        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
            throw (e)
        } catch (e: CertificateException) {
            e.printStackTrace()
            throw (e)
        } catch (e: IOException) {
            e.printStackTrace()
            throw (e)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build()

            try {
                keyGenerator!!.init(keyGenParameterSpec)

                keyGenerator!!.generateKey()
            } catch (e: InvalidAlgorithmParameterException) {
                e.printStackTrace()
            }
        }

    }

    private fun generateCipher(): Cipher? {
        var cipher: Cipher? = null

        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/" +
                            KeyProperties.BLOCK_MODE_CBC + "/" +
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        }

        try {
            keyStore!!.load(null)
            val key = keyStore!!.getKey(KEY_NAME, null)
            cipher!!.init(Cipher.ENCRYPT_MODE, key)
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: UnrecoverableKeyException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return cipher
    }
}
