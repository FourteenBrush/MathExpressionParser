package me.fourteendoggo.mathexpressionparser.symbol;

public record Variable(String name, double value) implements Symbol {

    public Variable {
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
    @Override
    public SymbolType getType() {
        return SymbolType.VARIABLE;
    }

    @Override
    public String getName() {
        return name;
    }
}
