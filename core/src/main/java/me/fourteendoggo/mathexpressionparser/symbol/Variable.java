package me.fourteendoggo.mathexpressionparser.symbol;

import me.fourteendoggo.mathexpressionparser.utils.Assert;

public record Variable(String getName, double value) implements Symbol {

    public Variable {
        Assert.isValidIdentifierName(getName);
    }

    @Override
    public SymbolType getType() {
        return SymbolType.VARIABLE;
    }
}
