package me.fourteendoggo.mathexpressionparser.symbol;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;
import me.fourteendoggo.mathexpressionparser.utils.Utility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Deque;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SymbolLookupTest {
    private SymbolLookup lookup;

    @BeforeEach
    public void setup() {
        lookup = new SymbolLookup();
    }

    @Test
    void testNodeLayout() {
        lookup = ExecutionEnv.defaulted().symbolLookup;
        Deque<SymbolLookup.Node> stack = new LinkedList<>();
        SymbolLookup.Node curr = lookup.root;
        stack.push(curr);

        while (!stack.isEmpty()) {
            SymbolLookup.Node node = stack.pop();
            for (SymbolLookup.Node child : node.children) {
                if (child == null) continue;
                stack.push(child);

                int hasChildren = child.getData() >> SymbolLookup.Node.HAS_CHILDREN_SHIFT;
                assertThat(hasChildren)
                        .withFailMessage("upper byte of Node.value must only be used to indicate used children")
                        .isIn(SymbolLookup.Node.HAS_CHILDREN, 0);

                char value = child.getCharacter();
                assertTrue(Utility.isValidIdentifierChar(value));
            }
        }
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

    @Test
    void testRetrievalOfPartialNames() {
        lookup.insert(new Variable("mi", 3.4));
        Symbol s = lookup.lookup("m".toCharArray(), 0);
        assertThat(s).isNull();

        assertThat(lookup.lookup("xmi aa".toCharArray(), 1)).isNotNull();
    }
}
