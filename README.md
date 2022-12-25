# MathExpressionParser

[![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](http://perso.crans.org/besson/LICENSE.html)

A lightweight Java library for parsing and evaluating mathematical expressions.

This algorithm works with a linked list of tokens that is created from the input string.
The tokens are then evaluated with order of operations.

## Usage

To parse an expression, just call the static `parse` method of the [ExpressionParser](src/main/java/me/fourteendoggo/mathexpressionparser/ExpressionParser.java) class:

```java
double result = ExpressionParser.parse("3(5-1)^2");
assert result == 48;
```

Note that this method throws an unchecked SyntaxException if the syntax of the expression is invalid.

### Functions

There is a built-in support for trigonometric and other standard functions.
Take a look at the [FunctionContainer](src/main/java/me/fourteendoggo/mathexpressionparser/function/FunctionContainer.java) class to see them all.

To add custom functions, call the appropriate `insertFunction` method on the [ExpressionParser](src/main/java/me/fourteendoggo/mathexpressionparser/ExpressionParser.java) class. <br/>
There are predefined methods to inserts functions with either one or two arguments. In other cases, just specify the minimum and maximum number of arguments.

Note that function names must only contain lowercase letters (a-z).

#### Examples:

```java
// this inserts a function called "twice" that doubles the input
ExpressionParser.insertFunction("twice", arg -> arg * 2);
double result = ExpressionParser.parse("twice(10)");
assert result == 20;
```

Functions cannot be overloaded, but you can define one function that takes a variable number of arguments.

```java
// insert a function called "add" that adds all the arguments together
// the function has a minimum of 2 and a maximum of 10 arguments
// note the get() method that returns the parameter at the specified index
ExpressionParser.insertFunction("add", 2, 10, ctx -> {
    double sum = 0;
    for (int i = 0; i < ctx.size(); i++) {
        sum += ctx.get(i);
    }
    return sum;
});
```

It's also possible to define a function that returns a constant value. This might be replaced in the future with actual constants like `$PI`.

```java
// insert a function called "pi" that returns the value of pi
ExpressionParser.insertFunction("pi", 0, 0, ctx -> Math.PI);
double result = ExpressionParser.parse("pi()");
assert result == Math.PI;
```

Functions are called like regular methods in Java and can also be nested, for example:

```java
double result = ExpressionParser.parse("sin(rad(max(60, 60 + 30)))");
// sin of 90 degrees is 1
assert result == 1;
```

```java
// 2min(1,2) is the same as 2*min(1,2)
double result = ExpressionParser.parse("2min(1, 2, 3, 4, 5)");
assert result == 2;
```

### Operators

| Operator | Example | Explanation                                               |
|:--------:|---------|-----------------------------------------------------------|
|    ^     | 2 ^ 3   | 2 to the power of 3                                       |
|    *     | 2 * 3   | 2 multiplied by 3                                         |
|    /     | 2 / 3   | 2 divided by 3                                            |
|    %     | 2 % 3   | remainder of 2 divided by 3 (modulo)                      |
|    +     | 2 + 3   | adds 2 and 3 together                                     |
|    -     | 2 - 3   | subtracts 2 and 3 or negates a number when placed like -x |

Take a look at the [Operator enum](src/main/java/me/fourteendoggo/mathexpressionparser/tokens/Operator.java) for more information.

## Additional information

The parser ignores spaces, except for spaces between two parts of a number, which are considered invalid.

A bunch of examples (tests, which should all be working) can be found in the [tests.txt file](src/test/tests.txt).

## TODO

- [x] Implementing multiple operators together with operator priority
- [x] Implementing function calls
- [ ] Implementing variables and constants
- [ ] Implementing boolean logic, currently these can be implemented with functions
- [ ] Making the solving algorithm more efficient
