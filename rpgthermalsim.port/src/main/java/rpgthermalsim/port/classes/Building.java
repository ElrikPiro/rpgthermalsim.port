package rpgthermalsim.port.classes;

import java.util.ArrayList;
import java.io.*;

public class Building {
	
	final public static char CLEAR[] = {0x1b,'[','2','J','\0'};
	public static int iteration = 0;
	
	protected Layout buildingLayout;
	protected Layout refs;
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
		// TODO Auto-generated method stub
		
	}

}
