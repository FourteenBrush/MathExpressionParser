package me.fourteendoggo.mathexpressionparser.environment;

import me.fourteendoggo.mathexpressionparser.exceptions.SymbolNotFoundException;
import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;
import me.fourteendoggo.mathexpressionparser.function.FunctionContext;
import me.fourteendoggo.mathexpressionparser.symbol.BuiltinSymbols;
import me.fourteendoggo.mathexpressionparser.symbol.Symbol;
import me.fourteendoggo.mathexpressionparser.symbol.SymbolLookup;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;

public class ExecutionEnv {
    private final SymbolLookup symbolLookup;

    public ExecutionEnv() {
        symbolLookup = new SymbolLookup();
        BuiltinSymbols.init(this);
    }

    /**
     * @see #insertFunction(String, int, int, ToDoubleFunction)
     */
    public void insertFunction(String name, DoubleSupplier fn) {
        insertSymbol(new FunctionCallSite(name, 0, ctx -> fn.getAsDouble()));
    }

    /**
     * @see #insertFunction(String, int, int, ToDoubleFunction)
     */
    public void insertFunction(String name, DoubleUnaryOperator fn) {
        insertSymbol(new FunctionCallSite(name, 1, ctx -> {
            double first = ctx.getDouble(0);
            return fn.applyAsDouble(first);
        }));
    }

    /**
     * @see #insertFunction(String, int, int, ToDoubleFunction)
     */
    public void insertFunction(String name, DoubleBinaryOperator fn) {
        insertSymbol(new FunctionCallSite(name, 2, ctx -> {
            double first = ctx.getDouble(0);
            double second = ctx.getDouble(1);
            return fn.applyAsDouble(first, second);
        }));
    }

    /**
     * @see #insertFunction(String, int, int, ToDoubleFunction)
     */
    public void insertFunction(String name, int numArgs, ToDoubleFunction<FunctionContext> fn) {
        insertSymbol(new FunctionCallSite(name, numArgs, numArgs, fn));
    }

    /**
     * Inserts a function with a certain amount of parameters, which are retrieved from the function context.
     * @param name the function name
     * @param minArgs the minimum amount of arguments
     * @param maxArgs the maximum amount of arguments
     * @param fn the function
     */
    public void insertFunction(String name, int minArgs, int maxArgs, ToDoubleFunction<FunctionContext> fn) {
        insertSymbol(new FunctionCallSite(name, minArgs, maxArgs, fn));
    }

    /**
     * Inserts a symbol into this environment.
     * @param symbol the symbol to be inserted.
     * @throws SyntaxException if the symbol was already inserted, either as a function or as a variable.
     */
    public void insertSymbol(Symbol symbol) {
        symbolLookup.insert(symbol);
    }

    // INTERNAL
    public Symbol lookupSymbol(char[] buf, int pos) {
        Symbol symbol = symbolLookup.lookup(buf, pos); // already incremented pos
        if (symbol == null) {
            String bufAsStr = new String(buf, pos, buf.length - pos);
            String symbolName = bufAsStr.split("[^a-zA-Z]")[0];
            throw new SymbolNotFoundException(symbolName);
        }
        return symbol;
    }
}
