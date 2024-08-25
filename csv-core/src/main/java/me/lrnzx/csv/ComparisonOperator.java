package me.lrnzx.csv;

import java.util.function.BiPredicate;

/**
 * Enum representing different comparison operators for CSV filtering.
 */
public enum ComparisonOperator {
    EQUALS((a, b) -> a.equals(b)),
    NOT_EQUALS((a, b) -> !a.equals(b)),
    GREATER_THAN((a, b) -> a.compareTo(b) > 0),
    LESS_THAN((a, b) -> a.compareTo(b) < 0),
    GREATER_THAN_OR_EQUAL((a, b) -> a.compareTo(b) >= 0),
    LESS_THAN_OR_EQUAL((a, b) -> a.compareTo(b) <= 0),
    CONTAINS((a, b) -> a.contains(b)),
    STARTS_WITH((a, b) -> a.startsWith(b)),
    ENDS_WITH((a, b) -> a.endsWith(b)),
    MATCHES((a, b) -> a.matches(b)),
    IS_NULL((a, b) -> a == null),
    IS_NOT_NULL((a, b) -> a != null);

    private final BiPredicate<String, String> predicate;

    ComparisonOperator(BiPredicate<String, String> predicate) {
        this.predicate = predicate;
    }

    /**
     * Applies the comparison operation to the given values.
     *
     * @param a The first value to compare.
     * @param b The second value to compare.
     * @return The result of the comparison.
     */
    public boolean apply(String a, String b) {
        return predicate.test(a, b);
    }
}