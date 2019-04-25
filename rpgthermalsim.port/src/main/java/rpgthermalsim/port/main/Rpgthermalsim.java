

package rpgthermalsim.port.main;
import rpgthermalsim.port.classes.*;

public class Rpgthermalsim {

	public static void main(String[] args) {
		Building build;
		
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
				System.err.println("usage:"+System.lineSeparator()+".\rpg-thermal-sim [filename]");
				System.exit(-1);
				break;
		}
		
		return;
	}

}
