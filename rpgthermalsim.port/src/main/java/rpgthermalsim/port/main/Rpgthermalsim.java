

package rpgthermalsim.port.main;
import rpgthermalsim.port.classes.*;

public class Rpgthermalsim {

	public static void main(String[] args) {
		Building build;
		
		switch(args.length) {
			case 1:
				build = new Building();
				build.loop();
				break;
			case 2:
				build = new Building(args[1]);
				build.loop();
				break;
			default:
				System.err.println("usage:\\nrpg-thermal-sim [filename]");
				System.exit(-1);
				break;
		}
		
		return;
	}

}
