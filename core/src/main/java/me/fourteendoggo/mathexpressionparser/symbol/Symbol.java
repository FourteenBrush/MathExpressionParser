package me.fourteendoggo.mathexpressionparser.symbol;

import org.intellij.lang.annotations.RegExp;

import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;

/**
 * A symbol insertable in a {@link SymbolLookup}.
 *
 * @see Variable
 * @see FunctionCallSite
 */
public interface Symbol {
    @RegExp
    String NAME_PATTERN = "[a-zA-Z_][a-zA-Z0-9_]*";

    SymbolType getType();

    // FIXME: annotating this with @Pattern(NAME_PATTERN) would mean propagating that annotation everywhere
    /**
     * Returns the symbol name, must match {@link Symbol#NAME_PATTERN}
     */
    String getName();
}
