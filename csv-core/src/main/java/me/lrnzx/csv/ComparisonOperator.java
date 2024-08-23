package me.lrnzx.csv;

/**
 * Enumerates the possible comparison operations that can be performed
 * in a ComparisonNode. This enum provides a type-safe way to specify
 * how two values should be compared.
 */
public enum ComparisonOperator {
    EQUALS, NOT_EQUALS, GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL, CONTAINS, STARTS_WITH, ENDS_WITH
}