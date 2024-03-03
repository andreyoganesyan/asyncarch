package org.example.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import org.example.models.auth.User;
import org.example.models.auth.UserCudEvent;
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
    private final KafkaTemplate<UUID, UserCudEvent> kafkaTemplate;

    @PostMapping
    public UserCudEvent createUser(@RequestBody UserEntityDto newUser) {
        var userEntity = new UserEntity();
        userEntity.setId(Optional.ofNullable(newUser.id()).orElse(UUID.randomUUID()));
        userEntity.setRole(newUser.role());
        userEntity.setEmail(newUser.email());
        userEntity.setDisplayName(newUser.displayName());
        userEntity.setPassword(passwordEncoder.encode(newUser.password()));

        var savedUser = userEntityRepository.save(userEntity);
        var userCreatedEvent = new UserCudEvent(UserCudEvent.EventType.CREATED, new User(savedUser.getId(), savedUser.getEmail(), savedUser.getDisplayName(), savedUser.getRole()));
        kafkaTemplate.send(UserCudEvent.TOPIC, savedUser.getId(), userCreatedEvent);
        return userCreatedEvent;
    }

    @PutMapping
    public UserCudEvent updateUser(@RequestBody UserEntityDto userToUpdate) {
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

        var userUpdatedEvent = new UserCudEvent(UserCudEvent.EventType.UPDATED, new User(savedUser.getId(), savedUser.getEmail(), savedUser.getDisplayName(), savedUser.getRole()));
        kafkaTemplate.send(UserCudEvent.TOPIC, savedUser.getId(), userUpdatedEvent);
        return userUpdatedEvent;
    }

    @DeleteMapping
    public UserCudEvent deleteUser(@RequestBody UserEntityDto userToDelete) {
        var existingUser = userEntityRepository.findById(userToDelete.id())
                .orElseThrow(() -> HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));
        userEntityRepository.delete(existingUser);
        var userDeletedEvent = new UserCudEvent(UserCudEvent.EventType.DELETED, new User(existingUser.getId(), existingUser.getEmail(), existingUser.getDisplayName(), existingUser.getRole()));
        kafkaTemplate.send(UserCudEvent.TOPIC, existingUser.getId(), userDeletedEvent);
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
}
