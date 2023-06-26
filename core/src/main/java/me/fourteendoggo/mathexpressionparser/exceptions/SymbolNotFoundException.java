package me.fourteendoggo.mathexpressionparser.exceptions;

public class SymbolNotFoundException extends SyntaxException {
    private final String symbol;

    public SymbolNotFoundException(String symbol) {
        super("symbol %s not found", symbol);
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
