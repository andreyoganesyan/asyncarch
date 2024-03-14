package org.example.task.management.streaming;

import lombok.RequiredArgsConstructor;
import org.example.auth.provider.UserPrincipal;
import org.example.models.auth.AuthTopics;
import org.example.models.auth.UserCudEventV1;
import org.example.models.auth.UserV1;
import org.example.task.management.db.UserEntity;
import org.example.task.management.db.UserEntityRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserStreamingListener {
    private final UserEntityRepository userEntityRepository;

    @KafkaListener(topics = AuthTopics.USER_STREAMING,
            groupId = "task-management-service-user-streaming")
    public void acceptUserCudEvent(@Payload UserCudEventV1 userCudEvent) {
        switch (userCudEvent.getEventType()) {
            case CREATED, UPDATED -> createOrUpdateUser(userCudEvent.getUser());
            case DELETED -> deactivateUser(userCudEvent.getUser());
        }
    }


    private void createOrUpdateUser(UserV1 userToUpdate) {
        UserEntity existingUser = userEntityRepository.findById(userToUpdate.getId())
                .orElse(new UserEntity());
        existingUser.setId(userToUpdate.getId());
        existingUser.setRole(mapRoleV1(userToUpdate));
        existingUser.setDisplayName(userToUpdate.getDisplayName());
        existingUser.setEmail(userToUpdate.getEmail());
        userEntityRepository.save(existingUser);
    }

    private void deactivateUser(UserV1 userToDeactivate) {
        Optional<UserEntity> userOpt = userEntityRepository.findById(userToDeactivate.getId());
        if (userOpt.isEmpty()) {
            return;
        }
        var user = userOpt.get();
        user.setActive(false);
        userEntityRepository.save(user);
    }

    private UserPrincipal.Role mapRoleV1(UserV1 user) {
        return switch (user.getRole()) {
            case ROLE_ACCOUNTANT -> UserPrincipal.Role.ROLE_ACCOUNTANT;
            case ROLE_ADMIN -> UserPrincipal.Role.ROLE_ADMIN;
            case ROLE_EMPLOYEE -> UserPrincipal.Role.ROLE_EMPLOYEE;
        };
    }
}
