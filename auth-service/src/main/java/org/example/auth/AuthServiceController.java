package org.example.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import org.example.models.auth.AuthTopics;
import org.example.models.auth.UserCudEventV1;
import org.example.models.auth.UserV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class AuthServiceController {
    @Value("security.jwt.secret")
    private final String jwtSecret;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserEntityRepository userEntityRepository;
    private final KafkaTemplate<UUID, UserCudEventV1> kafkaTemplate;

    @PostMapping
    public UserCudEventV1 createUser(@RequestBody UserEntityDto newUser) {
        var userEntity = new UserEntity();
        userEntity.setId(Optional.ofNullable(newUser.id()).orElse(UUID.randomUUID()));
        userEntity.setRole(newUser.role());
        userEntity.setEmail(newUser.email());
        userEntity.setDisplayName(newUser.displayName());
        userEntity.setPassword(passwordEncoder.encode(newUser.password()));

        var savedUser = userEntityRepository.save(userEntity);
        var userCreatedEvent = new UserCudEventV1()
                .withEventType(UserCudEventV1.EventType.CREATED)
                .withUser(new UserV1()
                        .withId(savedUser.getId())
                        .withEmail(savedUser.getEmail())
                        .withDisplayName(savedUser.getDisplayName())
                        .withRole(mapRoleV1(savedUser)));
        kafkaTemplate.send(AuthTopics.USER_STREAMING, savedUser.getId(), userCreatedEvent);
        return userCreatedEvent;
    }

    @PutMapping
    public UserCudEventV1 updateUser(@RequestBody UserEntityDto userToUpdate) {
        var existingUser = userEntityRepository.findById(userToUpdate.id())
                .orElseThrow(() -> HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        existingUser.setEmail(Optional.ofNullable(userToUpdate.email())
                .orElse(existingUser.getEmail()));
        existingUser.setPassword(Optional.ofNullable(userToUpdate.password())
                .map(passwordEncoder::encode)
                .orElse(existingUser.getPassword()));
        existingUser.setDisplayName(Optional.ofNullable(userToUpdate.displayName())
                .orElse(existingUser.getDisplayName()));
        existingUser.setRole(Optional.ofNullable(userToUpdate.role())
                .orElse(existingUser.getRole()));
        var savedUser = userEntityRepository.save(existingUser);

        var userUpdatedEvent = new UserCudEventV1()
                .withEventType(UserCudEventV1.EventType.UPDATED)
                .withUser(new UserV1()
                        .withId(savedUser.getId())
                        .withEmail(savedUser.getEmail())
                        .withDisplayName(savedUser.getDisplayName())
                        .withRole(mapRoleV1(savedUser)));
        kafkaTemplate.send(AuthTopics.USER_STREAMING, savedUser.getId(), userUpdatedEvent);
        return userUpdatedEvent;
    }

    @DeleteMapping
    public UserCudEventV1 deleteUser(@RequestBody UserEntityDto userToDelete) {
        var existingUser = userEntityRepository.findById(userToDelete.id())
                .orElseThrow(() -> HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));
        userEntityRepository.delete(existingUser);
        var userDeletedEvent = new UserCudEventV1()
                .withEventType(UserCudEventV1.EventType.DELETED)
                .withUser(new UserV1()
                        .withId(existingUser.getId())
                        .withEmail(existingUser.getEmail())
                        .withDisplayName(existingUser.getDisplayName())
                        .withRole(mapRoleV1(existingUser)));
        kafkaTemplate.send(AuthTopics.USER_STREAMING, existingUser.getId(), userDeletedEvent);
        return userDeletedEvent;
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody UserEntityDto credentials) {
        var existingUser = userEntityRepository.findByEmail(credentials.email())
                .orElseThrow(() -> HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));
        if (passwordEncoder.matches(credentials.password(), existingUser.getPassword())) {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            String token = JWT.create()
                    .withSubject(existingUser.getId().toString())
                    .sign(algorithm);
            return new TokenResponse(token);
        }
        throw HttpClientErrorException.create(HttpStatus.FORBIDDEN, "Invalid credentials", null, null, null);
    }

    public record TokenResponse(String accessToken) {
    }

    private UserV1.Role mapRoleV1(UserEntity user) {
        return switch (user.getRole()) {
            case ROLE_ADMIN -> UserV1.Role.ROLE_ADMIN;
            case ROLE_EMPLOYEE -> UserV1.Role.ROLE_EMPLOYEE;
            case ROLE_ACCOUNTANT -> UserV1.Role.ROLE_ACCOUNTANT;
        };
    }
}
