package me.lrnzx.csv;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DateBetweenNode implements ExpressionNode {
    private final String field;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final DateTimeFormatter formatter;

    public DateBetweenNode(String field, LocalDate startDate, LocalDate endDate, DateTimeFormatter formatter) {
        this.field = field;
        this.startDate = startDate;
        this.endDate = endDate;
        this.formatter = formatter;
    }

    @Override
    public boolean evaluate(Map<String, String> row) {
        final String value = row.get(field);

        if (value == null) return false;
        try {
            final LocalDate date = LocalDate.parse(value, formatter);
            return !date.isBefore(startDate) && !date.isAfter(endDate);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor,
                        Map<String, String> row) {
        return visitor.visit(this, row);
    }

    public String getField() {
        return field;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }
}