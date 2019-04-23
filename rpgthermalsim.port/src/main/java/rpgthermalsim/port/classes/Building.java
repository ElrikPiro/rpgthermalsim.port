package rpgthermalsim.port.classes;

import java.util.ArrayList;
import java.util.Iterator;
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
				_command(line);//TODO: this throws exception if failed
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

	private void _command(String line) throws BuildingException{
		String command;
		String[] args;
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
		}
		/*
		 * 
		}else if(command=="iterate"){
			bool a = (bool) std::getline(args,command,' ');
			if(a&&std::atoi(command.c_str())>1) iterate(std::atoi(command.c_str()));
			else iterate();
			refresh(ref);
			return 0;
		}else if(command=="build"){//void newRoom(std::string ID,int w,int h,std::string desc)
			std::string ID,desc;
			int w,h;
			if(std::getline(args,command,' ')){
				ID = command;
				if( this->buildingLayout.find(command) != this->buildingLayout.end() ) return 3;
			}else return 1;

			if(std::getline(args,command,' ')){
				w = std::atoi(command.c_str());
				if( w<1 ) return 2;
			}else return 1;

			if(std::getline(args,command,' ')){
				h = std::atoi(command.c_str());
				if( h<1 ) return 2;
			}else return 1;

			if(std::getline(args,desc)){

			}else desc = "Room without description";

			newRoom(ID,w,h,desc);
			ref[ID] = this->buildingLayout[ID];
			builds.push_back(input);
			return 0;
		}else if(command=="set"){
			std::string ID;
			int w,h;
			int flame=0,ignition=0,temperature=0;

			if(std::getline(args,command,' ')){
				ID = command;
				if( this->buildingLayout.find(command) == this->buildingLayout.end() ) return 4;
			}else return 1;

			if(std::getline(args,command,' ')){
				w = std::atoi(command.c_str())-1;
				if( w<0 ) return 2;
			}else return 1;

			if(std::getline(args,command,' ')){
				h = std::atoi(command.c_str())-1;
				if( h<0 ) return 2;
			}else return 1;

			if(std::getline(args,command,' ')){
				flame = std::atoi(command.c_str());
				if( flame<0 || flame>1 ) return 2;
			}

			if(std::getline(args,command,' ')){
				ignition = std::atoi(command.c_str());
				if( flame > 0 && ignition > 0 ) return 2;
			}

			if(std::getline(args,command,' ')){
				temperature = std::atoi(command.c_str());
				if( temperature < 0 ) return 2;
			}

			setCell(ID,w,h,flame,ignition,temperature);
			return 0;
		}else if(command=="exit") return -1;
		else if(command=="link"){
			std::string ID1,ID2;
			int w1,h1,w2,h2;

			if(std::getline(args,command,' ')){
				ID1 = command;
				if( this->buildingLayout.find(command) == this->buildingLayout.end() ) return 3;
			}else return 1;

			if(std::getline(args,command,' ')){
				w1 = std::atoi(command.c_str())-1;
				if( w1<0 ) return 2;
			}else return 1;

			if(std::getline(args,command,' ')){
				h1 = std::atoi(command.c_str())-1;
				if( h1<0 ) return 2;
			}else return 1;

			if(std::getline(args,command,' ')){
				ID2 = command;
				if( this->buildingLayout.find(command) == this->buildingLayout.end() ) return 3;
			}else return 1;

			if(std::getline(args,command,' ')){
				w2 = std::atoi(command.c_str())-1;
				if( w2<0 ) return 2;
			}else return 1;

			if(std::getline(args,command,' ')){
				h2 = std::atoi(command.c_str())-1;
				if( h2<0 ) return 2;
			}else return 1;

			linkCells(ID1,w1,h1,ID2,w2,h2);
			links.push_back(input);
			return 0;
		}else if(command=="list"){
			for(auto it = this->buildingLayout.begin();it!=this->buildingLayout.end();it++){
				listRooms();
				return -2;
			}
		}else if(command=="ignite"){
			std::string ID1;
			int x,y;

			if(std::getline(args,command,' ')){
				ID1 = command;
				if( this->buildingLayout.find(command) == this->buildingLayout.end() ) return 3;
			}else return 1;

			if(std::getline(args,command,' ')){
				x = std::atoi(command.c_str())-1;
				if( x<0 ) return 2;
			}else return 1;

			if(std::getline(args,command,' ')){
				y = std::atoi(command.c_str())-1;
				if( y<0 ) return 2;
			}else return 1;

			ignite(ID1,x,y);
			return 0;
		}else if(command=="deflagrate"){
			std::string ID1;
			int x,y,r;
				if(std::getline(args,command,' ')){
				ID1 = command;
					if( this->buildingLayout.find(command) == this->buildingLayout.end() ) return 3;
				}else return 1;

				if(std::getline(args,command,' ')){
					x = std::atoi(command.c_str())-1;
					if( x<0 ) return 2;
				}else return 1;

				if(std::getline(args,command,' ')){
					y = std::atoi(command.c_str())-1;
					if( y<0 ) return 2;
				}else return 1;

				if(std::getline(args,command,' ')){
					r = std::atoi(command.c_str());
					if( r<1 ) return 2;
				}else r=1;

				deflagrate(ID1,x,y,r);
				return 0;
		}else if(command=="block"){
			std::string ID1;
			int x,y;

			if(std::getline(args,command,' ')){
				ID1 = command;
				if( this->buildingLayout.find(command) == this->buildingLayout.end() ) return 3;
			}else return 1;

			if(std::getline(args,command,' ')){
				x = std::atoi(command.c_str())-1;
				if( x<0 ) return 2;
			}else return 1;

			if(std::getline(args,command,' ')){
				y = std::atoi(command.c_str())-1;
				if( y<0 ) return 2;
			}else return 1;

			block(ID1,x,y);
			puts.push_back(input);
			return 0;
		}else if(command=="unblock"){
			std::string ID1;
			int x,y;

			if(std::getline(args,command,' ')){
				ID1 = command;
				if( this->buildingLayout.find(command) == this->buildingLayout.end() ) return 3;
			}else return 1;

			if(std::getline(args,command,' ')){
				x = std::atoi(command.c_str())-1;
				if( x<0 ) return 2;
			}else return 1;

			if(std::getline(args,command,' ')){
				y = std::atoi(command.c_str())-1;
				if( y<0 ) return 2;
			}else return 1;

			unblock(ID1,x,y);
			puts.push_back(input);
			return 0;
		}else if(command=="put"){
			std::string ID1;
			int x,y,ignition;

			if(std::getline(args,command,' ')){
				ID1 = command;
				if( this->buildingLayout.find(command) == this->buildingLayout.end() ) return 3;
			}else return 1;

			if(std::getline(args,command,' ')){
				x = std::atoi(command.c_str())-1;
				if( x<0 ) return 2;
			}else return 1;

			if(std::getline(args,command,' ')){
				y = std::atoi(command.c_str())-1;
				if( y<0 ) return 2;
			}else return 1;

			if(std::getline(args,command,' ')){
				ignition = std::atoi(command.c_str());
				if( ignition<0 ) return 2;
			}else return 1;

			setCell(ID1,x,y,0,ignition,0);
			puts.push_back(input);
			return 0;
		}else if(command=="clear"){
			std::string ID1;
			int x,y;

			if(std::getline(args,command,' ')){
				ID1 = command;
				if( this->buildingLayout.find(command) == this->buildingLayout.end() ) return 3;
			}else return 1;

			if(std::getline(args,command,' ')){
				x = std::atoi(command.c_str())-1;
				if( x<0 ) return 2;
			}else return 1;

			if(std::getline(args,command,' ')){
				y = std::atoi(command.c_str())-1;
				if( y<0 ) return 2;
			}else return 1;

			setCell(ID1,x,y,0,0,0);
			puts.push_back(input);
			return 0;
		}else if(command=="save"){
			std::string filename;
			if(std::getline(args,command,' ')){
				return save(command);
			}else return save();
		}else if(command=="load"){
			std::string filename;
			if(std::getline(args,command,' ')){
				return load(command);
			}else return load();
		}else if(command=="reset"){
			reset();
			return 0;
		}

		return 1;
		 */
		
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
			h = command();
			switch(h) {
				case 0:
					refresh(ref);
					break;
				case 1:
					help();
					break;
				default:
					System.err.printf("Error code: %d\n",h);
					switch(h) {
						case 2:
							System.err.println("wrong input");
							break;
						case 3:
							System.err.println("object already exist");
							break;
						case 4:
							System.err.println("room does not exist");
							break;
						case 5:
							System.err.println("file could not be opened");
							break;
						default:
							System.err.println("UNEXPECTED ERROR CODE");
							System.exit(-h);
							break;
					}
			}
			
		}
		
	}

	private int command() {
		// TODO Auto-generated method stub
		return 0;
	}

	private void help() {
		// TODO Auto-generated method stub
		
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
