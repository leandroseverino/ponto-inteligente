package br.com.maxigenios.pontointeligente.api.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtilsTest {

	private static final String PWD = "123456";
	private final BCryptPasswordEncoder bCryptEnconder = new BCryptPasswordEncoder();
	
	@Test
	public void testSenhaNula() throws Exception {
		assertNull(PasswordUtils.generateBCrypt(null));
	}

	@Test
	public void testGenerateBCrypt() {		
		String hash = PasswordUtils.generateBCrypt(PWD);
		
		assertTrue(bCryptEnconder.matches(PWD, hash));
	}

}
