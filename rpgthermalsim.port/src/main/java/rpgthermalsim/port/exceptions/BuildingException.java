package rpgthermalsim.port.exceptions;

public class BuildingException extends Exception {

	public BuildingException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
	
	public static void putError(int code, String premsg) throws BuildingException {
		String message = "";
		switch(code) {
		case 1://already exists
			message = "Building "+premsg+" already exists";
			break;
		case 2:
			message = "Building "+premsg+" does not exists";
			break;
		case 3:
			message = "pos x and y must be zero or positive numbers.";
			break;
		default:
			message = premsg;
			break;
		}
		throw new BuildingException(message);
	}

}
