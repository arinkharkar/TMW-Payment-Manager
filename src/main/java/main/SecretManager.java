package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

public class SecretManager {
	
	private String password;
	private String filePath;
		
	private boolean decryptSecrets(String password) {
		
		
		return true;
	}
		
	
	/**
	 * @param password - the password used for this secret file
	 * @param filePath - the file this SecretManager manages
	 */
	public SecretManager(String password, String filePath) {
		this.password = password;
		this.filePath = filePath;
	}
	
	public void encryptFile(String path) throws FileNotFoundException {
		
		// Create the file object
		File file;
		try {
		file = new File(path);
		
		// make sure the path is valid and we have read permissions
		} catch(NullPointerException e) {
			throw new FileNotFoundException(String.format("Error, invalid path: %s! (SecretManager.java)", path));
		}
		if (!file.canRead()) {
			throw new FileNotFoundException(String.format("Error, invalid path: %s! (SecretManager.java)", path));
		}
		
		byte[] content;
		
		// Read all the bytes from the file
		try {
			content = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			throw new FileNotFoundException(String.format("Error, cannot read from file: %s! (SecretManager.java)", path));
	
		}
		
		// TODO: encryption
		
			
	}
	
}
