package com.axway.apigwgcm.triggers;

/**
 * Created by su on 12/6/2014.
 */
abstract public class UnaryOperation<T> implements Operation {

    abstract public void perform(T opA);
}
