package com.genc.hms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.genc.hms.service.CustomUserDetailService;
import com.genc.hms.util.CustomPasswordEncoder;
import com.genc.hms.util.JwtAuthenticationFilter;

/**
 * Main Spring Security configuration class.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize/@PostAuthorize annotations
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthFilter;
	private final CustomUserDetailService userDetailsService;
	private final CustomPasswordEncoder passwordEncoder;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, CustomUserDetailService userDetailsService,
			CustomPasswordEncoder passwordEncoder) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Exposes the AuthenticationManager bean, which is required for explicit
	 * authentication in the login controller.
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	/**
	 * Configures the main security filter chain.
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.cors(Customizer.withDefaults())
				// 1. Disable CSRF (not needed for stateless REST APIs)
				.csrf(AbstractHttpConfigurer::disable)

				// 2. Define authorization rules for endpoints
				.authorizeHttpRequests(auth -> auth
						// Allow unauthenticated access to authentication/public endpoints
						.requestMatchers("/api/auth/**").permitAll().requestMatchers("/api/admin/**").hasRole("ADMIN")
						.requestMatchers("/api/doctors/**").hasAnyRole("ADMIN", "DOCTOR")
						.requestMatchers("/api/patients/**").hasAnyRole("ADMIN", "PATIENT").anyRequest()
						.authenticated())

				// 3. Configure session management to be STATELESS (required for JWT)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				// 4. Set the custom Authentication Provider
				.authenticationProvider(authenticationProvider())

				// 5. Add the JWT filter before the Spring Security's
				// UsernamePasswordAuthenticationFilter
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * Configures the Authentication Provider using the CustomUserDetailService and
	 * CustomPasswordEncoder.
	 */
	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder);
		return authProvider;
	}
}