package rpgthermalsim.port.classes;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.digest.DigestUtils;


/**
 * 
 * Classes implementing this interface can generate a SHA-1 hash based on it's status, 
 * useful to compare if two objects are in the same state.
 * 
 * @author David Baselga
 * @since 1.1
 *
 */
public interface Digestable {
	
	public String digest() throws NoSuchAlgorithmException;
	
	public default String digest(String str) throws NoSuchAlgorithmException {
		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		return DigestUtils.sha1Hex(sha.digest(str.getBytes()));
	}
}
