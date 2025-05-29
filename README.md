# AppGrid in Compose
The main functionality is in PagerGrid.kt.
There's a Preview function that you can run and test.
Fling behaviour and snapping is not working/not completed, so you can ignore that part of the code.

PagerGrid function is a Composable function which is also a custom layout.
Layout() {} function can be devided into two parts:
- Measuring: where we calculate all the sizes/constraints of content;
- Placing: calculating correct position for each indexed item.
