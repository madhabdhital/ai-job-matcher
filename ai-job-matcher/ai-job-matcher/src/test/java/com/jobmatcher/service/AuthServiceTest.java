package com.jobmatcher.service;

import com.jobmatcher.dto.AuthResponse;
import com.jobmatcher.dto.LoginRequest;
import com.jobmatcher.dto.RegisterRequest;
import com.jobmatcher.entity.User;
import com.jobmatcher.repository.UserRepository;
import com.jobmatcher.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    private RegisterRequest registerRequest(String name, String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setFullName(name);
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    // ---------- registration ----------

    @Test
    void register_savesUserWithHashedPasswordAndNormalizedEmail() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Passw0rd@")).thenReturn("hashed-password");
        when(tokenProvider.generateToken("test@example.com")).thenReturn("jwt-token");

        AuthResponse response = authService.register(
                registerRequest("  Test User  ", "  Test@Example.COM ", "Passw0rd@"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());

        assertEquals("Test User", saved.getValue().getFullName());
        assertEquals("test@example.com", saved.getValue().getEmail());
        assertEquals("hashed-password", saved.getValue().getPassword());

        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getFullName());
    }

    @Test
    void register_duplicateEmail_throwsAndDoesNotSave() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        RuntimeException error = assertThrows(RuntimeException.class, () ->
                authService.register(registerRequest("Test User", "test@example.com", "Passw0rd@")));

        assertEquals("Email address is already registered", error.getMessage());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void register_blankEmail_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                authService.register(registerRequest("Test User", "   ", "Passw0rd@")));

        verify(userRepository, never()).save(any());
    }

    // ---------- login ----------

    @Test
    void login_validCredentials_returnsToken() {
        User user = new User("Test User", "test@example.com", "hashed-password");

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(tokenProvider.generateToken("test@example.com")).thenReturn("jwt-token");

        AuthResponse response = authService.login(loginRequest("Test@Example.com", "Passw0rd@"));

        assertEquals("jwt-token", response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getFullName());
    }

    @Test
    void login_normalizesEmailBeforeAuthenticating() {
        User user = new User("Test User", "test@example.com", "hashed-password");

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(tokenProvider.generateToken(anyString())).thenReturn("jwt-token");

        authService.login(loginRequest("  TEST@example.com ", "Passw0rd@"));

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals("test@example.com", captor.getValue().getPrincipal());
    }

    @Test
    void login_wrongPassword_throwsBadCredentialsAndIssuesNoToken() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("bad"));

        BadCredentialsException error = assertThrows(BadCredentialsException.class, () ->
                authService.login(loginRequest("test@example.com", "WrongPass1@")));

        assertEquals("Invalid email or password", error.getMessage());
        verifyNoInteractions(tokenProvider);
    }
}