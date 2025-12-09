package com.genc.hms.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.genc.hms.dto.JwtAuthenticationResponseDTO;
import com.genc.hms.dto.PatientRegisterRequestDTO;
import com.genc.hms.dto.UserLoginRequestDTO;
import com.genc.hms.dto.UserResponseDTO;
import com.genc.hms.entity.User;
import com.genc.hms.service.UserService;
import com.genc.hms.util.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth") // Change path to match SecurityConfig's permitAll path
@CrossOrigin
public class AuthController {

	private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

	private final UserService userService;
	private final JwtService jwtService;
	// You need the AuthenticationManager bean in your SecurityConfig for this to
	// work
	private final AuthenticationManager authenticationManager;

	public AuthController(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager) {
		this.userService = userService;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
	}

	// ----------------- Registration -----------------

	// REVISED registerPatient method in AuthController
	@PostMapping("/register")
	public ResponseEntity<UserResponseDTO> registerPatient(
	        @Valid @RequestBody PatientRegisterRequestDTO registrationDetails) {

	    // Keep CONFLICT check here as it's a specific business rule return
	    if (userService.findByEmail(registrationDetails.getEmail()).isPresent()) {
	        logger.warn("Registration attempt with existing email: {}", registrationDetails.getEmail());
	        // Custom exception (e.g., DuplicateEmailException) could replace this check
	        return ResponseEntity.status(HttpStatus.CONFLICT).build(); 
	    }

	    UserResponseDTO responseDTO = userService.registerPatient(registrationDetails);
	    logger.info("New patient registered with userId: {}", responseDTO.getUserId());
	    return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
	}

	// ----------------- JWT Login -----------------

	@PostMapping("/login")
	public ResponseEntity<JwtAuthenticationResponseDTO> login(@Valid @RequestBody UserLoginRequestDTO loginRequest) {

		logger.info("Login attempt for email: {}", loginRequest.getEmail());

		// 1. Authenticate user credentials
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

		// 2. Load UserDetails (already done by the provider, but casting for JWT
		// payload)
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();

		// 3. Generate JWT
		String jwtToken = jwtService.generateToken(userDetails);

		// 4. Create response (You'll need a new DTO for the token response)
		User user = (User) userDetails;

		JwtAuthenticationResponseDTO responseDTO = new JwtAuthenticationResponseDTO(jwtToken, user.getUserId(),
				userService.getRoleIdFromUser(user), // You still need this helper method
				user.getEmail(), user.getRole().name());

		logger.info("User {} logged in successfully, JWT generated.", user.getUserId());
		return ResponseEntity.ok(responseDTO);
	}

	// NOTE: Logout is generally a client-side action in JWT (deleting the token),
	// but you can implement a token revocation mechanism if needed.
}