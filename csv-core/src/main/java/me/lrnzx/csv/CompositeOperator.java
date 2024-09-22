package me.lrnzx.csv;

/**
 * Enumerates the possible logical operations that can be performed
 * in a CompositeNode. This enum provides a type-safe way to specify
 * how the results of multiple child nodes should be combined.
 */
public enum CompositeOperator {
    AND, OR;

    public CompositeOperator opposite() {
        return this == AND ? OR : AND;
    }
}