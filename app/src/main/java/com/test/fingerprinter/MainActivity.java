package com.test.fingerprinter;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {

    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private static final String KEY_NAME = "example_key";
    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;
    private TextView tv_certificate ;
    private Animation animTransDown;
    private ImageButton crntPosBtn;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_certificate = (TextView) findViewById(R.id.certificate);
        animTransDown = AnimationUtils.loadAnimation(
                this, R.anim.translate_down);
        crntPosBtn = (ImageButton) findViewById(R.id.currentPosition);


        keyguardManager =
                (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        fingerprintManager =
                (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
//        Toast.makeText(this,
//                "onCreate",
//                Toast.LENGTH_LONG).show();

        tv_certificate.setAnimation(animTransDown);

        if (!keyguardManager.isKeyguardSecure()) {

            Toast.makeText(this,
                    "Lock screen security not enabled in Settings",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,
                    "Fingerprint authentication permission not enabled",
                    Toast.LENGTH_LONG).show();

            return;
        }

        if (!fingerprintManager.hasEnrolledFingerprints()) {

            // This happens when no fingerprints are registered.
            Toast.makeText(this,
                    "Register at least one fingerprint in Settings",
                    Toast.LENGTH_LONG).show();
            return;
        }

        generateKey();

        //암호 초기화
        if (cipherInit()) {
            cryptoObject =
                    new FingerprintManager.CryptoObject(cipher);
            FingerPrintHandler helper = new FingerPrintHandler(this);
            helper.startAuth(fingerprintManager, cryptoObject);
        }

        crntPosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Gmap.class);
                startActivity(intent);

            }
        });


    } // end of onCreate

    @TargetApi(Build.VERSION_CODES.M)
    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore");
        } catch (NoSuchAlgorithmException |
                NoSuchProviderException e) {
            throw new RuntimeException(
                    "Failed to get KeyGenerator instance", e);
        }

        try {
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public class FingerPrintHandler extends FingerprintManager.AuthenticationCallback{
        private CancellationSignal cancellationSignal;
        private Context appContext;

        public FingerPrintHandler(Context context) {
            appContext = context;
        }

        public void startAuth(FingerprintManager manager,
                              FingerprintManager.CryptoObject cryptoObject) {

            cancellationSignal = new CancellationSignal();

            if (ActivityCompat.checkSelfPermission(appContext,
                    Manifest.permission.USE_FINGERPRINT) !=
                    PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        }

        @Override
        public void onAuthenticationError(int errMsgId,
                                          CharSequence errString) {
//            Toast.makeText(appContext,
//                    "Authentication error\n" + errString,
//                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAuthenticationHelp(int helpMsgId,
                                         CharSequence helpString) {
            Toast.makeText(appContext,
                    "Authentication help\n" + helpString,
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAuthenticationFailed() {

            //지문인식 실패
            Toast.makeText(appContext,
                    "본인인증 실패. \n다시 지문인식해주세요",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAuthenticationSucceeded(
                FingerprintManager.AuthenticationResult result) {

            // 지문인식 성공 토스트
            Toast.makeText(appContext,
                    "본인인증 성공. \n ",
                    Toast.LENGTH_LONG).show();

            //지문인식 성공시 화면 전환
            ComponentName componentName = new ComponentName("com.lego.mindstorms.robotcommander",
                    "com.lego.lms.ev3.retail.LaunchActivity");
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(componentName);
            startActivity(intent);

    }

    }

}
