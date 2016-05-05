package com.axway.apigwgcm.triggers;

import java.util.List;
import java.util.Set;

/**
 * Created by su on 12/6/2014.
 */
public interface EventTrigger {

    public static final int REQUEST_TRIGGER = 101;
    public static final int RESPONSE_TRIGGER = 102;

    public static final int BASE_TRIGGER = 0;
    public static final int HTTP_HEADER_EXISTS = BASE_TRIGGER + 1;
    public static final int HTTP_HEADER_EQUALS = BASE_TRIGGER + 2;
    public static final int HTTP_HEADER_CONTAINS = BASE_TRIGGER + 3;
    public static final int HTTP_HEADER_STARTS_WITH = BASE_TRIGGER + 4;
    public static final int HTTP_HEADER_ENDS_WITH = BASE_TRIGGER + 5;

    public static final int HTTP_METHOD_EQUALS = BASE_TRIGGER + 51;

    public static final int CONTENT_TYPE_EQUALS = BASE_TRIGGER + 101;
    public static final int CONTENT_TYPE_CONTAINS = BASE_TRIGGER + 102;

    public static final int CONTENT_LENGTH_LESS_THAN = BASE_TRIGGER + 201;
    public static final int CONTENT_LENGTH_GREATER_THAN = BASE_TRIGGER + 202;
    public static final int CONTENT_LENGTH_EQUALS = BASE_TRIGGER + 203;


//    public Class<? extends Operand> getOperandAType();
//    public Class<? extends Operand> getOperandBType();
    public List<Operation> getSupportedOperations();
    public String getTriggerName();

}
