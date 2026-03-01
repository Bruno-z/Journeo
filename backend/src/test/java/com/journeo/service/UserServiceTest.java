package com.journeo.service;

import com.journeo.dto.UserRequestDTO;
import com.journeo.model.User;
import com.journeo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_shouldSaveUser_whenValidDto() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password");
        dto.setFirstName("John");
        dto.setLastName("Doe");

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        User created = userService.createUser(dto);

        assertNotNull(created);
        assertEquals("John", created.getFirstName());
        assertEquals("Doe", created.getLastName());
        assertEquals("test@example.com", created.getEmail());
    }
}