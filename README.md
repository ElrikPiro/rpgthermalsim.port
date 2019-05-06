# rpgthermalsim.port
port of the rpgthermalsim (https://github.com/ElrikPiro/rpg-thermal-sim) from C++ to java

A thermal simulator to control the spreading of temperature and fire on a RPG setup. Personal project. The application is intended to use as an aid for the dungeon master on a tabletop RPG (i.e. Dungeons and Dragons, Pathfinder, Stormbringer, Hawkmoon, ...) accelerating calculations on room temperature, fire spreading and smoke presence on a grid.

## Instalation

Platform independent, tested on Linux. Download the jar archive and run it with the `java --jar archive.jar` command, Java Runtime Environment 8 or greater required.

You can also build and test it yourself with `mvn install` and `mvn test`. JDK 8 or greater and Apache Maven 3.6.0 or newer are required.

## Usage
### Launch
The application can be launched directly from the file browser by double-clicking the icon.

On linux systems you can launch it like any regular application:
```
PATH/TO/PROGRAM/rpg-thermal-sim [PATH/TO/SAVEFILE]
```
As remarked in the last code you can optionally pass a file as a parameter in order to load a previous map layout

### How to layout your map
Once you start the application the console terminal will output something like this:
```
Iteration: 0
command> 
```
Of course there's not much to do here, but let's assume that we want to model a 5x5 room, then, we would have to call the following command:
```
build roomA 5 5 The first room that we are modeling
```
now, the output will be something like this:
```
Iteration: 0
CODE: <roomA>
The first room that we are modeling
[   ][   ][   ][   ][   ]
[   ][   ][   ][   ][   ]
[   ][   ][   ][   ][   ]
[   ][   ][   ][   ][   ]
[   ][   ][   ][   ][   ]
command> 
```
The command build takes the first word as the name that identifies the room we've just created, the second is the width, the third the height, after that comes, optionally, a description.

Each cell of the room will be identified by the name of the room and it's coordinates, being the coordinates of the first cell (bottom left) `roomA 1 1` and the last cell (top right) `roomA 5 5`.

Now, figure we want this room to have a neighbouring room, let's say, like this:
```
[   ][   ][   ][   ][   ]
[   ][   ][   ][   ][   ]     [   ][   ][   ][   ]     
[   ][   ][   ][   ][   ][   ][   ][   ][   ][   ]
[   ][   ][   ][   ][   ]     [   ][   ][   ][   ]
[   ][   ][   ][   ][   ]
```
in order to achieve this layout we should create two additional rooms:
```
build doorAB 1 1 Door that connects roomA and roomB
build roomB 4 3
```
Let's check what does the output show us:
```
Iteration: 0
CODE: <doorAB>
Door that connects roomA and roomB
[   ]
CODE: <roomA>
The first room that we are modeling
[   ][   ][   ][   ][   ]
[   ][   ][   ][   ][   ]
[   ][   ][   ][   ][   ]
[   ][   ][   ][   ][   ]
[   ][   ][   ][   ][   ]
CODE: <roomB>
Room without description
[   ][   ][   ][   ]
[   ][   ][   ][   ]
[   ][   ][   ][   ]
command> 
```
Note that we didn't specified a description for roomB so it defaults to `Room without description`

Now this, by itself does not represent the scheme I'd drawn before, we must __link__ the cells together. In our rooms, each cell is represented by the brackets and it's contents, being `[   ]` an empty cell (there's nothing but air), cells can be shown in different colors and values that show information about it's state.

The most important state is the Temperature, in order to a cell to be able to calculate it's temperature, it needs to know it's neightbourging cells temperature. Every cell on a new room knows it's neighbouring cells, but if we want cells from different rooms to take into account each other, we'll need to specify it by ourselves, that's what the __link__ command does.

Let's connect the `doorAB 1 1` cell with the `roomA 5 3` and the `roomB 1 2` cells so we can achieve our objective with the next commands:
```
link doorAB 1 1 roomA 5 3
link doorAB 1 1 roomB 1 2
```
Now, our rooms are connected, even if the output does not differ from the last output.

Now It's time to **put** some ~~inflamable~~ forniture to decorate our pretty rooms, that's what the **put** command does, let's say we want to model a door that is made of wood so it ignites between 200ºC and 300ºC this door is, of course, located on the cell `doorAB 1 1` and the temperature of ignition will be 200ºC.

The command, simple as it is will be the next:
```
put doorAB 1 1 2
```
meaning the last `2` that the ignition temperature is 200 (200/100).

The output now is the next:
```
Iteration: 0
CODE: <doorAB>
Door that connects roomA and roomB
[ 2 ]
CODE: <roomA>
The first room that we are modeling
[   ][   ][   ][   ][   ]
[   ][   ][   ][   ][   ]
[   ][   ][   ][   ][   ]
[   ][   ][   ][   ][   ]
[   ][   ][   ][   ][   ]
CODE: <roomB>
Room without description
[   ][   ][   ][   ]
[   ][   ][   ][   ]
[   ][   ][   ][   ]
command> 
```
The `[ 2 ]` shown in blue means _"this cell has an inflamable object that will ignite at 200ºC"_

by now, let's **save** this map, using the next command:
```
save tutorial.txt
```
After using this command you'll be able to use the command **load** in order to reload your last saved map.
```
load tutorial.txt
```

### Playing with Fire
Now, let's start burning things, with the debugging command **set** we are going to start a fire on the `roomB 4 2` cell.
`set roomB 4 2 1 -20 0`
after sending the command our output will look like this:
```
Iteration: 0
CODE: <doorAB>
Door that connects roomA and roomB
[ 2 ]
CODE: <roomA>
The first room that we are modeling
[   ][   ][   ][   ][   ]
[   ][   ][   ][   ][   ]
[   ][   ][   ][   ][   ]
[   ][   ][   ][   ][   ]
[   ][   ][   ][   ][   ]
CODE: <roomB>
Room without description
[   ][   ][   ][   ]
[   ][   ][   ][ * ]
[   ][   ][   ][   ]
command>
```
The asterisk (colored in red), means that theré's fire on that cell, now let's just press enter without any command and observe how things are evolving. 

During the first 20 iterarions we could see of the grey cells were expanding, those grey cells had a number on it on most cases, that number is, of course, their temperature. We also could see how this temperature almost reached the ignition point of the door and how being this door on a different room it could get temperature, thanks to the **link** commands we used before.

After the 20th iteration, the fire unsets and until the 49th iteration, temperature will dissipate until there's nothing our empty rooms.

Finally I'm going to add some more inflammable material in order to show you a nice command to get a fire started and enjoy watching the world burn, first use the `load` command to reload the file, then, use the next commands:
```
put roomA 1 4 2
put roomA 1 5 2
put roomA 2 4 2
put roomA 2 5 2
put roomA 1 1 2
put roomA 1 2 2
put roomA 3 1 1
put roomA 4 1 1
put roomA 5 1 1
save

Iteration: 0
CODE: <doorAB>
Door that connects roomA and roomB
[ 2 ]
CODE: <roomA>
The first room that we are modeling
[ 2 ][ 2 ][   ][   ][   ]
[ 2 ][ 2 ][   ][   ][   ]
[   ][   ][   ][   ][   ]
[ 2 ][   ][   ][   ][   ]
[ 2 ][   ][ 1 ][ 1 ][ 1 ]
CODE: <roomB>
Room without description
[   ][   ][   ][   ]
[   ][   ][   ][   ]
[   ][   ][   ][   ]
command> 
```
with a bit of sythesis and imagination you can see a bed, a closet and a lot of paper on this bunch of blue numbers, whatever, just **ignite** the cell `roomA 1 5` and iterate to enjoy the spectacle.
```
ignite roomA 1 5
```

## Available commands
```
command> help
	refresh [roomID [roomID [(...)}]] -         cleans the screen and shows all created rooms, if a list of room IDs is provided, it will only show those rooms.

	iterate [n] -                               calculates the next n iterations, n defaults to 1

	build roomID w h [description] -            builds a new empty room

	set roomID x y flame ignition temperature - sets a new status to the selected cell, do not use it on a file you are going to save.

	link roomID x y roomID x y -                link two cells, intended to connect cells between rooms

	list -                                      shows all room names and descriptions

	ignite roomID x y -                         sets a cell on fire

	deflagrate roomID x y [r] -                 set a cell and it's neightbours on fire, if r is set higher than 1, it will do it recursively r times

	block roomID x y -                          makes a cell unspreadable

	unblock roomID x y -                        makes a cell spreadable

	put roomID x y ignition -                   puts an inflamable object on the selected cell, the ignition point is the passed value per 100┬║C

	clear roomID x y -                          resets a cell to the default empty state

	save [filename] -                           Saves the layout on the specified file, if no file is specified, it will save it on the last file loaded or saved

	load [filename] -                           Loads the building layout from the specified file, if no file is specified loads it from the last file red or saved

	reset -                                     Deletes all rooms and resets the iteration counter
Note that blank spaces will act as a separator.
GLOSSARY
	roomID -                                    Alphanumeric, no spaces, its the reference for a room
	flame -                                     Integer, 1 or 0, defines if a cell is on fire
	ignition -                                  Integer, if positive, sets the ignition point of a cell, if negative, defines how many iterations until the fire on that cell unsets
	temperature -                               Temperature counters of the Cell, a fire generates 500 of them each iteration, a temperature counter is like 1┬║C
	filename -                                  The name of the target file to load or save the building data

OTHER INFO
	rooms -                                     Rooms are shown as a 2D array of cells, the first cell (1,1) is the one at the bottom left
```
