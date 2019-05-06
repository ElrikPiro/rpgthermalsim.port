package rpgthermalsim.port.classes;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.digest.DigestUtils;

public interface Digestable {
	
	public String digest() throws NoSuchAlgorithmException;
	
	public default String digest(String str) throws NoSuchAlgorithmException {
		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		return DigestUtils.sha1Hex(sha.digest(str.getBytes()));
	}
}
