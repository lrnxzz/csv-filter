package me.lrnzx.csv;

import java.util.List;
import java.util.Map;

public class CompositeNode implements ExpressionNode {
    private final List<ExpressionNode> children;
    private final CompositeOperator operator;

    public CompositeNode(List<ExpressionNode> children, CompositeOperator operator) {
        this.children = children;
        this.operator = operator;
    }

    @Override
    public boolean evaluate(Map<String, String> row) {
        return new ExpressionNodeEvaluator().visit(this, row);
    }

    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor, Map<String, String> row) {
        return visitor.visit(this, row);
    }

    public CompositeOperator getOperator() {
        return operator;
    }

    public List<ExpressionNode> getChildren() {
        return children;
    }
}