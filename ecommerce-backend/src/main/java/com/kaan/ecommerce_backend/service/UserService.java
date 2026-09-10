package com.kaan.ecommerce_backend.service;

import com.kaan.ecommerce_backend.dto.AuthResponse;
import com.kaan.ecommerce_backend.dto.UserProfileDto;
import com.kaan.ecommerce_backend.dto.UserResponseDto;
import com.kaan.ecommerce_backend.entity.User;
import com.kaan.ecommerce_backend.repository.UserRepository;
import com.kaan.ecommerce_backend.security.JwtService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {



    private final UserRepository userRepository;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public AuthResponse loginOrRegister(String email, String firstName, String lastName, String provider) {

        String normalizedEmail = (email != null) ? email.trim().toLowerCase() : null;
        User user;

        Optional<User> existingUser = userRepository.findByEmail(normalizedEmail);

        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            User newUser = new User();
            newUser.setEmail(normalizedEmail);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setAuthProvider(provider);

            newUser.setRole("USER");

            user = userRepository.save(newUser);
        }

        String jwtToken = jwtService.generateToken(user.getEmail());

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                jwtToken,
                user.getRole()
        );
    }

    public List<UserResponseDto> getAllUsersForAdmin(String role) {

        List<User> users;
        if (role == null || role.trim().isEmpty()) {
            users = userRepository.findAllByOrderByIdAsc();
        } else {
            users = userRepository.findByRoleOrderByIdAsc(role);
        }

        return users.stream().map(user -> new UserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getAuthProvider(),
                user.getPhotoURL(),
                user.getPhone(),
                user.getCity(),
                user.getCountry()
        )).collect(Collectors.toList());
    }

    public UserProfileDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserProfileDto.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .address(user.getAddress())
                .city(user.getCity())
                .country(user.getCountry())
                .photoURL(user.getPhotoURL())
                .build();
    }

    public UserProfileDto updateProfile(String email, UserProfileDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setCity(dto.getCity());
        user.setCountry(dto.getCountry());

        userRepository.save(user);
        return getProfile(email);
    }

    public void updatePhotoURL(String email, String photoURL) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPhotoURL(photoURL);
        userRepository.save(user);
    }
}
