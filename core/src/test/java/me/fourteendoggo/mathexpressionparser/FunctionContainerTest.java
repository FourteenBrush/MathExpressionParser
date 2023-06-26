package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.exceptions.SyntaxException;
import me.fourteendoggo.mathexpressionparser.function.FunctionCallSite;
import me.fourteendoggo.mathexpressionparser.function.FunctionContainer;
import me.fourteendoggo.mathexpressionparser.function.FunctionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class FunctionContainerTest {
    private FunctionContainer container;

    @BeforeEach
    void setup() {
        container = FunctionContainer.empty();
    }

    @Test
    void testInsertion() {
        assertDoesNotThrow(() -> {
            container.insertFunction("cos", Math::cos);
            container.insertFunction("co", Math::cos);
            container.insertFunction("cow", Math::cos);
            container.insertFunction("cosh", Math::cos);
            container.insertFunction("coss", Math::cos);
        });
    }

    @Test
    void testSearching() {
        container.insertFunction("sin", Math::sin);
        FunctionCallSite function = container.getFunction("sin(1)".toCharArray(), 0);
        FunctionContext parameters = function.allocateParameters();
        parameters.add(1);
        assertEquals(Math.sin(1), function.apply(parameters));
    }

    @Test
    void testFunctionContextResizing() {
        FunctionCallSite function = new FunctionCallSite("max", 2, Integer.MAX_VALUE, ctx -> {
            double max = ctx.getDouble(0);
            for (int i = 1; i < ctx.size(); i++) {
                max = Math.max(max, ctx.getDouble(i));
            }
            return max;
        });
        container.insertFunction(function);
        FunctionContext parameters = function.allocateParameters();
        IntStream.range(0, 20).forEach(parameters::add);

        assertEquals(20, parameters.size());
        char[] buf = "max(bla bla bla)".toCharArray();
        assertEquals(19, container.getFunction(buf, 0).apply(parameters));
    }

    @Test
    void overlappingFunctionsStayPersistent() {
        container.insertFunction("sin", Math::sin);
        container.insertFunction("sinh", Math::sinh);
        assertDoesNotThrow(() -> container.getFunction("sin(1)".toCharArray(), 0));
        assertDoesNotThrow(() -> container.getFunction("sinh(1)".toCharArray(), 0));
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
        assertThrows(SyntaxException.class, () -> container.insertFunction("sin", Math::sin));
    }
}
