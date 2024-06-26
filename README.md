# MathExpressionParser

![GitHub Workflow Status (with event)](https://img.shields.io/github/actions/workflow/status/FourteenBrush/MathExpressionParser/build.yml)
![Code Coverage](https://img.shields.io/codecov/c/github/FourteenBrush/MathExpressionParser)
![GitHub License](https://img.shields.io/github/license/FourteenBrush/MathExpressionParser)
[![GitHub release](https://img.shields.io/github/v/release/FourteenBrush/MathExpressionParser)](https://github.com/FourteenBrush/MathExpressionParser/releases)

A lightweight Java library for parsing and evaluating mathematical expressions.

## Dependency

Replace `Tag` with the appropriate version, which you can find on the
[releases page](https://github.com/FourteenBrush/MathExpressionParser/releases) or on top of this file.

### Maven:

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
```xml
<dependency>
    <groupId>com.github.FourteenBrush</groupId>
    <artifactId>MathExpressionParser</artifactId>
    <version>Tag</version>
</dependency>
```

### Gradle

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
```
```gradle
dependencies {
    implementation 'com.github.FourteenBrush:MathExpressionParser:Tag'
}
```

## Usage

To parse an expression, call the static `parse` method of the [ExpressionParser](core/src/main/java/me/fourteendoggo/mathexpressionparser/ExpressionParser.java) class:

```java
double result = ExpressionParser.parse("3(5-1)");
assert result == 12;
```

> [!NOTE]
> This method throws an unchecked `SyntaxException` if the expression is invalid.

### Functions and variables

There is built-in support for trigonometric, mathematical and other common functions, click
[here](core/src/main/java/me/fourteendoggo/mathexpressionparser/symbol/BuiltinSymbols.java) to see them all.

Functions are called like normal Java methods, they can have zero or more parameters and can be nested:

```java
double result = ExpressionParser.parse("sin(rad(90))");
assert result == 1; // sine of 90° is 1
```

```java
double result = ExpressionParser.parse("min(5, 10, 3)");
assert result == 3;
```

Variables are just called by their name like you would expect them.

```java
double pi = ExpressionParser.parse("pi");
assert pi == Math.PI;

double one = ExpressionParser.parse("true == 1");
assert one == 1;
```

Built-in variables are `pi`, `e`, `tau`, `true` (1) and `false` (0).

### Inserting custom functions and variables.

To insert custom functions or variables, call the appropriate insert method on the `ExpressionParser` class.
This inserts into the global execution environment, examples:

```java
/* This inserts a function called twice that doubles its input */
ExpressionParser.insertFunction("twice", number -> number * 2);
double result = ExpressionParser.parse("twice(2)");
assert result == 4;
```

Functions cannot be overloaded, but you can define a function that accepts a variable amount of arguments.  

```java
/* 
 * This inserts a function called 'add' that returns the sum of all of its arguments.
 * It accepts a minimum of 2 and a maximum of 10 arguments.
 */
ExpressionParser.insertFunction("add", 2, 10, ctx -> {
    double sum = ctx.getDouble(0);
    for (int i = 1; i < ctx.size(); i++) {
        sum += ctx.getDouble(i); // gets the argument at that index
    }   
});

double result = ExpressionParser.parse("add(1, 2, 4)");
assert result == 7;
```
PS: don't actually do this, there is already a built-in `sum` function.

For more complex functions, take a look at the method that accepts a `Symbol` and pass in a 
[FunctionCallSite](core/src/main/java/me/fourteendoggo/mathexpressionparser/function/FunctionCallSite.java).

Inserting variables is very similar:

```java
ExpressionParser.insertVariable("magic", 1.234);
double magic = ExpressionParser.parse("magic");
assert magic == 1.234;
```

### Using a custom execution environment (recommended):

As mentioned above, inserting functions or variables will place them in the global symbol lookup.
If you want more flexibility over what symbols can be used in what context, you can explicitly provide a
`ExecutionEnv`:

```java
ExecutionEnv env = ExecutionEnv.empty();
// 'day' function is only bound to this environment
env.insertFunction("day", () -> {
    DayOfWeek day = LocalDate.now().getDayOfWeek();
    return day.getValue();
});

// tell the parser which environment to use
double dayOfWeek = ExpressionParser.parse("day()", env); 
assert dayOfWeek >= 1 && dayOfWeek <= 7;

// ERROR: global environment does not have this function, we did not specify our own one so the global one is used
double error = ExpressionParser.parse("day()");
```

### Operators

> [!NOTE]
> For all logical operators, a 0 means false, whereas everything else is true. To make it more clear, you can use
the built-in `true` and `false` variables.

| Operator | Example  | Explanation                                                |
|:--------:|----------|------------------------------------------------------------|
|    !     | !(1 < 2) | logical not, unary                                         |
|    ~     | ~2       | bitwise not, unary                                         |
|    *     | 2 * 3    | multiplies 2 by 3                                          |
|    /     | 2 / 3    | divides 2 by 3 (floating point division)                   |
|    %     | 8 % 3    | remainder of 8 divided by 3 (modulo)                       |
|    +     | 2 + 3    | adds 2 and 3 together                                      |
|    -     | 2 - 3    | subtracts 2 and 3                                          |
|    <<    | 4 << 2   | shifts 4 2 bits to the left                                |
|    >>    | 32 >> 2  | shifts 32 2 bits to the right                              |
|    <     | 3 < 1    | returns 1 if 3 is smaller than 1, 0 otherwise              |
|    >     | 4 > 2    | returns 1 if 4 is bigger than 2, 0 otherwise               |
|    <=    | 12 <= 3  | returns 1 if 12 is smaller than or equal to 3, 0 otherwise |
|    >=    | 10 >= 9  | returns 1 if 10 is bigger than or equal to 9, 0 otherwise  |
|    ==    | 10 == 9  | returns 1 if 10 equals 9, 0 otherwise                      |
|    !=    | 10 != 9  | returns 1 if 10 does not equal 9, 0 otherwise              |
|    &     | 10 & 9   | bitwise and, requires integers as operands                 |
|    ^     | 2 ^ 3    | power, pow(2, 3) as a function also exists                 |
|    \|    | 10 \| 9  | bitwise or, requires integers as operands                  |
|    &&    | 10 && 9  | boolean and                                                |
|   \|\|   | 2 \|\| 1 | boolean or                                                 |

Take a look at the [Operator](core/src/main/java/me/fourteendoggo/mathexpressionparser/token/Operator.java) enum for more information.

## Additional information

This algorithm works with a linked list of calculations that is created from the input string.
The calculations are then evaluated with order of operations.

The parser ignores spaces, except for spaces between two parts of a number, which are considered invalid.

A list of examples (tests, which should all be working) can be found in the [tests](core/src/test/resources/positive-input.csv) file.

## TODO (no particular order)

- [x] Implementing multiple operators together with operator priority
- [x] Implementing function calls
- [x] Making the solving algorithm more efficient
- [x] Implementing boolean logic, currently these can be implemented with functions
- [x] Implementing variables and constants
- [x] Make it possible to insert variables and functions on a non-global base
- [ ] Allow numeric values with different bases, e.g. 0x1, 0b2.
- [ ] Allowing to insert variables through the parser, e.g. "x = sqrt(16)"
- [ ] Allow multi-line expressions
- [ ] Allow expressions to be cached for later reuse

## More examples:

```java
Scanner in = new Scanner(System.in);

ExecutionEnv env = ExecutionEnv.createDefault();
env.insertFunction("input", in::nextDouble);

env.insertFunction("isLeapYear", 1, ctx -> {
    // you need a FunctionContext parameter to force the input to be an int
    // as all parameters are doubles implicitly  
    int year = ctx.getInt(0);
    return Utility.boolToDouble(java.time.Year.isLeap(year));
});

double result = ExpressionParser.parse("isLeapYear(input())", env);
boolean isLeapYear = Utility.doubleToBool(result);
```

```java
ExecutionEnv env = ExecutionEnv.createDefault();
env.insertVariable("x", 4);
env.insertVariable("y", 2);
env.insertVariable("z", 4);

double vectorMagnitude = ExpressionParser.parse("sqrt(pow(x, 2) + pow(y, 2) + pow(z, 2))", env);
assert vectorMagnitude == 6;
```
