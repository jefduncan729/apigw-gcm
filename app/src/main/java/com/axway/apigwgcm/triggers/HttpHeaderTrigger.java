package com.axway.apigwgcm.triggers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by su on 12/6/2014.
 */
public class HttpHeaderTrigger {

/*
    @Override
    public Class<? extends Operand> getOperandAType() {
        return StringOperand.class;
    }

    @Override
    public Class<? extends Operand> getOperandBType() {
        return StringOperand.class;
    }
*/
//    @Override
    public List<Operation> getSupportedOperations() {
        List<Operation> rv = new ArrayList<Operation>();
/*
        rv.add(new StringOperation(Operation.OP_EQUALS));
        rv.add(new StringOperation(Operation.OP_NOT_EQUALS));
        rv.add(new UnaryOperation(Operation.OP_EXISTS));
        rv.add(new UnaryOperation(Operation.OP_NOT_EXISTS));
        rv.add(new StringOperation(Operation.OP_CONTAINS));
        rv.add(new StringOperation(Operation.OP_NOT_CONTAINS));
        rv.add(new StringOperation(Operation.OP_STARTS_WITH));
        rv.add(new StringOperation(Operation.OP_ENDS_WITH));
        rv.add(new StringOperation(Operation.OP_NOT_STARTS_WITH));
        rv.add(new StringOperation(Operation.OP_NOT_ENDS_WITH));
*/
        return rv;
    }
}
