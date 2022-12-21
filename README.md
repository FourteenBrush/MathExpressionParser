# MathExpressionParser
Java utility to parse mathematical expressions and evaluate them

## Usage

```java
double result = ExpressionParser.parse("2 * (2 + 2)")
assert result == 8
```

The parse method throws an SyntaxException if the expression is invalid

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

- [ ] Implementing multiple operators together with operator priority
- [ ] Fix precision loss when working with very small numbers
