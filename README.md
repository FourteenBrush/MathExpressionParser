# MathExpressionParser
Java utility to parse mathematical expressions and evaluate them

## Usage

```java
ExpressionParser parser = new ExpressionParser();
double output = parser.parse("100 + 10.5");

assertEquals(110.5, output);
```

The parse method throws a InputMismatchException, meaning the input could not be parsed correctly, you may want to catch this exception

## Syntax

| Operator | Example | Explanation                                               |
|:--------:|---------|-----------------------------------------------------------|
|    ^     | 2 ^ 3   | 2 to the power of 3                                       |
|    *     | 2 * 3   | 2 multiplied by 3                                         |
|    /     | 2 / 3   | 2 divided by 3                                            |
|    %     | 2 % 3   | remainder of 2 divided by 3                               |
|    +     | 2 + 3   | adds 2 and 3 together                                     |
|    -     | 2 - 3   | subtracts 2 and 3 or negates a number when placed like -x |

## Notes

```
- The parser does not care about spaces, so put as many as you want
  
- Currently only expressions of the form "x operator y" work, this is because operator priority
  isn't yet implemented. Using more operators will break those
```

## Todo

- [x] Use doubles instead of integers
- [ ] Implementing multiple operators together with operator priority
- [ ] Implementing parentheses
- [ ] Fix precision loss when working with very small numbers
- [ ] Strictly disallowing invalid input by throwing the appropriate exception
