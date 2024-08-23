package me.lrnzx.csv;

import java.util.List;
import java.util.Map;

/**
 * Represents a composite node in an expression tree.
 * This node combines multiple child nodes using a specified logical operator (AND, OR).
 * It implements the Composite pattern, allowing for the creation of complex, nested conditions.
 */
public class CompositeNode implements ExpressionNode {
    private final List<ExpressionNode> children;
    private final CompositeOperator operator;

    /**
     * Constructs a new CompositeNode with the specified children and operator.
     *
     * @param children A list of child ExpressionNodes to be combined.
     * @param operator The logical operator (AND, OR) used to combine the results of the children.
     */
    public CompositeNode(final List<ExpressionNode> children,
                         final CompositeOperator operator) {
        this.children = children;
        this.operator = operator;
    }

    /**
     * Evaluates this composite node against a given CSV row.
     * This method uses the ExpressionNodeEvaluator to perform the actual evaluation.
     *
     * @param row A map representing a single row of the CSV, where keys are column names and values are cell values.
     * @return true if the composite condition evaluates to true, false otherwise.
     */
    @Override
    public boolean evaluate(final Map<String, String> row) {
        return new ExpressionNodeEvaluator().visit(this, row);
    }

    /**
     * Accepts a visitor to this node, implementing the Visitor pattern.
     * This allows for different operations to be performed on the node without changing its structure.
     *
     * @param visitor The visitor that will operate on this node.
     * @param row The CSV row data for the visitor to use.
     * @return The result of the visitor's operation on this node.
     */
    @Override
    public <T> T accept(final ExpressionNodeVisitor<T> visitor,
                        Map<String, String> row) {
        return visitor.visit(this, row);
    }

    public CompositeOperator getOperator() {
        return operator;
    }

    public List<ExpressionNode> getChildren() {
        return children;
    }
}