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
		String line, nil;
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
		} catch (Exception e) {//TODO: change by specific exception of command
			/*
			    std::cout << "Failed to interpret line: \n" << iterations << ": " << line << std::endl;
				std::getline(std::cin,nil);
				failed = true;
			 */
		}
		
		return;
	}

	private void _command(String line) {
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
