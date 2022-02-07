# Mindustry_code_compiler
code compiler for the Mindustry game

Python syntax, any lines not recognized will be added raw

## Currently Supporting:
### Loops and Conditions
```
if
elif
else
while loop
```
### Functions Already Built into the Game
```
max()
min()
angle()
len()
noise()
abs()
log()
log10()
sin()
cos()
tan()
floor()
ceil()
sqrt()
rand()
```
### Mathmatical Operators
```
^
**
%
*
//
/
+
-
<<
```
### Boolean Operators
```
<=
<
>=
<>
!=
===
==
```
### Reading and Writing to Cells and Banks
```
reading from cells by name and brackets: x = cell1[4 + a]
writing to cells by name and brackets cell1[4 + a] = x
```

## Errors
```
currently a bug involving negative numbers and the "subtract" symbol, do 'a = 0 - x' i guess.
```

## Modifying
```
All names for functions can be directly modified in data/operators.txt, along with adding additional call names (L15-30). Do not modify anything else.
For example: max:max can be changed to getMax:max to modify the name. The right side field determines what is given to Mindustry, so do not modify it.

This program generates a random tag to be used for additionally created variables. The tag length, or the tag its self, can be modified on line 12 or 18.
```
