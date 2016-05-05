package com.axway.apigwgcm.triggers;

import java.util.Map;

/**
 * Created by su on 12/18/2014.
 */
public interface TriggerEvaluator {

    public boolean evaluate(final Map<String, Object> msg, final String expression);
}
