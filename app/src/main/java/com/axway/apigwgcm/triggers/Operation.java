package com.axway.apigwgcm.triggers;

/**
 * Created by su on 12/6/2014.
 */
public interface Operation {

    public static final String OP_EXISTS = "exists";
    public static final String OP_NOT_EXISTS = "does not exist";
    public static final String OP_EQUALS = "equals";
    public static final String OP_NOT_EQUALS = "does not equal";
    public static final String OP_STARTS_WITH = "starts with";
    public static final String OP_ENDS_WITH = "ends with";
    public static final String OP_NOT_STARTS_WITH = "does not start with";
    public static final String OP_NOT_ENDS_WITH = "does not end with";
    public static final String OP_CONTAINS = "contains";
    public static final String OP_NOT_CONTAINS = "does not contain";
    public static final String OP_GREATER_THAN = "greater than";
    public static final String OP_LESS_THAN = "less than";
    public static final String OP_GREATER_THAN_OR_EQUAL = OP_GREATER_THAN + " or equal to";
    public static final String OP_LESS_THAN_OR_EQUAL = OP_LESS_THAN + " or equal to";

    public static final String OP_NOT = "!";
    public static final String OP_AND = "&&";
    public static final String OP_OR = "||";
}
