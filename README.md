# eph-titanic

Programming Exercise: “Shuffling the table cells on the titanic.”

## Overview

### Stage 1.
Design a user interface which has a way for a user to choose the number of rows and columns in a table. The user interface will need an OK button which will be required for the next stage. Be as creative as you like, but two fields and a button are satisfactory for this stage.

### Stage 2.
When the user clicks on the OK button, show the (#main) div and write a table of the specified dimensions into the (iframe) provided already on the page. In addition, you will need to write some styles into this iframe for highlighting 'selected' cells (e.g. with a background colour). We do not want you to use external stylesheets for this.

### Stage 3.
Move the (#coords) span around to follow the mouse when WITHIN the iframe and display the current coordinates of the mouse WITHIN the iframe. The screenshot below will give some idea of what we mean. Note, this will need to work when the iframe is scrolled as well. As soon as the mouse moves outside of the iframe, the (#coords) span should be hidden again.

### Stage 4.
Provide a basic cell selection approach for the table. This will include:
1. A single left­click cancels the selection of any other cells, and toggles the selection of the cell at the mouse pointer.
2. A single right­click toggles the selection of the cell at the mouse pointer, and preserves selection of any other cells. Essentially, you are adding to the selection.
3. A double left­click cancels the selection of any other cells, and selects the entire row at the mouse pointer.

### Stage 5.
Pressing the delete key should delete all cells that are considered 'selected'. This will create an uneven table, which is fine.
Stage 6.
The logging area (#log) needs to be filled with logging information of the operations performed. The operations that need to be logged are:
a) Toggling selection of a cell. Specify the cell grid position e.g. (0, 1) for row 0, column 1
b) Clearing selection of all cells.
c) Selecting an entire row. Specify the index of the row selected
e.g. row 1
d) Deleting the selected cells.

## Setup

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL.

To run unit tests, run:

    lein do clean, cljsbuild test unit

## License

Copyright © 2016 MJP

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
