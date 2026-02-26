package com.journeo.service;

import com.journeo.dto.UserRequestDTO;
import com.journeo.dto.UserResponseDTO;
import com.journeo.model.User;
import com.journeo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Crée un utilisateur
    public User createUser(UserRequestDTO dto) {
        // Validation du email
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        // Validation du password
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        // Validation du role
        if (dto.getRole() == null || dto.getRole().isBlank()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRoleEnum());

        return userRepository.save(user);
    }

    // Met à jour un utilisateur
    public User updateUser(Long id, UserRequestDTO dto) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return null;

        // Mise à jour conditionnelle du email
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            user.setEmail(dto.getEmail());
        }

        // Mise à jour conditionnelle du password
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // Mise à jour conditionnelle du role
        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            user.setRole(dto.getRoleEnum());
        }

        return userRepository.save(user);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    // 🔹 Conversion en DTO
    public UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(user);
    }

    public List<UserResponseDTO> toDTOList(List<User> users) {
        return users.stream().map(this::toDTO).collect(Collectors.toList());
    }
}