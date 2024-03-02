package me.fourteendoggo.mathexpressionparser.symbol;

import me.fourteendoggo.mathexpressionparser.utils.Assert;
import me.fourteendoggo.mathexpressionparser.utils.Utility;

public record Variable(String getName, double value) implements Symbol {

    public Variable {
        Assert.isTrue(Utility.isValidIdentifierName(getName), "invalid variable name %s", getName);
    }

    @Override
    public SymbolType getType() {
        return SymbolType.VARIABLE;
    }
}
