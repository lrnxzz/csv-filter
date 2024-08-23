package me.lrnzx.csv;

import java.util.Map;

/**
 * Represents a leaf node in the expression tree for basic comparisons.
 * This class implements the ExpressionNode interface and is responsible for
 * evaluating simple conditions on a single field of a CSV row.
 */
public class ComparisonNode implements ExpressionNode {
    private final String field;
    private final String value;

    private final ComparisonOperator operator;

    /**
     * Constructs a new ComparisonNode with the specified field, operator, and value.
     *
     * @param field The name of the field in the CSV to compare.
     * @param operator The comparison operator to use.
     * @param value The value to compare against.
     */
    public ComparisonNode(final String field,
                          final ComparisonOperator operator,
                          final String value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    /**
     * Evaluates this comparison node against a given CSV row.
     * This method uses the ExpressionNodeEvaluator to perform the actual evaluation.
     *
     * @param row A map representing a single row of the CSV, where keys are column names and values are cell values.
     * @return true if the comparison evaluates to true, false otherwise.
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
                        final Map<String, String> row) {
        return visitor.visit(this, row);
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    public ComparisonOperator getOperator() {
        return operator;
    }
}
