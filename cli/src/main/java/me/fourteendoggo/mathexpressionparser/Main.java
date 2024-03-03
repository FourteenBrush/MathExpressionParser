package me.fourteendoggo.mathexpressionparser;

import me.fourteendoggo.mathexpressionparser.symbol.ExecutionEnv;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    private static boolean running = true;

    public static void main(String[] args) {
        ExecutionEnv env = ExecutionEnv.createDefault();
        env.insertFunction("exit", () -> {
            running = false;
            return 0;
        });
        env.insertVariable("x0", 1);
        env.insertVariable("x1A", 1);

        Scanner in = new Scanner(System.in);

        while (running) {
            System.out.print("Enter an expression or type exit() to exit: ");
            try {
                String input = in.nextLine();
                long now = System.nanoTime();
                double result = ExpressionParser.parse(input, env);
                double elapsedMicros = (System.nanoTime() - now) / 1000.0;
                System.out.printf("[✔] Result: %s (%s µs)%n", result, elapsedMicros);
            } catch (NoSuchElementException e) { // ctrl-c
                break;
            } catch (Throwable t) {
                System.out.printf("\u001B[31m[✘] Error: %s\u001B[0m\n", t.getMessage());
            }
        }
    }
}
