package com.genc.hms.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.genc.hms.dto.UserLoginResponseDTO;
import com.genc.hms.entity.User;
import com.genc.hms.enums.Role;
import com.genc.hms.repository.UserRepository;
import com.genc.hms.util.CustomPasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private CustomPasswordEncoder customPasswordEncoder;

	@InjectMocks
	private UserService userService;

	private User mockUser;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mockUser = new User();
		mockUser.setUserId(1L);
		mockUser.setEmail("test@example.com");
		mockUser.setPassword("encodedPass");
		mockUser.setRole(Role.PATIENT); // use whatever enum or role type you have
	}

	@Test
	void testLogin_Success() {
		// Arrange
		when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
		when(customPasswordEncoder.matches("password123", "encodedPass")).thenReturn(true);

		// Act
		UserLoginResponseDTO response = userService.login("test@example.com", "password123");

		// Assert
		assertNotNull(response);
		assertEquals("test@example.com", response.getEmail());
		assertEquals("Login successful!", response.getMessage());
		verify(userRepository, times(1)).findByEmail("test@example.com");
	}

	@Test
	void testLogin_InvalidEmail_ThrowsException() {
		// Arrange
		when(userRepository.findByEmail("wrong@example.com")).thenReturn(Optional.empty());

		// Act & Assert
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.login("wrong@example.com", "password123"));

		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		assertEquals("Invalid email or password", exception.getReason());
	}

	@Test
	void testLogin_InvalidPassword_ThrowsException() {
		// Arrange
		when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
		when(customPasswordEncoder.matches("wrongPass", "encodedPass")).thenReturn(false);

		// Act & Assert
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.login("test@example.com", "wrongPass"));

		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		assertEquals("Invalid email or password", exception.getReason());
	}
}
