package me.fourteendoggo.mathexpressionparser.symbol;

import me.fourteendoggo.mathexpressionparser.exceptions.SymbolNotFoundException;
import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;
import me.fourteendoggo.mathexpressionparser.function.FunctionContext;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;

// TODO: add support for removing/ inserting if absent

/**
 * An environment instance, to which symbols can be bound.
 * When using this environment as a lookup for the parser,
 * only symbols found in this environment will be seen.
 */
public class ExecutionEnv {
    //private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9]*");
    private final SymbolLookup symbolLookup;

    /**
     * Creates an empty {@link ExecutionEnv}, no symbols are bound.
     */
    public ExecutionEnv() {
        symbolLookup = new SymbolLookup();
    }

    // TODO: add empty()

    /**
     *
     * @return a {@link ExecutionEnv} populated with all default symbols.
     */
    @ApiStatus.Experimental
    public static ExecutionEnv createDefault() {
        // delegate to BuiltinSymbols to not clutter this class
        return BuiltinSymbols.createExecutionEnv();
    }

    public void insertVariable(String name, double value) {
        insertSymbol(new Variable(name, value));
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
        insertFunction(name, numArgs, numArgs, fn);
    }

    /**
     * Inserts a function with a certain amount of parameters, which correspond to parameters in the function context.
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

    /**
     * Looks up a symbol based on an input
     * @param buf the input as a char array
     * @param pos the position to start searching at
     * @return the found symbol
     * @throws SymbolNotFoundException if no symbol could be found
     */
    @ApiStatus.Internal
    public Symbol lookupSymbol(char[] buf, int pos) {
        Symbol symbol = symbolLookup.lookup(buf, pos);
        if (symbol == null) {
            String bufAsStr = new String(buf, pos, buf.length - pos);
            // TODO: also change when valid chars for symbol name change
            String symbolName = bufAsStr.split("[^a-zA-Z0-9_]")[0];
            throw new SymbolNotFoundException(symbolName);
        }
        return symbol;
    }
}
