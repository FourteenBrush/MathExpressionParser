# MathExpressionParser

[![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](http://perso.crans.org/besson/LICENSE.html)

A lightweight Java library for parsing and evaluating mathematical expressions.

This algorithm works with a linked list of tokens that is created from the input string.
The tokens are then evaluated with order of operations.
The algorithm is the most performant when higher order operators are placed in front of
lower priority ones but this will probably change in the future.

## Usage

The usage is very simple, just call the static parse method on the ExpressionParser class.

```java
double result = ExpressionParser.parse("3(5-1)^2");
assert result == 48;
```

Note that the parse method throws an unchecked SyntaxException if the expression is invalid.

### Functions

Support for functions is being worked on, a few basic functions are already implemented:

- sin, asin, sinh
- cos, acos, cosh
- tan, atan, tanh
- sqrt
- rad, which converts the input to radians
- log, which is the natural logarithm

Functions are called like regular functions in Java, for example:

```java
double result = ExpressionParser.parse("sin(rad(90))");
assert result == 1;
```

Custom functions can be added, but currently only functions with one argument are supported.

```java
// this inserts a function called "myFunc" that doubles the input
ExpressionParser.insertFunction("myfunc", args -> args[0] * 2);
double result = ExpressionParser.parse("myFunc(10)");
assert result = 20;
```

### Operators

| Operator | Example | Explanation                                               |
|:--------:|---------|-----------------------------------------------------------|
|    ^     | 2 ^ 3   | 2 to the power of 3                                       |
|    *     | 2 * 3   | 2 multiplied by 3                                         |
|    /     | 2 / 3   | 2 divided by 3                                            |
|    %     | 2 % 3   | remainder of 2 divided by 3                               |
|    +     | 2 + 3   | adds 2 and 3 together                                     |
|    -     | 2 - 3   | subtracts 2 and 3 or negates a number when placed like -x |

I can refer to the [Operator enum](src/main/java/me/fourteendoggo/mathexpressionparser/tokens/Operator.java) for more information.

## Notes

```
- The parser ignores spaces, except for spaces between two  parts of a number, which are considered invalid.
- Functions are very unstable at the moment and may not work as intended.
```

## Todo

- [ ] Implementing function calls
- [ ] Making the solving algorithm more efficient
- [ ] Implementing variables and boolean logic
- [x] Implementing multiple operators together with operator priority
