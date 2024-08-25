package me.lrnzx.csv.fluent;

import me.lrnzx.csv.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A fluent API for building CSV filters with chaining methods.
 */
public class CsvFluentFilter {
    private final ExpressionNode root;

    private CsvFluentFilter(ExpressionNode root) {
        this.root = root;
    }

    /**
     * Starts building a filter for a specified field.
     *
     * @param field The name of the field to filter.
     * @return A new FieldCondition instance for defining filter conditions.
     */
    public static FieldCondition where(final String field) {
        if (field == null || field.trim().isEmpty()) {
            throw new IllegalArgumentException("Field name cannot be null or empty");
        }
        return new FieldCondition(field);
    }

    /**
     * Evaluates the filter against a given CSV row.
     *
     * @param row A map representing a CSV row.
     * @return True if the filter matches the row, false otherwise.
     */
    public boolean evaluate(final Map<String, String> row) {
        return root.evaluate(row);
    }

    /**
     * Combines this filter with another filter using the specified logical operator.
     *
     * @param other Another CsvFluentFilter to combine with.
     * @param operator The logical operator to use (AND/OR).
     * @return A new CsvFluentFilter instance with the combined filters.
     */
    private CsvFluentFilter combineWith(final CsvFluentFilter other, final CompositeOperator operator) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot combine with null filter");
        }
        List<ExpressionNode> children = new ArrayList<>();
        children.add(this.root);
        children.add(other.root);
        return new CsvFluentFilter(new CompositeNode(children, operator));
    }

    /**
     * Combines this filter with another filter using AND operator.
     *
     * @param other Another CsvFluentFilter to combine with.
     * @return A new CsvFluentFilter instance with AND operation.
     */
    public CsvFluentFilter and(final CsvFluentFilter other) {
        return combineWith(other, CompositeOperator.AND);
    }

    /**
     * Combines this filter with another filter using OR operator.
     *
     * @param other Another CsvFluentFilter to combine with.
     * @return A new CsvFluentFilter instance with OR operation.
     */
    public CsvFluentFilter or(final CsvFluentFilter other) {
        return combineWith(other, CompositeOperator.OR);
    }

    /**
     * Defines conditions for a specific field.
     */
    public static class FieldCondition {
        private final String field;

        private FieldCondition(final String field) {
            this.field = field;
        }

        public CsvFluentFilter isEqualTo(final String value) {
            return createFilter(ComparisonOperator.EQUALS, value);
        }

        public CsvFluentFilter isNotEqualTo(final String value) {
            return createFilter(ComparisonOperator.NOT_EQUALS, value);
        }

        public CsvFluentFilter isGreaterThan(final String value) {
            return createFilter(ComparisonOperator.GREATER_THAN, value);
        }

        public CsvFluentFilter isLessThan(final String value) {
            return createFilter(ComparisonOperator.LESS_THAN, value);
        }

        public CsvFluentFilter isGreaterThanOrEqualTo(final String value) {
            return createFilter(ComparisonOperator.GREATER_THAN_OR_EQUAL, value);
        }

        public CsvFluentFilter isLessThanOrEqualTo(final String value) {
            return createFilter(ComparisonOperator.LESS_THAN_OR_EQUAL, value);
        }

        public CsvFluentFilter contains(final String value) {
            return createFilter(ComparisonOperator.CONTAINS, value);
        }

        public CsvFluentFilter startsWith(final String value) {
            return createFilter(ComparisonOperator.STARTS_WITH, value);
        }

        public CsvFluentFilter endsWith(final String value) {
            return createFilter(ComparisonOperator.ENDS_WITH, value);
        }

        private CsvFluentFilter createFilter(ComparisonOperator operator, String value) {
            if (value == null) {
                throw new IllegalArgumentException("Value cannot be null");
            }
            return new CsvFluentFilter(new ComparisonNode(field, operator, value));
        }
    }

    /**
     * Builder class for creating filters with specific conditions.
     */
    public static class FilterBuilder {
        private final Function<String, CsvFluentFilter> filterFunction;

        private FilterBuilder(Function<String, CsvFluentFilter> filterFunction) {
            this.filterFunction = filterFunction;
        }

        public CsvFluentFilter withValue(String value) {
            return filterFunction.apply(value);
        }
    }

    /**
     * Factory method for creating a filter builder for field equality.
     *
     * @param fieldName The field name for the filter.
     * @return A new FilterBuilder instance.
     */
    public static FilterBuilder fieldEquals(String fieldName) {
        return new FilterBuilder(value -> where(fieldName).isEqualTo(value));
    }

    /**
     * Factory method for creating a filter builder for greater than condition.
     *
     * @param fieldName The field name for the filter.
     * @return A new FilterBuilder instance.
     */
    public static FilterBuilder fieldGreaterThan(String fieldName) {
        return new FilterBuilder(value -> where(fieldName).isGreaterThan(value));
    }

    /**
     * Factory method for creating a filter builder for less than condition.
     *
     * @param fieldName The field name for the filter.
     * @return A new FilterBuilder instance.
     */
    public static FilterBuilder fieldLessThan(String fieldName) {
        return new FilterBuilder(value -> where(fieldName).isLessThan(value));
    }
}
