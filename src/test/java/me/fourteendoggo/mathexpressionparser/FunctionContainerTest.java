package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;
import me.fourteendoggo.mathexpressionparser.function.FunctionContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FunctionContainerTest {
    private FunctionContainer container;

    @BeforeEach
    void setup() {
        container = new FunctionContainer();
    }

    @Test
    void testInsertion() {
        container.insertFunction(new FunctionCallSite("cos", 0, context -> 0));
    }

    @Test
    void testSearching() {
        container.insertFunction("sin", Math::sin);
        assertDoesNotThrow(() -> container.search("sin(1)".toCharArray(), 0));
    }

    @Test
    void overlappingFunctionsStayPersistent() {
        container.insertFunction("sin", Math::sin);
        container.insertFunction("sinh", Math::sinh);
        assertDoesNotThrow(() -> container.search("sin(1)".toCharArray(), 0));
        assertDoesNotThrow(() -> container.search("sinh(1)".toCharArray(), 0));
    }

    @Test
    void nonLowercaseFunctionThrows() {
        assertThrows(SyntaxException.class, () -> container.insertFunction(new FunctionCallSite("COS", 0, context -> 0)));
        assertThrows(SyntaxException.class, () -> container.insertFunction(new FunctionCallSite("cos1", 0, context -> 0)));
        assertThrows(SyntaxException.class, () -> container.insertFunction(new FunctionCallSite("@sin", 0, context -> 0)));
    }

    @Test
    void duplicateFunctionThrows() {
        container.insertFunction("sin", Math::sin);
        // TODO: throw an exception when a function is already present
        assertThrows(SyntaxException.class, () -> container.insertFunction("sin", Math::sin));
    }
}
