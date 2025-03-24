# Evaluator
An evaluator of a stripped down version of JS, aiming to replicate REPL of Node.js.

## Supports:
- Declarations
- Function Declarations
  - functions defined like:
  - ```js
    function double(x) {
      return x * 2;
    }
    ```
- Expressions
  - Arithmetic operations of +, -, *, /
  - Assignment
  - Function expressions
  - Variable References
  - Literals (values)



## Example Usage
For an initial program with:
```js
/* 1 */ let a = 1;
/* 2 */ let b = 2;
/* 3 */ let c = 3;
/* 4 */ let d = a + b;
/* 5 */ c + d
```
These requests can be executed:
```
>>> evalLine 5
<<< 6
>>> assign a 2
<<< ok
>>> evalLine 5
<<< 7
```
 
## Tests:
Tests are contained in `src/test/` directory. The tests are:
- `EvalTests.kt` 
  - Tests core evaluation functionality
- `ParseTests.kt`
  - Tests core parsing functionality