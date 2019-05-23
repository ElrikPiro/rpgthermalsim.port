package rpgthermalsim.port.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import rpgthermalsim.port.exceptions.BuildingException;
import rpgthermalsim.port.exceptions.RoomException;

import java.io.*;
import java.security.NoSuchAlgorithmException;


/**
 * 
 * Builder and loop class for managing the data structure of the instance.
 * 
 * @author David Baselga
 * @since 0.1
 */
@RestController
public class Building implements Digestable{
	
	final protected static char CLEAR[] = {0x1b,'[','2','J','\0'};
	protected int iteration = 0;
	
	protected Layout buildingLayout = new Layout();
	protected Layout ref = new Layout();
	protected ArrayList<String> builds = new ArrayList<String>();;
	protected ArrayList<String> puts = new ArrayList<String>();
	protected ArrayList<String> links = new ArrayList<String>();
	protected String file;
	private Scanner teclado = new Scanner(System.in);
	
	/**
	 * Default Class constructor
	 * 
	 * @author David Baselga
	 * @since 0.1
	 */
	public Building() {
		file = null;
	}
	
	/**
	 * Class constructor that reads commands from a file, if the file has errors, those will be shown on
	 * stdout
	 * 
	 * @param file File to load the building from.
	 * @author David Baselga
	 * @since 0.1
	 */
	public Building(String string) {
		int iterations = 0;
		String line = null;
		
		try (BufferedReader bf = 
				new BufferedReader(new FileReader(string))  ) {
			while(bf.ready()) {
				line = bf.readLine();
				if(line.length()==0) line = "#";
				_command(line);
				iterations++;
			}
			this.file = string;
		} catch (FileNotFoundException e) {
			Logger.getGlobal().log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
			System.exit(-1);
		} catch (IOException e) {
			Logger.getGlobal().log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
			System.exit(-1);
		} catch (BuildingException e) {
			Logger.getGlobal().log(Level.FINER, Arrays.toString(e.getStackTrace()));
			Logger.getGlobal().log(Level.WARNING, "Failed to interpret line: "+System.lineSeparator()+iterations+": "+line+System.lineSeparator());
		} catch (RoomException e) {
			Logger.getGlobal().log(Level.FINER, Arrays.toString(e.getStackTrace()));
			Logger.getGlobal().log(Level.SEVERE,"Room exception at line: "+System.lineSeparator()+iterations+": "+line+System.lineSeparator());
		}
		
		return;
	}

	/**
	 * Runs the passed parameter as a command
	 * 
	 * @param line String containing the parameter to parse and run.
	 * @throws BuildngException When the input is not properly formatted
	 * @throws RoomExceptien When tries to access a cell that does not exists
	 * @author David Baselga
	 * @since 0.1
	 */
	private void _command(String line) throws BuildingException, IOException, RoomException{
		String command;
		String[] args;
		String ID;
		int w,h;
		int x,y;
		int ignition = 0;
		Iterator<Cell> it;
		Cell c;
		float istn = 0;
		
		args = line.split(" ");
		command = args[0];
		if(command.length()==0) {
			iterate();
			refresh(ref);
			return;
		}else if(command.charAt(0)=='#') return;
		
		switch(command) {
			case "help":
				help();
				return;
			case "refresh":
				ref.clear();
				for(int i = 1;i<args.length;i++) {
					if(!this.buildingLayout.containsKey(args[i])) throw new BuildingException(args[i]+" is not a valid key.");
					ref.put(args[i], this.buildingLayout.get(args[i]));
				}
				
				if(ref.isEmpty()) ref = (Layout) this.buildingLayout.clone();
				refresh(ref);
				
				return;
			case "iterate":
				if(args.length>1) {
					iterate(Integer.parseInt(args[1]));
				}else iterate();
				refresh(ref);
				return;
			case "build":
				String desc;
				if(args.length<4) throw new BuildingException("Build command requires at least 3 parameters.");
				ID = args[1];
				if(this.buildingLayout.containsKey(ID)) BuildingException.putError(1,ID);
				w = Integer.parseInt(args[2]);
				h = Integer.parseInt(args[3]);
				if(w < 1 || h < 1) throw new BuildingException("witdh and height must be non-zero positive numbers.");
				if(args.length>4) {
					desc = "";
					for(int i = 4; i<args.length;i++)
						desc = desc+" "+args[i];
					desc = desc.trim();
				}
				else desc = "Room without description";
				
				newRoom(ID, w, h, desc);
				
				ref.put(ID, this.buildingLayout.get(ID));
				builds.add(line);
				return;
			case "set":
				int[] values = new int[3];
				
				if(args.length<4) throw new BuildingException("set command requires at least 3 parameters.");
				
				ID = args[1];
				if(!this.buildingLayout.containsKey(ID)) BuildingException.putError(2, ID);
				
				w = Integer.parseInt(args[2]);
				h = Integer.parseInt(args[3]);
				if(w < 0 || h < 0) BuildingException.putError(3,null);
				
				for(int i = 4;i<args.length;i++) {
					if(i>6) break;
					values[i-4] = Integer.parseInt(args[i]);
					if(i-4 == 0 && (values[i-4]<0 || values[i-4]>1) ) throw new BuildingException("flame value must be 1 or 0.");
					if(i-5 == 0 && values[i-4]>0 && values[i-5]>0) throw new BuildingException("flame and ignition cant be positive at the same time.");
					if(i-6 == 0 && values[i-6]<0) throw new BuildingException("temperature cannot be negative.");
				}
				
				setCell(ID,w,h,values[0],values[1],values[2]);
				return;
			case "exit":
				throw new IOException("Exit requested.");
			case "link":
				String ID1,ID2;
				int w1,h1,w2,h2;
				
				if(args.length<7) throw new BuildingException("link command requires at least 6 parameters.");
				
				ID1 = args[1];
				ID2 = args[4];
				if(!this.buildingLayout.containsKey(ID1)) BuildingException.putError(2, ID1);
				if(!this.buildingLayout.containsKey(ID2)) BuildingException.putError(2, ID2);
				
				w1 = Integer.parseInt(args[2]);
				h1 = Integer.parseInt(args[3]);
				w2 = Integer.parseInt(args[5]);
				h2 = Integer.parseInt(args[6]);
				if(w1 < 0 || h1 < 0 || w2 < 0 || h2 < 0) BuildingException.putError(3,null);
				
				linkCells(ID1,w1,h1,ID2,w2,h2);
				
				links.add(line);
				return;
			case "list":
				listRooms();
				return;
			case "ignite":
				
				if(args.length<4) throw new BuildingException("ignite command requires at least 4 parameters.");
				
				ID = args[1];
				if(!this.buildingLayout.containsKey(ID)) BuildingException.putError(2, ID);
				
				x = Integer.parseInt(args[2]); y = Integer.parseInt(args[3]);
				if(x < 0 || y < 0) BuildingException.putError(3,null);
				
				ignite(ID,x,y);
				return;
			case "deflagrate":
				int r = 1;
				
				if(args.length<4) throw new BuildingException("deflagrate command requires at least 4 parameters.");
				
				ID = args[1];
				if(!this.buildingLayout.containsKey(ID)) BuildingException.putError(2, ID);
				
				x = Integer.parseInt(args[2]); y = Integer.parseInt(args[3]);
				if(x < 0 || y < 0) BuildingException.putError(3,null);
				
				if(args.length>4) {
					r = Integer.parseInt(args[4]);
					if( r < 1 ) throw new BuildingException("radius must be a non-zero positive number.");
				}
				
				deflagrate(ID,x,y,r);
				return;
			case "block":
				if(args.length<4) throw new BuildingException("block command requires at least 4 parameters.");
				
				ID = args[1];
				if(!this.buildingLayout.containsKey(ID)) BuildingException.putError(2, ID);
				
				x = Integer.parseInt(args[2]); y = Integer.parseInt(args[3]);
				if(x < 0 || y < 0) BuildingException.putError(3,null);
				
				if(args.length>4) {
					istn = Float.parseFloat(args[4]);
					if(istn >1.0f || istn<0.0f) throw new BuildingException("insulation must be between 0 and 1.");
				}
				
				block(ID,x,y,istn);
				puts.add(line);
				return;
			case "unblock":
				if(args.length<4) throw new BuildingException("unblock command requires at least 4 parameters.");
				
				ID = args[1];
				if(!this.buildingLayout.containsKey(ID)) BuildingException.putError(2, ID);
				
				x = Integer.parseInt(args[2]); y = Integer.parseInt(args[3]);
				if(x < 0 || y < 0) BuildingException.putError(3,null);
				
				unblock(ID,x,y);
				puts.add(line);
				return;
			case "put":
				if(args.length<5) throw new BuildingException("put command requires at least 4 parameters.");
				
				ID = args[1];
				if(!this.buildingLayout.containsKey(ID)) BuildingException.putError(2, ID);
				
				x = Integer.parseInt(args[2]); y = Integer.parseInt(args[3]);
				if(x < 0 || y < 0) BuildingException.putError(3,null);
				
				ignition = Integer.parseInt(args[4]);
				if(ignition < 0) throw new BuildingException("ignition value must be zero or positive numbers.");
				
				if(args.length>5) {
					istn = Float.parseFloat(args[5]);
					if(istn >1.0f || istn<0.0f) throw new BuildingException("insulation must be between 0 and 1.");
					block(ID,x,y,istn);
				}
				
				setCell(ID,x,y,0,ignition,0);
				puts.add(line);
				return;
			case "clear":
				if(args.length<4) throw new BuildingException("clear command requires at least 4 parameters.");
				
				ID = args[1];
				if(!this.buildingLayout.containsKey(ID)) BuildingException.putError(2, ID);
				
				x = Integer.parseInt(args[2]); y = Integer.parseInt(args[3]);
				if(x < 0 || y < 0) BuildingException.putError(3,null);
				
				setCell(ID,x,y,0,0,0);
				puts.add(line);
				return;
			case "save":
				if(args.length>1) save(args[1]);
				else save();
				return;
			case "load":
				if(args.length>1) load(args[1]);
				else load();
				return;
			case "reset":
				reset();
				return;
			case "sink":
				if(args.length<5) throw new BuildingException("sink command requires at least 5 parameters.");
				
				ID = args[1];
				if(!this.buildingLayout.containsKey(ID)) BuildingException.putError(2, ID);
				
				x = Integer.parseInt(args[2]); y = Integer.parseInt(args[3]);
				if(x < 0 || y < 0) BuildingException.putError(3,null);
				
				c = new FixedTempCell(Integer.parseInt(args[4]));
				this.buildingLayout.get(ID).layout.add(c);
				this.buildingLayout.get(ID).getCellXY(x, y).linkCells(c);
				
				puts.add(line);
				return;
			case "unsink":
				if(args.length<4) throw new BuildingException("unsink command requires at least 4 parameters.");
				
				ID = args[1];
				if(!this.buildingLayout.containsKey(ID)) BuildingException.putError(2, ID);
				
				x = Integer.parseInt(args[2]); y = Integer.parseInt(args[3]);
				if(x < 0 || y < 0) BuildingException.putError(3,null);
				
				it = this.buildingLayout.get(ID).getCellXY(x, y).getNeightbourhood().iterator();
				
				while(it.hasNext()) {
					c = it.next();
					if(c.getClass() == FixedTempCell.class) {
						this.buildingLayout.get(ID).getCellXY(x, y).getNeightbourhood().remove(c);
						this.buildingLayout.get(ID).layout.remove(c);
						break;
					}
				}
				puts.add(line);
				return;
			default:
				throw new BuildingException("Command "+command+" not supported.");
		}
	}

	/**
	 * Resets the object to an empty state and calls the garbage collector.
	 * 
	 * @author David Baselga
	 * @since 0.1
	 */
	private void reset() {
		buildingLayout.clear();
		ref.clear();
		builds.clear();
		puts.clear();
		links.clear();
		iteration = 0;
		System.gc();
	}

	/**
	 * Reloads a previously saved building from a file
	 * 
	 * @throws IOException file does not exist or building never set a file before.
	 * @author David Baselga
	 * @since 0.1
	 */
	private void load() throws IOException {
		load(file);
	}

	/**
	 * Loads a building from a file
	 * 
	 * @param string File to load the building from.
	 * @throws IOException file does not exist.
	 * @author David Baselga
	 * @since 0.1
	 */
	private void load(String string) throws IOException {
		FileReader readfile = new FileReader(string);
		BufferedReader bf = new BufferedReader(readfile);
		int iterations = 1;
		String line;
		
		reset();
		while(bf.ready()) {
			line = bf.readLine();
			if(line.isEmpty()) line = "#";
			try {
				_command(line);
			} catch (BuildingException e) {
				// TODO Auto-generated catch block
				Logger.getGlobal().log(Level.FINER, Arrays.toString(e.getStackTrace()));
				Logger.getGlobal().log(Level.WARNING,"Failed to interpret line: "+System.lineSeparator()+iterations+": "+line+System.lineSeparator());
				bf.close();
				return;
			} catch (RoomException e) {
				Logger.getGlobal().log(Level.FINER, Arrays.toString(e.getStackTrace()));
				Logger.getGlobal().log(Level.SEVERE,"Room exception at line: "+System.lineSeparator()+iterations+": "+line+System.lineSeparator());
			}
			iterations++;
		}
		
		bf.close();
		this.file = string;
		return;
	}

	/**
	 * Saves a building into a file
	 * 
	 * @throws IOException file does not exist or never specified before.
	 * @author David Baselga
	 * @since 0.1
	 */
	private void save() throws IOException{
		save(file);
	}

	/**
	 * Saves a building into a file
	 * 
	 * @param string File to save the building into.
	 * @throws IOException can't write into file.
	 * @author David Baselga
	 * @since 0.1
	 */
	@SuppressWarnings("unchecked")
	private void save(String string) throws IOException{
		FileWriter fw = new FileWriter(string);
		
		Iterator<String> b = builds.iterator(), p = puts.iterator(), l = links.iterator();
		@SuppressWarnings("rawtypes")
		Iterator[] ita = {b,p,l};
		Iterator<String> it;
		
		for( int i = 0; i < ita.length ; i++ ) {
			it = ita[i];
			while(it.hasNext()) {
				fw.write(it.next());
				fw.write(System.lineSeparator());
				fw.flush();
			}
		}
		
		fw.close();
		file = string;
		return;
	}

	/**
	 * Set a cell as reachable
	 * 
	 * @param iD Room Id.
	 * @param x Cell x position
	 * @param y Cell y position
	 * @throws RoomException Referenced cell does not exist.
	 * @author David Baselga
	 * @since 0.1
	 */
	private void unblock(String iD, int x, int y) throws RoomException {
		Cell c = buildingLayout.get(iD).getCellXY(x, y);
		c.setReachable();
	}

	/**
	 * Set a cell as unreachable and gives it a insulation value
	 * 
	 * @param iD Room Id.
	 * @param x Cell x position
	 * @param y Cell y position
	 * @param istn Insulation value 0.0 means totally insulated, 1.0 means no insulation at all.
	 * @throws RoomException Referenced cell does not exist.
	 * @author David Baselga
	 * @since 1.1
	 */
	private void block(String iD, int x, int y, float istn) throws RoomException {
		Cell c = buildingLayout.get(iD).getCellXY(x, y);
		c.setUnreachable(istn);
	}

	/**
	 * Simulates a gas deflagration iterating recursively within all cell neighbours
	 * 
	 * @param iD Room Id.
	 * @param x Cell x position
	 * @param y Cell y position
	 * @param r number of recursive iterations
	 * @throws RoomException Referenced cell does not exist.
	 * @author David Baselga
	 * @since 0.1
	 */
	private void deflagrate(String iD, int x, int y, int r) throws RoomException {
		Cell c = buildingLayout.get(iD).getCellXY(x, y);
		_ignite(c);
		_deflagrate(c,r);
	}

	/**
	 * Recursive algorithm to perform the deflagrate command. Related to {@link #deflagrate(String, int, int, int)}
	 * 
	 * @param c Cell to deflagrate
	 * @param r number of recursive iterations
	 * @author David Baselga
	 * @since 0.1
	 */
	private void _deflagrate(Cell c, int r) {
		Set<Cell> nhood = c.getNeightbourhood();
		Iterator<Cell> it = nhood.iterator();
		Cell d;
		while(it.hasNext()) {
			d = it.next();
			if(d.isSpreadable()) _ignite(d);
			if(d.isSpreadable() && r>1) _deflagrate(d,r-1);
		}
	}

	/**
	 * Ignites a cell
	 * 
	 * @param iD Room Id.
	 * @param x Cell x position
	 * @param y Cell y position
	 * @throws RoomException Referenced cell does not exist.
	 * @author David Baselga
	 * @since 0.1
	 */
	private void ignite(String iD, int x, int y) throws RoomException {
		Cell c = buildingLayout.get(iD).getCellXY(x, y);
		_ignite(c);
	}

	/**
	 * Ignites a cell
	 * 
	 * @param c Cell to ignite
	 * @author David Baselga
	 * @since 0.1
	 */
	private void _ignite(Cell c) {
		c.ignite();
	}

	/**
	 * Lists all created rooms
	 * 
	 * @author David Baselga
	 * @since 0.1
	 */
	private void listRooms() {
		String name;
		Room r;
		Iterator<String> it = buildingLayout.keySet().iterator();
		while(it.hasNext()) {
			name = it.next();
			r = buildingLayout.get(name);
			System.out.println("<"+name+">: "+System.lineSeparator()+r.getDesc());
		}
	}

	/**
	 * Links two cells in order to share temperature
	 * 
	 * @param iD1 Room Id for first cell.
	 * @param w1 first Cell x position
	 * @param h1 first Cell y position
	 * @param iD2 Room Id for second cell.
	 * @param w2 second Cell x position
	 * @param h2 second Cell y position
	 * @throws RoomException Referenced cell does not exist.
	 * @author David Baselga
	 * @since 0.1
	 */
	private void linkCells(String iD1, int w1, int h1, String iD2, int w2, int h2) throws RoomException {
		buildingLayout.get(iD1).getCellXY(w1, h1).linkCells(buildingLayout.get(iD2).getCellXY(w2, h2));
	}

	/**
	 * Forces to set the status of a cell
	 * 
	 * @param iD1 Room Id for first cell.
	 * @param w1 first Cell x position
	 * @param h1 first Cell y position
	 * @param integer1 Value for flame status.
	 * @param integer2 Value for ignition status.
	 * @param integer3 Value for temperature.
	 * @throws RoomException Referenced cell does not exist.
	 * @author David Baselga
	 * @since 0.1
	 */
	private void setCell(String iD, int w, int h, Integer integer, Integer integer2, Integer integer3) throws RoomException {
		buildingLayout.get(iD).getCellXY(w,h).setStatus(integer.intValue(),integer2.intValue(),integer3.intValue());
	}

	/**
	 * Creates a new Room and adds it to the building layout.
	 * 
	 * @param iD1 Room Id for first cell.
	 * @param w1 first Cell x position
	 * @param h1 first Cell y position
	 * @param iD2 Room Id for second cell.
	 * @param w2 second Cell x position
	 * @param h2 second Cell y position
	 * @throws RoomException Referenced cell does not exist.
	 * @author David Baselga
	 * @since 0.1
	 */
	private void newRoom(String iD, int w, int h, String desc) {
		Room r = new Room(w,h,desc);
		buildingLayout.put(iD, r);
	}

	/**
	 * Iterates n times, having the cells from every room calculate it's statuses.
	 * 
	 * @param n number of iterations executed.
	 * @author David Baselga
	 * @since 0.1
	 */
	private void iterate(int parseInt) {
		Set<String> lay = buildingLayout.keySet();
		Iterator<String> it;
		
		for(int i = 0;i<parseInt;i++) {
			this.iteration++;
			it = lay.iterator();
			while(it.hasNext()) {
				buildingLayout.get(it.next()).iterate();
			}
		}
	}

	/**
	 * Equivalent to {@link #iterate(int)}
	 * 
	 * @author David Baselga
	 * @since 0.1
	 */
	private void iterate() {
		iterate(1);
	}

	/**
	 * Runs an input loop that executes {@link #command()} until it returns an exit code.
	 * 
	 * @author David Baselga
	 * @since 0.1
	 */
	public void loop() {
		refresh(ref);
		int h = 0;
		while(h!=-1) {
			h = command();
		}
			
	}

	/**
	 * Prompts an input line from System.in, passes it to {@link #_command(String)} and handles thrown exceptions.
	 * 
	 * @author David Baselga
	 * @since 0.1
	 * @return 0 if it must continue operating, -1 if it reaches an exit condition.
	 */
	private int command() {
		String input;
		System.out.print("command> ");
		input = teclado.nextLine();
		try {
			_command(input);
		} catch (BuildingException e) {
			Logger.getGlobal().log(Level.WARNING, Arrays.toString(e.getStackTrace()));
			help();
		} catch (IOException e) {
			Logger.getGlobal().log(Level.FINER, Arrays.toString(e.getStackTrace()));
			return -1;
		} catch (RoomException e) {
			Logger.getGlobal().log(Level.WARNING, Arrays.toString(e.getStackTrace()));
		}
		return 0;
	}

	/**
	 * Prints the command help on stdout
	 * 
	 * @author David Baselga
	 * @since 0.1
	 */
	private void help() {
		System.out.println(
				"\trefresh [roomID [roomID [(...)}]] -         cleans the screen and shows all created rooms, "+
				"if a list of room IDs is provided, it will only show those rooms."+System.lineSeparator()+
				""+System.lineSeparator()+
				"\titerate [n] -                               calculates the next n iterations, n defaults to 1"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\tbuild roomID w h [description] -            builds a new empty room"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\tset roomID x y flame ignition temperature - sets a new status to the selected "+
				"cell, do not use it on a file you are going to save."+System.lineSeparator()+
				""+System.lineSeparator()+
				"\tlink roomID x y roomID x y -                link two cells, intended to connect cells between rooms"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\tlist -                                      shows all room names and descriptions"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\tignite roomID x y -                         sets a cell on fire"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\tdeflagrate roomID x y [r] -                 set a cell and it's neightbours on fire, "+
				"if r is set higher than 1, it will do it recursively r times"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\tblock roomID x y [insulation] -             makes a cell unspreadable or insulated"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\tunblock roomID x y -                        makes a cell spreadable"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\tput roomID x y ignition [insulation] -      puts an inflamable object on the selected cell, the ignition point is the passed value per 100ºC"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\tclear roomID x y -                          resets a cell to the default empty state"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\tsave [filename] -                           Saves the layout on the specified file, if no file is specified, it will save it on the last file loaded or saved"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\tload [filename] -                           Loads the building layout from the specified file, if no file is specified loads it from the last file red or saved"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\treset -                                     Deletes all rooms and resets the iteration counter"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\tsink roomID x y temperature -               Creates a fixed temperature sink and links it to given cell"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\tunsink roomID x y -                         Removes fixed temperature sink from the given cell"+System.lineSeparator()+
				"Note that blank spaces will act as a separator."+System.lineSeparator()+
				"GLOSSARY"+System.lineSeparator()+
				"\troomID -                                    Alphanumeric, no spaces, its the reference for a room"+System.lineSeparator()+
				"\tflame -                                     Integer, 1 or 0, defines if a cell is on fire"+System.lineSeparator()+
				"\tignition -                                  Integer, if positive, sets the ignition point of a cell, if negative, defines how many iterations until the fire on that cell unsets"+System.lineSeparator()+
				"\ttemperature -                               Temperature counters of the Cell, a fire generates 500 of them each iteration, a temperature counter is like 1ºC"+System.lineSeparator()+
				"\tfilename -                                  The name of the target file to load or save the building data"+System.lineSeparator()+
				"\tinsulation -                                Sets how much can the cell's temperature have an effect on it's environment or itself"+System.lineSeparator()+
				""+System.lineSeparator()+
				"OTHER INFO"+System.lineSeparator()+
				"\trooms -                                     Rooms are shown as a 2D array of cells, the first cell (1,1) is the one at the bottom left"+System.lineSeparator()
				);
		
	}

	/**
	 * Renders the list of rooms passed by parameter.
	 * 
	 * @param ref2 {@link Layout} containing the set of rooms to render.
	 * @author David Baselga
	 * @returns String Render result.
	 * @since 0.1
	 */
	private String refresh(Layout ref2) {
		System.out.print(CLEAR);
		StringBuilder oss = new StringBuilder();
		JSONObject json = new JSONObject();
		
		json.put("iteration", this.iteration);
		oss.append("Iteration: "+this.iteration+System.lineSeparator());
		
		Iterator<String> keys = ref2.keySet().iterator();
		json.put("rooms", new JSONArray());
		while(keys.hasNext()) {
			String key = keys.next();
			json.accumulate("rooms", ref2.get(key).toJSON(key));
			oss.append("CODE:<" + key + ">" + System.lineSeparator() + 
					ref2.get(key).toString() + System.lineSeparator());
		}
		
		System.out.print(oss.toString());
		return json.toString();
	}

	/**
	 * @author David Baselga
	 * @since 1.1
	 */
	@Override
	public String digest() throws NoSuchAlgorithmException {
		StringBuilder oss = new StringBuilder();
		Iterator<String> keys;
		String key;
		oss.append(iteration);
		
		keys = buildingLayout.keySet().iterator();
		while(keys.hasNext()) {
			key = keys.next();
			oss.append(buildingLayout.get(key).digest());
		}
		
		keys = ref.keySet().iterator();
		while(keys.hasNext()) {
			key = keys.next();
			oss.append(ref.get(key).digest());
		}
		
		for(int i = 0;i<builds.size();i++) {
			oss.append(builds.get(i));
		}
		
		for(int i = 0;i<puts.size();i++) {
			oss.append(puts.get(i));
		}
		
		for(int i = 0;i<links.size();i++) {
			oss.append(links.get(i));
		}
		
		oss.append(file);
		
		return digest(oss.toString());
	}
	
	/**
	 * 
	 * Runs the passed command and returns the render results
	 * 
	 * @param line the command to execute
	 * @returns the render result
	 */
	@CrossOrigin(origins = "*")
	@GetMapping("/thermalSim")
	public String RESThandler(@RequestParam(value="command", defaultValue="#") String line) {
		try {
			_command(line);
		} catch (BuildingException | IOException | RoomException e) {
			Logger.getGlobal().log(Level.FINER, Arrays.toString(e.getStackTrace()));
			return "ERROR: "+e.getMessage();
		}
		
		return refresh(ref);
	}

}
