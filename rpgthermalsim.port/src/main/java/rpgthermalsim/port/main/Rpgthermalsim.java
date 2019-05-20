

package rpgthermalsim.port.main;
import java.util.logging.Level;
import java.util.logging.Logger;

import rpgthermalsim.port.classes.*;

public class Rpgthermalsim {

	public static Building build;
	
	public static void main(String[] args) {
		
		switch(args.length) {
			case 0:
				build = new Building();
				build.loop();
				break;
			case 1:
				build = new Building(args[0]);
				build.loop();
				break;
			default:
				Logger.getGlobal().log(Level.SEVERE,"usage:"+System.lineSeparator()+".\rpg-thermal-sim [filename]");
				System.exit(-1);
				break;
		}
		
		return;
	}

}
