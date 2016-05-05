package com.axway.apigwgcm;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.net.ssl.TrustManagerFactory;

/**
 * Created by su on 4/13/2015.
 */
public class KeystoreManager {
    private static final String TAG = KeystoreManager.class.getSimpleName();

    private WeakReference<Context> ctxRef;
    public static final String TRUST_STORE_FNAME = "trust_store.bks";
    public static final String TRUST_STORE_PASS = "Secret1";
    private static KeystoreManager inst = null;


    protected KeystoreManager(Context ctx) {
        super();
        ctxRef = new WeakReference<Context>(ctx);
    }

    public static KeystoreManager from(Context ctx) {
        if (inst == null) {
            inst = new KeystoreManager(ctx);
            Log.d(TAG, "KeystoreManager instantiated");
        }
        return inst;
    }

    public boolean removeKeystore() {
        if (ctxRef == null || ctxRef.get() == null) {
            Log.d(TAG, "context unavailable");
            return false;
        }
        boolean rv = false;
        File f = new File(ctxRef.get().getFilesDir(), TRUST_STORE_FNAME);
        if (f.exists()) {
            rv = f.delete();
            Log.d(TAG, "keystore removed: " + f.getAbsolutePath() + " " + Boolean.toString(rv));
        }
        return rv;
    }

/*
    public boolean addTrustedCert(ServerInfo info, CertPath cp) {
        String a = StringUtil.format("%s_%d", info.getHost(), info.getPort());
        return addTrustedCert(a, cp);
    }

*/
    public boolean addTrustedCert(String alias, CertPath cp)  {
        if (ctxRef == null || ctxRef.get() == null) {
            Log.d(TAG, "context unavailable");
            return false;
        }
        boolean rv = false;
        try {
            final KeyStore trustStore = loadKeystore();    //trustStore = KeyStore.getInstance("BKS");

            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            int i = 0;
            for (Certificate c : cp.getCertificates()) {
                if (c.getType().equals("X.509")) {
                    X509Certificate c509 = (X509Certificate) c;
                    trustStore.setCertificateEntry(alias + Integer.toString(i++), c509);
                }
            }
            saveKeystore(trustStore);
            rv = true;
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        } catch (KeyStoreException e) {
            Log.e(TAG, "KeyStoreException", e);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "NoSuchAlgorithmException", e);
        }catch (CertificateException e) {
            Log.e(TAG, "CertificateException", e);
        }
        return rv;
    }

    public Certificate[] getCertPath(String alias) {
        if (TextUtils.isEmpty(alias))
            return null;
        final KeyStore trustStore = loadKeystore();    //trustStore = KeyStore.getInstance("BKS");
        Certificate[] rv = null;
        boolean haveAlias = false;
        int n = 0;
        try {
            for (int i = 0; i < 9; i++) {
                String a = alias + Integer.toString(i);
                if (trustStore.containsAlias(a))
                    n++;
            }
            if (n > 0) {
                rv = new Certificate[n];
                for (int i = 0; i < n; i++) {
                    String a = alias + Integer.toString(i);
                    rv[i] = trustStore.getCertificate(a);
                }
            }
        }
        catch (KeyStoreException e) {
            Log.e(TAG, "keystore exception", e);
        }
        return rv;
    }

    public KeyStore loadKeystore() {
        if (ctxRef == null || ctxRef.get() == null) {
            Log.d(TAG, "context unavailable");
            return null;
        }
        File f = new File(ctxRef.get().getFilesDir(), TRUST_STORE_FNAME);
        Log.d(TAG, "loading keystore from " + f.getAbsolutePath());
        InputStream trustStoreLocation = null;
        KeyStore trustStore = null;
        try {
            trustStoreLocation = new FileInputStream(f);
        }
        catch (FileNotFoundException e) {
            Log.w(TAG, "file not found: " + f.getAbsolutePath());
            trustStoreLocation = null;
        }
        try {
            trustStore = KeyStore.getInstance("BKS");
            trustStore.load(trustStoreLocation, TRUST_STORE_PASS.toCharArray());
            Log.d(TAG, "keystore loaded");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "NoSuchAlgorithmException", e);
        } catch (CertificateException e) {
            Log.e(TAG, "CertificateException", e);
        } catch (KeyStoreException e) {
            Log.e(TAG, "KeyStoreException", e);
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }
        return trustStore;
    }

    private void saveKeystore(KeyStore trustStore) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        if (ctxRef == null || ctxRef.get() == null) {
            Log.d(TAG, "context unavailable");
            return;
        }
        File f = new File(ctxRef.get().getFilesDir(), TRUST_STORE_FNAME);
        final OutputStream out = new FileOutputStream(f);
        trustStore.store(out, TRUST_STORE_PASS.toCharArray());
        Log.d(TAG, "keystore saved to " + f.getAbsolutePath());
    }

    public List<Certificate> getTrustedCerts() {
        if (ctxRef == null || ctxRef.get() == null) {
            Log.d(TAG, "context unavailable");
            return null;
        }
        List<Certificate> rv = null;
        File f = new File(ctxRef.get().getFilesDir(), TRUST_STORE_FNAME);
        if (!f.exists())
            return rv;
        InputStream trustStoreLocation = null;
        try {
            trustStoreLocation = new FileInputStream(f);
            final KeyStore trustStore = KeyStore.getInstance("BKS");
            trustStore.load(trustStoreLocation, TRUST_STORE_PASS.toCharArray());
            final Enumeration<String> aliases = trustStore.aliases();
            if (aliases != null) {
                rv = new ArrayList<Certificate>();
                String alias;
                while (aliases.hasMoreElements()) {
                    alias = aliases.nextElement();
                    Certificate c = trustStore.getCertificate(alias);
                    if (c != null)
                        rv.add(c);
                }
            }
        }
        catch (IOException e) {
            Log.d(TAG, e.getLocalizedMessage(), e);
        }
        catch (CertificateException e) {
            Log.d(TAG, e.getLocalizedMessage(), e);
        }
        catch (NoSuchAlgorithmException e) {
            Log.d(TAG, e.getLocalizedMessage(), e);
        }
        catch (KeyStoreException e) {
            Log.d(TAG, e.getLocalizedMessage(), e);
        }
        finally {
            if (trustStoreLocation != null)
                try { trustStoreLocation.close(); } catch (IOException e) {}
        }
        return rv;
    }
}
