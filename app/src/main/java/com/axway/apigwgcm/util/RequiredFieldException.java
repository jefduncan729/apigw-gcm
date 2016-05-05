package com.axway.apigwgcm.util;

import java.util.Locale;

/**
 * Created by su on 10/11/2014.
 */
public class RequiredFieldException extends ValidationException {

    public RequiredFieldException(String fldName) {
        super(StringUtil.format("%s is required", fldName));
    }
}
