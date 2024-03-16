package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;
import me.fourteendoggo.mathexpressionparser.symbol.Symbol;
import me.fourteendoggo.mathexpressionparser.symbol.SymbolLookup;
import me.fourteendoggo.mathexpressionparser.symbol.Variable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SymbolLookupTest {
    private SymbolLookup lookup;

    @BeforeEach
    public void setup() {
        lookup = new SymbolLookup();
    }

    @Test
    void testNodeLayout() {
        lookup.
    }

    @Test
    void testVariableLookup() {
        Symbol boolTrue = new Variable("t", 1);
        char[] buf = "t + 1".toCharArray();
        lookup.insert(boolTrue);
        assertEquals(boolTrue, lookup.lookup(buf, 0));
    }

    @Test
    void testFunctionLookup() {
        Symbol func = new FunctionCallSite("e", 0, ctx -> Math.E);
        char[] buf = "e() + xyz".toCharArray();
        lookup.insert(func);

        FunctionCallSite retrievedFunc = (FunctionCallSite) lookup.lookup(buf, 0);
        assertEquals(func, retrievedFunc);
        assertEquals(Math.E, retrievedFunc.apply(retrievedFunc.allocateParameters()));
    }

    @Test
    void testInsertingDuplicateVariableThrows() {
        Symbol pi = new Variable("pi", Math.PI);
        lookup.insert(pi);
        assertThrows(SyntaxException.class, () -> lookup.insert(pi));
    }

    @Test
    void testInsertingDuplicateFunctionThrows() {
        Symbol func = new FunctionCallSite("e", 0, ctx -> Math.E);
        lookup.insert(func);
        assertThrows(SyntaxException.class, () -> lookup.insert(func));
    }

    @Test
    void testInsertingInvalidNamesThrows() {
        assertThrows(SyntaxException.class, () -> lookup.insert(new FunctionCallSite("@pi", 0, ctx -> Math.PI)));
    }
}
