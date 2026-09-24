package com.cams.auth.service;

import com.cams.auth.dto.LoginRequest;
import com.cams.auth.dto.LoginResponse;
import com.cams.auth.dto.RegisterRequest;
import com.cams.auth.dto.RegisterResponse;
import com.cams.auth.entity.User;
import com.cams.auth.repository.UserRepository;
import com.cams.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateUserSuccessfully() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("amit");
        request.setPassword("password123");

        when(userRepository.existsByUsername("amit"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        User savedUser = new User(
                "amit",
                "encodedPassword",
                "USER"
        );

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        RegisterResponse response =
                authService.register(request);

        assertNotNull(response);
        assertEquals("amit", response.getUsername());
        assertEquals("USER", response.getRole());

        verify(userRepository)
                .existsByUsername("amit");

        verify(passwordEncoder)
                .encode("password123");

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void register_shouldStoreEncodedPassword() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("amit");
        request.setPassword("password123");

        when(userRepository.existsByUsername("amit"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        User savedUser = new User(
                "amit",
                "encodedPassword",
                "USER"
        );

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        authService.register(request);

        verify(userRepository).save(
                argThat(user ->
                        user.getUsername().equals("amit")
                                && user.getPassword().equals("encodedPassword")
                                && user.getRole().equals("USER")
                )
        );
    }

    @Test
    void register_shouldThrowException_whenUsernameAlreadyExists() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("amit");
        request.setPassword("password123");

        when(userRepository.existsByUsername("amit"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        verify(userRepository)
                .existsByUsername("amit");

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void login_shouldReturnJwtToken_whenCredentialsAreValid() {

        LoginRequest request = new LoginRequest();
        request.setUsername("amit");
        request.setPassword("password123");

        User user = new User(
                "amit",
                "encodedPassword",
                "USER"
        );

        when(userRepository.findByUsername("amit"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123",
                "encodedPassword"
        )).thenReturn(true);

        when(jwtService.generateToken("amit", "USER"))
                .thenReturn("jwt-token");

        LoginResponse response =
                authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("amit", response.getUsername());
        assertEquals("USER", response.getRole());

        verify(userRepository)
                .findByUsername("amit");

        verify(passwordEncoder)
                .matches("password123", "encodedPassword");

        verify(jwtService)
                .generateToken("amit", "USER");
    }

    @Test
    void login_shouldThrowException_whenPasswordIsInvalid() {

        LoginRequest request = new LoginRequest();
        request.setUsername("amit");
        request.setPassword("wrongPassword");

        User user = new User(
                "amit",
                "encodedPassword",
                "USER"
        );

        when(userRepository.findByUsername("amit"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrongPassword",
                "encodedPassword"
        )).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        verify(passwordEncoder)
                .matches("wrongPassword", "encodedPassword");

        verify(jwtService, never())
                .generateToken(anyString(), anyString());
    }

    @Test
    void login_shouldThrowException_whenUsernameDoesNotExist() {

        LoginRequest request = new LoginRequest();
        request.setUsername("unknown");
        request.setPassword("password123");

        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        verify(userRepository)
                .findByUsername("unknown");

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(jwtService, never())
                .generateToken(anyString(), anyString());
    }

    @Test
    void register_shouldAlwaysCreateUserWithUserRole() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");

        when(userRepository.existsByUsername("newuser"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponse response =
                authService.register(request);

        assertEquals("USER", response.getRole());

        verify(userRepository).save(
                argThat(user ->
                        "USER".equals(user.getRole())
                )
        );
    }



}