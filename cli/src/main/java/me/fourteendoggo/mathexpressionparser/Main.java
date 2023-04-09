package me.fourteendoggo.mathexpressionparser;

import java.util.Scanner;

public class Main {

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.print("Enter an expression or type exit() to exit: ");
            String input = in.nextLine();
            long now = System.nanoTime();
            double result;
            try {
                result = ExpressionParser.parse(input);
            } catch (Throwable t) {
                System.out.println("\u001B[31m[✘] Error: " + t.getMessage() + "\u001B[0m");
                continue;
            }
            double elapsedMicros = (System.nanoTime() - now) / 1000.0;
            System.out.printf("[✔] Result: %s (%s µs)%n", result, elapsedMicros);
        }
    }
}
