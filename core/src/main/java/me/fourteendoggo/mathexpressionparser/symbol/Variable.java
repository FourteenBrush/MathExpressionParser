package me.fourteendoggo.mathexpressionparser.symbol;

public record Variable(String getName, double value) implements Symbol {

    public Variable {
        // NOTE: variable names are only checked for validity after insertion
        if (getName.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }

    @Override
    public SymbolType getType() {
        return SymbolType.VARIABLE;
    }
}
