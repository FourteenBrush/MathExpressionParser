package me.fourteendoggo.mathexpressionparser.symbol;

/**
 * Used to allow being inserted into a SymbolLookup
 */
public interface Symbol {

    SymbolType getType();

    String getName();
}
