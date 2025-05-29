# AppGrid in Compose
The main functionality is in PagerGrid.kt.
There's a Preview function that you can run and test.

PagerGrid function is a Composable function which is also a custom layout.
Layout() {} function can be devided into two parts:
- Measuring: where we calculate all the sizes/constraints of the content;
- Placing: calculating the correct positions for each indexed item.

The Composable misses snapping and drag&drop logic.
