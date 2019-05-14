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
	
	/**
	 * 
	 * Method to be implemented on inheriting classes, takes the relevant variables, mounting them on a string
	 *  representing the status of the object and calls {@link #digest(String)}
	 * 
	 * @return A SHA-1 hash representing the status of the object.
	 * @throws NoSuchAlgorithmException
	 */
	public String digest() throws NoSuchAlgorithmException;
	
	/**
	 * 
	 * Encodes a String object into a SHA-1 hash.
	 * 
	 * @param str The string to encode
	 * @return A SHA-1 hash encoding the parameter str.
	 * @throws NoSuchAlgorithmException
	 */
	public default String digest(String str) throws NoSuchAlgorithmException {
		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		return DigestUtils.sha1Hex(sha.digest(str.getBytes()));
	}
}
