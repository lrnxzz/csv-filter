package me.lrnzx.csv.fluent;

import me.lrnzx.csv.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

/**
 * A fluent API for building and evaluating CSV filters.
 * This class provides a chain-able interface for constructing complex filter conditions
 * to be applied on CSV data represented as Map&lt;String, String&gt;.
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
     * @throws IllegalArgumentException if the field name is null or empty.
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
     * @return true if the filter matches the row, false otherwise.
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
     * @throws IllegalArgumentException if the other filter is null.
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
        CsvFluentFilter combined = combineWith(other, CompositeOperator.AND);
        return combined.optimize();
    }

    /**
     * Combines this filter with another filter using OR operator.
     *
     * @param other Another CsvFluentFilter to combine with.
     * @return A new CsvFluentFilter instance with OR operation.
     */
    public CsvFluentFilter or(final CsvFluentFilter other) {
        CsvFluentFilter combined = combineWith(other, CompositeOperator.OR);
        return combined.optimize();
    }

    /**
     * Negates the current filter condition.
     *
     * @return A new CsvFluentFilter instance with the negated condition.
     */
    public CsvFluentFilter not() {
        CsvFluentFilter negated = new CsvFluentFilter(new NotNode(this.root));
        return negated.optimize();
    }

    /**
     * Optimizes the current filter for improved performance.
     * This method is automatically called at the end of each filter creation or combination operation.
     *
     * @return An optimized version of the current CsvFluentFilter.
     */
    public CsvFluentFilter optimize() {
        QueryOptimizer optimizer = new QueryOptimizer();
        ExpressionNode optimizedRoot = optimizer.optimize(this.root);
        return new CsvFluentFilter(optimizedRoot);
    }

    /**
     * Checks if this filter is equal to another object.
     * Two filters are equal if their root expressions are equal.
     *
     * @param obj The object to compare with.
     * @return true if the filters are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CsvFluentFilter)) return false;
        CsvFluentFilter other = (CsvFluentFilter) obj;
        return Objects.equals(this.root, other.root);
    }

    /**
     * Returns the hash code for this filter.
     *
     * @return The hash code based on the root expression.
     */
    @Override
    public int hashCode() {
        return Objects.hash(root);
    }

    /**
     * Defines conditions for a specific field.
     */
    public static class FieldCondition {
        protected final String field;

        protected FieldCondition(final String field) {
            this.field = field;
        }

        /**
         * Creates a filter checking if the field is equal to the given value.
         *
         * @param value The value to compare against.
         * @return A new CsvFluentFilter with the equality condition.
         */
        public CsvFluentFilter isEqualTo(final String value) {
            return createFilter(ComparisonOperator.EQUALS, value);
        }

        /**
         * Creates a filter checking if the field is not equal to the given value.
         *
         * @param value The value to compare against.
         * @return A new CsvFluentFilter with the inequality condition.
         */
        public CsvFluentFilter isNotEqualTo(final String value) {
            return createFilter(ComparisonOperator.NOT_EQUALS, value);
        }

        /**
         * Creates a filter checking if the field is greater than the given value.
         *
         * @param value The value to compare against.
         * @return A new CsvFluentFilter with the greater than condition.
         */
        public CsvFluentFilter isGreaterThan(final String value) {
            return createFilter(ComparisonOperator.GREATER_THAN, value);
        }

        /**
         * Creates a filter checking if the field is less than the given value.
         *
         * @param value The value to compare against.
         * @return A new CsvFluentFilter with the less than condition.
         */
        public CsvFluentFilter isLessThan(final String value) {
            return createFilter(ComparisonOperator.LESS_THAN, value);
        }

        /**
         * Creates a filter checking if the field is greater than or equal to the given value.
         *
         * @param value The value to compare against.
         * @return A new CsvFluentFilter with the greater than or equal condition.
         */
        public CsvFluentFilter isGreaterThanOrEqualTo(final String value) {
            return createFilter(ComparisonOperator.GREATER_THAN_OR_EQUAL, value);
        }

        /**
         * Creates a filter checking if the field is less than or equal to the given value.
         *
         * @param value The value to compare against.
         * @return A new CsvFluentFilter with the less than or equal condition.
         */
        public CsvFluentFilter isLessThanOrEqualTo(final String value) {
            return createFilter(ComparisonOperator.LESS_THAN_OR_EQUAL, value);
        }

        /**
         * Creates a filter checking if the field contains the given value.
         *
         * @param value The value to check for.
         * @return A new CsvFluentFilter with the contains condition.
         */
        public CsvFluentFilter contains(final String value) {
            return createFilter(ComparisonOperator.CONTAINS, value);
        }

        /**
         * Creates a filter checking if the field starts with the given value.
         *
         * @param value The value to check for.
         * @return A new CsvFluentFilter with the starts with condition.
         */
        public CsvFluentFilter startsWith(final String value) {
            return createFilter(ComparisonOperator.STARTS_WITH, value);
        }

        /**
         * Creates a filter checking if the field ends with the given value.
         *
         * @param value The value to check for.
         * @return A new CsvFluentFilter with the ends with condition.
         */
        public CsvFluentFilter endsWith(final String value) {
            return createFilter(ComparisonOperator.ENDS_WITH, value);
        }

        /**
         * Creates a filter checking if the field matches the given regular expression.
         *
         * @param regex The regular expression to match against.
         * @return A new CsvFluentFilter with the regex match condition.
         */
        public CsvFluentFilter matches(final String regex) {
            return createFilter(ComparisonOperator.MATCHES, regex);
        }

        /**
         * Creates a filter checking if the field is between two values (inclusive).
         *
         * @param lowerBound The lower bound of the range.
         * @param upperBound The upper bound of the range.
         * @return A new CsvFluentFilter with the between condition.
         */
        public CsvFluentFilter between(final String lowerBound, final String upperBound) {
            return new CsvFluentFilter(new BetweenNode(field, lowerBound, upperBound)).optimize();
        }

        /**
         * Creates a filter checking if the field's value is in the given list.
         *
         * @param values The list of values to check against.
         * @return A new CsvFluentFilter with the in-list condition.
         */
        public CsvFluentFilter in(final List<String> values) {
            return new CsvFluentFilter(new InListNode(field, values)).optimize();
        }

        /**
         * Creates a filter checking if the field is null.
         *
         * @return A new CsvFluentFilter with the is-null condition.
         */
        public CsvFluentFilter isNull() {
            return createFilter(ComparisonOperator.IS_NULL, null);
        }

        /**
         * Creates a filter checking if the field is not null.
         *
         * @return A new CsvFluentFilter with the is-not-null condition.
         */
        public CsvFluentFilter isNotNull() {
            return createFilter(ComparisonOperator.IS_NOT_NULL, null);
        }

        /**
         * Converts the field condition to a date field condition.
         *
         * @param format The date format to use for parsing.
         * @return A new DateFieldCondition for date-specific comparisons.
         */
        public DateFieldCondition asDate(String format) {
            return new DateFieldCondition(field, format);
        }

        protected CsvFluentFilter createFilter(ComparisonOperator operator, String value) {
            if (value == null &&
                    operator != ComparisonOperator.IS_NULL &&
                    operator != ComparisonOperator.IS_NOT_NULL) {
                throw new IllegalArgumentException("Value cannot be null for operator: " + operator);
            }
            return new CsvFluentFilter(new ComparisonNode(field, operator, value)).optimize();
        }
    }

    /**
     * Specialized field condition for date comparisons.
     */
    public static class DateFieldCondition extends FieldCondition {
        private final DateTimeFormatter formatter;

        public DateFieldCondition(String field, String format) {
            super(field);
            this.formatter = DateTimeFormatter.ofPattern(format);
        }

        /**
         * Creates a filter checking if the date field is after the given date.
         *
         * @param date The date to compare against.
         * @return A new CsvFluentFilter with the after date condition.
         */
        public CsvFluentFilter isAfter(LocalDate date) {
            try {
                return createFilter(ComparisonOperator.GREATER_THAN, date.format(formatter));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format", e);
            }
        }

        /**
         * Creates a filter checking if the date field is before the given date.
         *
         * @param date The date to compare against.
         * @return A new CsvFluentFilter with the before date condition.
         */
        public CsvFluentFilter isBefore(LocalDate date) {
            try {
                return createFilter(ComparisonOperator.LESS_THAN, date.format(formatter));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format", e);
            }
        }

        /**
         * Creates a filter checking if the date field is on or after the given date.
         *
         * @param date The date to compare against.
         * @return A new CsvFluentFilter with the on or after date condition.
         */
        public CsvFluentFilter isOnOrAfter(LocalDate date) {
            try {
                return createFilter(ComparisonOperator.GREATER_THAN_OR_EQUAL, date.format(formatter));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format", e);
            }
        }

        /**
         * Creates a filter checking if the date field is on or before the given date.
         *
         * @param date The date to compare against.
         * @return A new CsvFluentFilter with the on or before date condition.
         */
        public CsvFluentFilter isOnOrBefore(LocalDate date) {
            try {
                return createFilter(ComparisonOperator.LESS_THAN_OR_EQUAL, date.format(formatter));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format", e);
            }
        }

        /**
         * Creates a filter checking if the date field is on the given date.
         *
         * @param date The date to compare against.
         * @return A new CsvFluentFilter with the on date condition.
         */
        public CsvFluentFilter isOn(LocalDate date) {
            try {
                return createFilter(ComparisonOperator.EQUALS, date.format(formatter));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format", e);
            }
        }

        /**
         * Creates a filter checking if the date field is between two dates (inclusive).
         *
         * @param start The start date of the range.
         * @param end The end date of the range.
         * @return A new CsvFluentFilter with the between dates condition.
         */
        public CsvFluentFilter isBetween(LocalDate start, LocalDate end) {
            try {
                String startStr = start.format(formatter);
                String endStr = end.format(formatter);
                return new CsvFluentFilter(new BetweenNode(field, startStr, endStr)).optimize();
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format", e);
            }
        }
    }

    /**
     * Creates a case-insensitive field condition.
     *
     * @param field The name of the field.
     * @return A new FieldCondition that performs case-insensitive comparisons.
     */
    public static FieldCondition whereIgnoreCase(final String field) {
        return new CaseInsensitiveFieldCondition(field);
    }

    /**
     * Specialized field condition for case-insensitive comparisons.
     */
    public static class CaseInsensitiveFieldCondition extends FieldCondition {
        public CaseInsensitiveFieldCondition(String field) {
            super(field);
        }

        @Override
        protected CsvFluentFilter createFilter(ComparisonOperator operator, String value) {
            return new CsvFluentFilter(new CaseInsensitiveComparisonNode(field, operator, value)).optimize();
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

        /**
         * Applies the filter condition with the given value.
         *
         * @param value The value to use in the filter condition.
         * @return A new CsvFluentFilter with the applied condition.
         */
        public CsvFluentFilter withValue(String value) {
            return filterFunction.apply(value);
        }
    }

    /**
     * Creates a filter builder for equality comparisons.
     *
     * @param fieldName The name of the field to compare.
     * @return A new FilterBuilder for equality comparisons.
     */
    public static FilterBuilder fieldEquals(String fieldName) {
        return new FilterBuilder(value -> where(fieldName).isEqualTo(value));
    }

    /**
     * Creates a filter builder for greater than comparisons.
     *
     * @param fieldName The name of the field to compare.
     * @return A new FilterBuilder for greater than comparisons.
     */
    public static FilterBuilder fieldGreaterThan(String fieldName) {
        return new FilterBuilder(value -> where(fieldName).isGreaterThan(value));
    }

    /**
     * Creates a filter builder for less than comparisons.
     *
     * @param fieldName The name of the field to compare.
     * @return A new FilterBuilder for less than comparisons.
     */
    public static FilterBuilder fieldLessThan(String fieldName) {
        return new FilterBuilder(value -> where(fieldName).isLessThan(value));
    }
}
