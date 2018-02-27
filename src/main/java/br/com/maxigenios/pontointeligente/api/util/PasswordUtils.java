package br.com.maxigenios.pontointeligente.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtils {

	private static final Logger log = LoggerFactory.getLogger(PasswordUtils.class);

	public PasswordUtils() {
	}

	/**
	 * Generate a hash with BCrypt.
	 * 
	 * @param password
	 * @return String
	 */
	public static String generateBCrypt(String password) {
		if (password == null) {
			return password;
		}

		log.info("Generating the hash with the BCrypt.");
		BCryptPasswordEncoder bCryptEncoder = new BCryptPasswordEncoder();
		return bCryptEncoder.encode(password);
	}

}
