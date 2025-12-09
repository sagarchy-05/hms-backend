package com.genc.hms.controller;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.genc.hms.dto.UserChangePasswordRequestDTO;
import com.genc.hms.dto.UserResponseDTO;
import com.genc.hms.entity.User;
import com.genc.hms.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	// ----------------- Registration & Login are now in AuthController
	// -----------------

	// ----------------- Profile & Security -----------------

	/**
	 * Replaces the old /me endpoint. Retrieves the current logged-in user's details
	 * directly from the JWT/Security Context using @AuthenticationPrincipal.
	 */
	@GetMapping("/me")
	public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal User currentUser) {
		// The currentUser object is populated directly by Spring Security after JWT
		// validation.
		if (currentUser == null) {
			// This case should ideally not happen if the endpoint is protected by
			// .authenticated()
			logger.warn("Access to /me attempted without authentication principal.");
			return ResponseEntity.badRequest().build();
		}

		logger.info("Retrieving profile for user {}", currentUser.getUserId());

		// Assuming UserResponseDTO is suitable for returning profile data
		UserResponseDTO responseDTO = new UserResponseDTO(currentUser.getUserId(),
				userService.getRoleIdFromUser(currentUser), currentUser.getEmail(), currentUser.getRole().name());

		return ResponseEntity.ok(responseDTO);
	}

	/**
	 * This endpoint now relies on role-based authorization in the SecurityConfig
	 * and ensures the user can only view their own profile or if they are ADMIN.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<UserResponseDTO> findUserById(@PathVariable long id,
			@AuthenticationPrincipal User currentUser) {

		// Authorization check: Only allow ADMINs or the user themselves.
		boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
		boolean isSelf = currentUser.getUserId().longValue() == id;

		if (!isAdmin && !isSelf) {
			logger.warn("User {} attempted to access unauthorized user details for ID {}", currentUser.getUserId(), id);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		// 1. PERFORMANCE OPTIMIZATION: If accessing self, use the principal object
		// (zero DB call)
		if (isSelf) {
			logger.info("User {} retrieved own details.", id);
			UserResponseDTO responseDTO = new UserResponseDTO(currentUser.getUserId(),
					userService.getRoleIdFromUser(currentUser), currentUser.getEmail(), currentUser.getRole().name());
			return ResponseEntity.ok(responseDTO);
		}

		// 2. Accessing Other Users (Requires DB call, delegated to Service)
		Optional<User> user = userService.findById(id); // DB CALL

		if (user.isPresent()) {
			User u = user.get();
			UserResponseDTO userResponseDTO = new UserResponseDTO(u.getUserId(), userService.getRoleIdFromUser(u),
					u.getEmail(), u.getRole().name());
			logger.info("User {} retrieved details of user {}", currentUser.getUserId(), id);
			return ResponseEntity.ok(userResponseDTO);
		} else {
			logger.warn("User {} tried to retrieve non-existent user {}", currentUser.getUserId(), id);
			return ResponseEntity.notFound().build();
		}
	}

	/**
	 * Password change uses the email/ID from the authenticated principal, not the
	 * session.
	 */
	// REVISED changePassword method in UserController
	@PostMapping("/change-password")
	public ResponseEntity<String> changePassword(@Valid @RequestBody UserChangePasswordRequestDTO request,
			@AuthenticationPrincipal User currentUser) {

		String authenticatedEmail = currentUser.getEmail();
		Long currentUserId = currentUser.getUserId();

		// The service method should throw an exception (e.g., InvalidPasswordException)
		// on failure, which is caught by a global handler.
		boolean success = userService.changePassword(authenticatedEmail, request);

		if (success) {
			logger.info("User {} changed password successfully", currentUserId);
			return ResponseEntity.ok("Password successfully changed. Please log in again with the new password.");
		} else {
			// Assuming userService.changePassword returns false on current password
			// mismatch
			logger.warn("User {} failed to change password (invalid current password)", currentUserId);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Failed to change password. Check your current password.");
		}
	}
}