package me.lrnzx.csv;

import java.util.Map;

public class ConstantNode implements ExpressionNode {
    public static final ConstantNode TRUE = new ConstantNode(true);
    public static final ConstantNode FALSE = new ConstantNode(false);

    private final boolean value;

    public ConstantNode(boolean value) {
        this.value = value;
    }

    @Override
    public boolean evaluate(Map<String, String> row) {
        return value;
    }

    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor, Map<String, String> row) {
        return visitor.visit(this, row);
    }

    public boolean getValue() {
        return value;
    }
}