package org.example.task.management.streaming;

import lombok.RequiredArgsConstructor;
import org.example.models.auth.User;
import org.example.models.auth.UserCudEvent;
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

    @KafkaListener(topics = UserCudEvent.TOPIC,
            groupId = "task-management-service-user-streaming")
    public void acceptUserCudEvent(@Payload UserCudEvent userCudEvent) {
        switch (userCudEvent.eventType()) {
            case CREATED, UPDATED -> createOrUpdateUser(userCudEvent.user());
            case DELETED -> deactivateUser(userCudEvent.user());
        }
    }


    private void createOrUpdateUser(User userToUpdate) {
        UserEntity existingUser = userEntityRepository.findById(userToUpdate.id())
                .orElse(new UserEntity());
        existingUser.setId(userToUpdate.id());
        existingUser.setRole(userToUpdate.role());
        existingUser.setDisplayName(userToUpdate.displayName());
        existingUser.setEmail(userToUpdate.email());
        userEntityRepository.save(existingUser);
    }

    private void deactivateUser(User userToDeactivate) {
        Optional<UserEntity> userOpt = userEntityRepository.findById(userToDeactivate.id());
        if (userOpt.isEmpty()) {
            return;
        }
        var user = userOpt.get();
        user.setActive(false);
        userEntityRepository.save(user);
    }
}
