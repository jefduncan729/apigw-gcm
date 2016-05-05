package com.axway.apigwgcm.api;

import java.security.cert.CertPath;

/**
 * Created by su on 12/11/2014.
 */
public class CertValidationException extends Exception {

    private CertPath certPath;

    protected CertValidationException() {
        super();
        certPath = null;
    }

    public CertValidationException(final CertPath certPath) {
        this();
        this.certPath = certPath;
    }

    public CertPath getCertPath() {
        return certPath;
    }

//    public void setCertPath(CertPath certPath) {
//        this.certPath = certPath;
//    }
}
