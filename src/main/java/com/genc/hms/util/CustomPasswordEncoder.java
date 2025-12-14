package com.genc.hms.util;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for secure password hashing and verification using the jBCrypt
 * library. This component is intended to be used by services (e.g.,
 * UserService) for authentication purposes.
 */
@Component
public class CustomPasswordEncoder implements PasswordEncoder {

	/**
	 * Defines the computational complexity (cost factor) for the BCrypt algorithm.
	 * A higher value increases security but decreases performance. 10 is a common
	 * default.
	 */
	private static final int LOG_ROUNDS = 10;

	/**
	 * Hashes the raw (plain-text) password using BCrypt with a randomly generated
	 * salt. The generated hash includes the salt and the cost factor. * @param
	 * rawPassword The clear-text password provided by the user.
	 * 
	 * @return The securely hashed password string.
	 */
	public String encode(CharSequence rawPassword) {
		// Generate a salt using the defined log rounds
		String salt = BCrypt.gensalt(LOG_ROUNDS);

		// Hash the password with the generated salt
		return BCrypt.hashpw(rawPassword.toString(), salt);
	}

	/**
	 * Verifies a raw (plain-text) password against a previously encoded (hashed)
	 * password. BCrypt handles extracting the salt and cost factor from the encoded
	 * string automatically. * @param rawPassword The clear-text password input for
	 * login/verification.
	 * 
	 * @param encodedPassword The stored, hashed password.
	 * @return true if the raw password matches the encoded password, false
	 *         otherwise.
	 */
	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		return BCrypt.checkpw(rawPassword.toString(), encodedPassword.toString());
	}
}