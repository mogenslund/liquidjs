# liquidjs
Experiments with liquid port to ClojureScript and Node.js

Almost nothing is working yet. It is a proof-of-concept.

## To run in browser
Execute:

```
./build
```
Then open `index.html`.

Or go to [salza.dk/liq](http://salza.dk/liq)

See [salza.dk/cheatsheet.html](http://salza.dk/cheatsheet.html) for some keyboard shortcuts. Not all of them are working in the ClojureScript version. Mostly text editing and evaluation. No typeahead and file navigation.

## To run using Lumo and NodeJS
Requires Lumo.

To start the pieces using Lumo execute:

```
lumo -c "./src" -m dk.salza.liq.core --tty
```