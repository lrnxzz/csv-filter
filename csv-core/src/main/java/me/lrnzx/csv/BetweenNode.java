package me.lrnzx.csv;

import java.util.Map;

/**
 * Represents a node in the expression tree for between comparisons.
 * This class implements the ExpressionNode interface and is responsible for
 * evaluating whether a field's value lies within a specified range.
 */
public class BetweenNode implements ExpressionNode {
    private final String field;
    private final String lowerBound;
    private final String upperBound;
    private final boolean lowerInclusive;
    private final boolean upperInclusive;

    /**
     * Constructs a new BetweenNode with the specified field, bounds, and inclusivity.
     *
     * @param field           The name of the field in the CSV to compare.
     * @param lowerBound      The lower bound of the range.
     * @param upperBound      The upper bound of the range.
     * @param lowerInclusive  Whether the lower bound is inclusive.
     * @param upperInclusive  Whether the upper bound is inclusive.
     */
    public BetweenNode(String field, String lowerBound, String upperBound,
                       boolean lowerInclusive, boolean upperInclusive) {
        this.field = field;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.lowerInclusive = lowerInclusive;
        this.upperInclusive = upperInclusive;
    }

    /**
     * Evaluates this between node against a given CSV row.
     * It checks whether the field's value lies within the specified range,
     * considering inclusivity of the bounds.
     *
     * @param row A map representing a single row of the CSV, where keys are column names and values are cell values.
     * @return true if the field's value is within the range, false otherwise.
     */
    @Override
    public boolean evaluate(Map<String, String> row) {
        final String valueStr = row.get(field);
        if (valueStr == null) return false;

        // Try to parse values as numbers; fallback to string comparison if parsing fails
        Double value = parseDouble(valueStr);
        Double lower = parseDouble(lowerBound);
        Double upper = parseDouble(upperBound);

        if (value != null && lower != null && upper != null) {
            boolean lowerCheck = lowerInclusive ? value >= lower : value > lower;
            boolean upperCheck = upperInclusive ? value <= upper : value < upper;
            return lowerCheck && upperCheck;
        } else {
            // Fallback to string comparison
            boolean lowerCheck = lowerInclusive ? valueStr.compareTo(lowerBound) >= 0 : valueStr.compareTo(lowerBound) > 0;
            boolean upperCheck = upperInclusive ? valueStr.compareTo(upperBound) <= 0 : valueStr.compareTo(upperBound) < 0;
            return lowerCheck && upperCheck;
        }
    }

    /**
     * Accepts a visitor to this node, implementing the Visitor pattern.
     * This allows for different operations to be performed on the node without changing its structure.
     *
     * @param visitor The visitor that will operate on this node.
     * @param row     The CSV row data for the visitor to use.
     * @param <T>     The return type of the visitor's operation.
     * @return The result of the visitor's operation on this node.
     */
    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor, Map<String, String> row) {
        return visitor.visit(this, row);
    }

    public String getField() {
        return this.field;
    }

    public String getLowerBound() {
        return lowerBound;
    }

    public String getUpperBound() {
        return upperBound;
    }

    public boolean isLowerInclusive() {
        return lowerInclusive;
    }

    public boolean isUpperInclusive() {
        return upperInclusive;
    }

    /**
     * Tries to parse a string into a Double.
     *
     * @param value The string to parse.
     * @return The Double value, or null if parsing fails.
     */
    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
