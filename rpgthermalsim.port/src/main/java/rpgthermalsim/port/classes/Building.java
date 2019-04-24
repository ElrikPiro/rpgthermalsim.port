package rpgthermalsim.port.classes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.io.*;

public class Building {
	
	final public static char CLEAR[] = {0x1b,'[','2','J','\0'};
	public int iteration = 0;
	
	protected Layout buildingLayout;
	protected Layout ref;
	protected ArrayList<String> builds,puts,links;
	protected String file;
	
	public Building(String string) {
		int iterations = 0;
		String line = null, nil;
		boolean failed = false;
		int ret;
		
		try {
			FileReader readfile = new FileReader(string);
			BufferedReader bf = new BufferedReader(readfile);
			while(bf.ready()) {
				line = bf.readLine();
				if(line.length()==0) line = "#";
				_command(line);
				iterations++;
			}
			bf.close();
			this.file = string;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (BuildingException e) {
			e.printStackTrace();
			System.err.printf("Failed to interpret line: "+System.lineSeparator()+iterations+": "+line+System.lineSeparator());
			failed = true;
		}
		
		return;
	}

	private void _command(String line) throws BuildingException, IOException{
		String command;
		String[] args;
		String ID;
		int w,h;
		int x,y;
		Integer flame = new Integer(0);
		Integer ignition = new Integer(0);
		Integer temperature  = new Integer(0);
		
		
		args = line.split(" ");
		command = args[0];
		if(command.length()==0) {
			iterate();
			refresh(ref);
			return;
		}else if(command.charAt(0)=='#') return;
		
		switch(command) {
			case "refresh":
				ref.clear();
				for(int i = 1;i<args.length;i++) {
					if(!this.buildingLayout.containsKey(args[i])) throw new BuildingException(args[i]+" is not a valid key.");
					ref.replace(args[i], this.buildingLayout.get(args[i]));
				}
				
				if(ref.isEmpty()) ref = this.buildingLayout;
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
				if(this.buildingLayout.containsKey(ID)) throw new BuildingException("Building "+ID+" already exists.");
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
				
				newRoom(ID,w,h,desc);
				
				ref.replace(ID, this.buildingLayout.get(ID));
				builds.add(line);
				return;
			case "set":
				Integer[] values = {flame,ignition,temperature};
				
				if(args.length<4) throw new BuildingException("set command requires at least 3 parameters.");
				
				ID = args[1];
				if(!this.buildingLayout.containsKey(ID)) throw new BuildingException("Building "+ID+" does not exist.");
				
				w = Integer.parseInt(args[2]);
				h = Integer.parseInt(args[3]);
				if(w < 0 || h < 0) throw new BuildingException("pos x and y must be zero or positive numbers.");
				
				for(int i = 4;i<args.length;i++) {
					if(i>6) break;
					values[i-4] = new Integer(args[i]);
					if(i-4 == 0 && values[i-4].intValue()<0 || values[i-4].intValue()>1) throw new BuildingException("flame value must be 1 or 0.");
					if(i-5 == 0 && values[i-4].intValue()>0 && values[i-5].intValue()>0) throw new BuildingException("flame and ignition cant be positive at the same time.");
					if(i-6 == 0 && values[i-6].intValue()<0) throw new BuildingException("temperature cannot be negative.");
				}
				
				setCell(ID,w,h,values[0],values[1],values[2]);
				return;
			case "exit":
				System.exit(0);
			case "link":
				String ID1,ID2;
				int w1,h1,w2,h2;
				
				if(args.length<7) throw new BuildingException("link command requires at least 6 parameters.");
				
				ID1 = args[1];
				ID2 = args[4];
				if(!this.buildingLayout.containsKey(ID1)) throw new BuildingException("Building "+ID1+" does not exist.");
				if(!this.buildingLayout.containsKey(ID2)) throw new BuildingException("Building "+ID2+" does not exist.");
				
				w1 = Integer.parseInt(args[2]);
				h1 = Integer.parseInt(args[3]);
				w2 = Integer.parseInt(args[5]);
				h2 = Integer.parseInt(args[6]);
				if(w1 < 0 || h1 < 0 || w2 < 0 || h2 < 0) throw new BuildingException("pos x and y must be zero or positive numbers.");
				
				linkCells(ID1,w1,h1,ID2,w2,h2);
				
				links.add(line);
				return;
			case "list":
				listRooms();
				return;
			case "ignite":
				
				if(args.length<4) throw new BuildingException("ignite command requires at least 4 parameters.");
				
				ID = args[1];
				if(!this.buildingLayout.containsKey(ID)) throw new BuildingException("Building "+ID+" does not exist.");
				
				x = Integer.parseInt(args[2]); y = Integer.parseInt(args[3]);
				if(x < 0 || y < 0) throw new BuildingException("pos x and y must be zero or positive numbers.");
				
				ignite(ID,x,y);
				return;
			case "deflagrate":
				int r = 1;
				
				if(args.length<4) throw new BuildingException("deflagrate command requires at least 4 parameters.");
				
				ID = args[1];
				if(!this.buildingLayout.containsKey(ID)) throw new BuildingException("Building "+ID+" does not exist.");
				
				x = Integer.parseInt(args[2]); y = Integer.parseInt(args[3]);
				if(x < 0 || y < 0) throw new BuildingException("pos x and y must be zero or positive numbers.");
				
				if(args.length>4) {
					r = Integer.parseInt(args[4]);
					if( r < 1 ) throw new BuildingException("radius must be a non-zero positive number.");
				}
				
				deflagrate(ID,x,y,r);
				return;
			case "block":
				if(args.length<4) throw new BuildingException("block command requires at least 4 parameters.");
				
				ID = args[1];
				if(!this.buildingLayout.containsKey(ID)) throw new BuildingException("Building "+ID+" does not exist.");
				
				x = Integer.parseInt(args[2]); y = Integer.parseInt(args[3]);
				if(x < 0 || y < 0) throw new BuildingException("pos x and y must be zero or positive numbers.");
				
				block(ID,x,y);
				puts.add(line);
				return;
			case "unblock":
				if(args.length<4) throw new BuildingException("unblock command requires at least 4 parameters.");
				
				ID = args[1];
				if(!this.buildingLayout.containsKey(ID)) throw new BuildingException("Building "+ID+" does not exist.");
				
				x = Integer.parseInt(args[2]); y = Integer.parseInt(args[3]);
				if(x < 0 || y < 0) throw new BuildingException("pos x and y must be zero or positive numbers.");
				
				unblock(ID,x,y);
				puts.add(line);
				return;
			case "put":
				if(args.length<5) throw new BuildingException("put command requires at least 4 parameters.");
				
				ID = args[1];
				if(!this.buildingLayout.containsKey(ID)) throw new BuildingException("Building "+ID+" does not exist.");
				
				x = Integer.parseInt(args[2]); y = Integer.parseInt(args[3]);
				if(x < 0 || y < 0) throw new BuildingException("pos x and y must be zero or positive numbers.");
				
				ignition = new Integer(Integer.parseInt(args[4]));
				if(ignition < 0) throw new BuildingException("ignition value must be zero or positive numbers.");
				
				setCell(ID,x,y,0,ignition.intValue(),0);
				puts.add(line);
				return;
			case "clear":
				if(args.length<4) throw new BuildingException("clear command requires at least 4 parameters.");
				
				ID = args[1];
				if(!this.buildingLayout.containsKey(ID)) throw new BuildingException("Building "+ID+" does not exist.");
				
				x = Integer.parseInt(args[2]); y = Integer.parseInt(args[3]);
				if(x < 0 || y < 0) throw new BuildingException("pos x and y must be zero or positive numbers.");
				
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
			default:
				throw new BuildingException("Command "+command+" not supported.");
		}
	}

	private void reset() {
		// TODO Auto-generated method stub
		
	}

	private void load() {
		// TODO Auto-generated method stub
		
	}

	private void load(String string) {
		// TODO Auto-generated method stub
		
	}

	private void save() throws IOException{
		// TODO Auto-generated method stub
		
	}

	private void save(String string) throws IOException{
		// TODO Auto-generated method stub
		
	}

	private void unblock(String iD, int x, int y) {
		// TODO Auto-generated method stub
		
	}

	private void block(String iD, int x, int y) {
		// TODO Auto-generated method stub
		
	}

	private void deflagrate(String iD, int x, int y, int r) {
		// TODO Auto-generated method stub
		
	}

	private void ignite(String iD, int x, int y) {
		// TODO Auto-generated method stub
		
	}

	private void listRooms() {
		// TODO Auto-generated method stub
		
	}

	private void linkCells(String iD1, int w1, int h1, String iD2, int w2, int h2) {
		// TODO Auto-generated method stub
		
	}

	private void setCell(String iD, int w, int h, Integer integer, Integer integer2, Integer integer3) {
		// TODO Auto-generated method stub
		
	}

	private void newRoom(String iD, int w, int h, String desc) {
		// TODO Auto-generated method stub
		
	}

	private void iterate(int parseInt) {
		// TODO Auto-generated method stub
		
	}

	private void iterate() {
		// TODO Auto-generated method stub
		
	}

	/*
	 * Do nothing
	 */
	public Building() {}

	public void loop() {
		refresh(ref);
		int h = 0;
		while(h!=-1) {
			command();
		}
			
	}

	private void command() {
		String input;
		System.out.print("command> ");
		Scanner teclado = new Scanner(System.in);
		input = teclado.nextLine();
		try {
			_command(input);
		} catch (BuildingException e) {
			e.printStackTrace();
			help();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

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
				"\tblock roomID x y -                          makes a cell unspreadable"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\tunblock roomID x y -                        makes a cell spreadable"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\tput roomID x y ignition -                   puts an inflamable object on the selected cell, the ignition point is the passed value per 100ºC"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\tclear roomID x y -                          resets a cell to the default empty state"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\tsave [filename] -                           Saves the layout on the specified file, if no file is specified, it will save it on the last file loaded or saved"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\tload [filename] -                           Loads the building layout from the specified file, if no file is specified loads it from the last file red or saved"+System.lineSeparator()+
				""+System.lineSeparator()+
				"\treset -                                     Deletes all rooms and resets the iteration counter"+System.lineSeparator()+
				"Note that blank spaces will act as a separator."+System.lineSeparator()+
				"GLOSSARY"+System.lineSeparator()+
				"\troomID -                                    Alphanumeric, no spaces, its the reference for a room"+System.lineSeparator()+
				"\tflame -                                     Integer, 1 or 0, defines if a cell is on fire"+System.lineSeparator()+
				"\tignition -                                  Integer, if positive, sets the ignition point of a cell, if negative, defines how many iterations until the fire on that cell unsets"+System.lineSeparator()+
				"\ttemperature -                               Temperature counters of the Cell, a fire generates 500 of them each iteration, a temperature counter is like 1ºC"+System.lineSeparator()+
				"\tfilename -                                  The name of the target file to load or save the building data"+System.lineSeparator()+
				""+System.lineSeparator()+
				"OTHER INFO"+System.lineSeparator()+
				"\trooms -                                     Rooms are shown as a 2D array of cells, the first cell (1,1) is the one at the bottom left"+System.lineSeparator()
				);
		
	}

	private void refresh(Layout ref2) {
		System.out.print(CLEAR);
		System.out.println("Iteration: "+this.iteration);
		Iterator<String> keys = ref2.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			System.out.print("CODE: <" + key + ">" + System.lineSeparator() + ref2.get(key).toString());
		}
		
	}

}
