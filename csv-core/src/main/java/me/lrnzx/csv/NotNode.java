package me.lrnzx.csv;

import java.util.Map;

public class NotNode implements ExpressionNode {
    private final ExpressionNode childNode;

    public NotNode(ExpressionNode childNode) {
        this.childNode = childNode;
    }

    @Override
    public boolean evaluate(Map<String, String> row) {
        return !childNode.evaluate(row);
    }

    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor, Map<String, String> row) {
        return visitor.visit(this, row);
    }

    public ExpressionNode getChildNode() {
        return childNode;
    }
}