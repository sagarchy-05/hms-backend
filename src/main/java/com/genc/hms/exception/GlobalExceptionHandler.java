package com.genc.hms.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // 🚨 New Import
import org.springframework.security.authentication.BadCredentialsException; // 🚨 New Import
import org.springframework.security.core.userdetails.UsernameNotFoundException; // 🚨 New Import
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles all exceptions globally across the project, providing consistent JSON
 * error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	// --- 1. Validation Errors (@Valid) ---
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();

		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.put(error.getField(), error.getDefaultMessage());
		}

		return ResponseEntity.badRequest().body(errors); // 400 Bad Request
	}

	// --- 2. Security/Authentication Errors ---

	// Handles wrong email/password during login (Username not found is caught first
	// by the provider)
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
		Map<String, String> error = new HashMap<>();
		error.put("message", "Invalid username or password.");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error); // 401 Unauthorized
	}

	// Handles missing user (thrown by CustomUserDetailService)
	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
		Map<String, String> error = new HashMap<>();
		error.put("message", "User not found.");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error); // 401 Unauthorized
	}

	// Handles access denied errors (e.g., attempting to access /api/admin without
	// ADMIN role)
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
		Map<String, String> error = new HashMap<>();
		error.put("message", "You do not have permission to access this resource.");
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error); // 403 Forbidden
	}

	// --- 3. Catch-All Generic Runtime Errors ---

	@ResponseStatus(HttpStatus.NOT_FOUND)
	public static class ResourceNotFoundException extends RuntimeException {
		public ResourceNotFoundException(String message) {
			super(message);
		}
	}

	// Ensure this remains the last handler as it catches all unhandled
	// RuntimeExceptions.
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
		Map<String, String> error = new HashMap<>();
		// In production, consider logging the full stack trace and returning a generic
		// error message
		// to the client for security.
		error.put("message", "An unexpected error occurred on the server.");
		error.put("details", ex.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error); // 500 Internal Server Error
	}
}