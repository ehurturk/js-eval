# Evaluator
An evaluator of a stripped down version of JS, aiming to replicate REPL of Node.js.
```javascript
/* 1 */ let a = 1;
/* 2 */ let b = 2;
/* 3 */ let c = 3;
/* 4 */ let d = a + b;
/* 5 */ c + d
```
```
>>> evalLine 5
<<< 6
>>> assign a 2
<<< ok
>>> evalLine 5
<<< 7
```
