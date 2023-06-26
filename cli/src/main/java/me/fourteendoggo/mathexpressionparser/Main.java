package me.fourteendoggo.mathexpressionparser;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.print("Enter an expression or type exit() to exit: ");
            try {
                String input = in.nextLine();
                long now = System.nanoTime();
                double result = ExpressionParser.parse(input);
                double elapsedMicros = (System.nanoTime() - now) / 1000.0;
                System.out.printf("[✔] Result: %s (%s µs)%n", result, elapsedMicros);
            } catch (NoSuchElementException e) { // ctrl-c
                break;
            } catch (Throwable t) {
                System.out.println("\u001B[31m[✘] Error: " + t.getMessage() + "\u001B[0m");
            }
        }
    }
}
