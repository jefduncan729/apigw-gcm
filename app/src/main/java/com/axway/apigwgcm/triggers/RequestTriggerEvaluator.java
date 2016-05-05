package com.axway.apigwgcm.triggers;

import com.vordel.common.Dictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by su on 12/18/2014.
 */
public class RequestTriggerEvaluator implements TriggerEvaluator {

    private static final String TAG = RequestTriggerEvaluator.class.getSimpleName();

    private Dictionary msgHdrs;

    private String[] tokenize(final String expr) {
        List<String> tokens = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        String e = expr.trim();
        for (int i = 0; i < e.length(); i++) {
            char c = e.charAt(i);
            if (c == ' ') {
                tokens.add(sb.toString());
                if (tokens.size() == 2) {
                    tokens.add(e.substring(i+1));
                    sb = null;
                    break;
                }
                else
                    sb = new StringBuilder();
            }
            else {
                sb.append(c);
            }
        }
        if (sb != null && sb.length() > 0)
            tokens.add(sb.toString());
        if (tokens.size() < 2)
            return null;
        String[] rv = new String[tokens.size()];
        for (int i = 0; i < tokens.size(); i++)
            rv[i] = tokens.get(i);
        return rv;
    }

    @Override
    public boolean evaluate(final Map<String, Object> msg, final String expression) {
        if (expression == null || expression.length() == 0)
            return false;
        String[] tokens = tokenize(expression);
        if (tokens == null || tokens.length < 2)
            return false;
        String opA = null;
        String opB = null;
        String operator = null;
        if (tokens.length == 2) {
            opA = tokens[0];
            operator = tokens[1];
            return evaluateUnary(msg, operator, opA);
        }
        else if (tokens.length == 3) {
            opA = tokens[0];
            operator = tokens[1];
            opB = tokens[2];
            return evaluateBinary(msg, operator, opA, opB);
        }
        return false;
    }

    private Dictionary getMsgHdrs(final Map<String, Object> msg) {
        if (msgHdrs == null) {
            if (msg == null)
                return null;
            msgHdrs = (Dictionary)msg.get("http.headers");
        }
        return msgHdrs;
    }

    private boolean isHeaderArg(final String arg) {
        if (arg == null || arg.length() == 0)
            return false;
        return arg.startsWith("header.");
    }

    private Object getHeaderVal(final Dictionary hdrs, final String opA) {
        if (hdrs == null)
            return null;
        String key = opA.substring(7);
        return hdrs.get(key);
    }

    private boolean evaluateUnary(final Map<String, Object> msg, final String operator, final String operand) {
        String opA = null;
        if (Operation.OP_EXISTS.equals(operator)) {
            if (isHeaderArg(operand)) {
                opA = (String) getHeaderVal((Dictionary) msg.get("http.headers"), operand);
            }
            else {
                opA = operand;
            }
            return (opA != null);
        }
        return false;
    }

    private String stripQuotes(final String input) {
        if (input == null || input.length() == 0)
            return input;
        String rv = input;
        if ((rv.startsWith("'") && rv.endsWith("'")) || (rv.startsWith("\"") && rv.endsWith("\""))) {
            rv = rv.substring(1, rv.length()-1);
        }
        return rv;
    }

    private boolean evaluateBinary(final Map<String, Object> msg, final String operator, final String operandA, final String operandB) {
        String opA = (isHeaderArg(operandA) ? (String) getHeaderVal(getMsgHdrs(msg), operandA) : operandA);
        String opB = (isHeaderArg(operandB) ? (String) getHeaderVal(getMsgHdrs(msg), operandB) : operandB);
        opA = stripQuotes(opA);
        opB = stripQuotes(opB);
        if (opA == null)
            return false;
//        Log.v(TAG, "evaluating " + opA + " " + operator + " " + opB);
        if (Operation.OP_EQUALS.equals(operator)) {
            return opA.equals(opB);
        }
        if (Operation.OP_CONTAINS.equals(operator)) {
            return opA.contains(opB);
        }
        if (Operation.OP_STARTS_WITH.equals(operator)) {
            return opA.startsWith(opB);
        }
        if (Operation.OP_ENDS_WITH.equals(operator)) {
            return opA.endsWith(opB);
        }
        return false;
    }

    public static void test(String[] args) {
/*
        RequestTriggerEvaluator me = new RequestTriggerEvaluator();
        Log.d(TAG, "starting test harness");
        Map<String, Object> msg = new HashMap<String, Object>();
        Dictionary hdrs = new Dictionary() {
            @Override
            public Object get(String s) {
                if ("X-GCM".equals(s))
                    return "true";
                if ("Content-Type".equals(s))
                    return "application/json";
                return null;
            }
        };
        msg.put("http.headers", hdrs);
        String expr = "header.X-GCM exists";
        Log.d(TAG, expr + ": " + me.evaluate(msg, expr));

        expr = "header.X-GCM equals 'true'";
        Log.d(TAG, expr + ": " + me.evaluate(msg, expr));

        expr = "header.Content-Type equals 'text/plain'";
        Log.d(TAG, expr + ": " + me.evaluate(msg, expr));

        expr = "header.Content-Type equals 'application/json'";
        Log.d(TAG, expr + ": " + me.evaluate(msg, expr));


        expr = "header.X-GCM equals \"true\"";
        Log.d(TAG, expr + ": " + me.evaluate(msg, expr));

        expr = "header.Content-Type equals \"text/plain\"";
        Log.d(TAG, expr + ": " + me.evaluate(msg, expr));

        expr = "header.Content-Type equals \"application/json\"";
        Log.d(TAG, expr + ": " + me.evaluate(msg, expr));

        expr = "header.Content-Type contains \"json\"";
        Log.d(TAG, expr + ": " + me.evaluate(msg, expr));

        expr = "header.Content-Type contains \"nosj\"";
        Log.d(TAG, expr + ": " + me.evaluate(msg, expr));

        Log.d(TAG, "testing complete");
*/
    }
}
