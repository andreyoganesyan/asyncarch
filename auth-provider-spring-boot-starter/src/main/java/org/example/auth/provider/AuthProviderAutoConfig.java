package org.example.auth.provider;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.example.models.auth.AuthTopics;
import org.example.models.auth.UserCudEventV1;
import org.example.models.auth.UserV1;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.security.Principal;
import java.util.UUID;

@AutoConfiguration
@ComponentScan
@EnableKafkaStreams
public class AuthProviderAutoConfig {
    @Bean
    public KTable<UUID, UserPrincipal> userKTable(StreamsBuilder streamsBuilder) {
        return streamsBuilder.stream(AuthTopics.USER_STREAMING, Consumed.with(new Serdes.UUIDSerde(), new JsonSerde<>(UserCudEventV1.class)).withOffsetResetPolicy(Topology.AutoOffsetReset.EARLIEST))
                .groupBy(((key, value) -> value.getUser().getId()))
                .reduce((agg, event) ->
                        switch (event.getEventType()) {
                            case CREATED, UPDATED -> event;
                            case DELETED -> null;
                        }
                ).filter((key, event) -> event.getEventType() != UserCudEventV1.EventType.DELETED)
                .mapValues(this::extractUserPrinciple, Materialized.<UUID, UserPrincipal, KeyValueStore<Bytes, byte[]>>as("auth_provider_user_ktable")
                        .withValueSerde(new JsonSerde<>(UserPrincipal.class))
                        .withKeySerde(new Serdes.UUIDSerde()));
    }

    private UserPrincipal extractUserPrinciple(UserCudEventV1 userCudEventV1) {
        UserV1 user = userCudEventV1.getUser();
        UserPrincipal.Role role = switch (user.getRole()) {
            case ROLE_EMPLOYEE -> UserPrincipal.Role.ROLE_EMPLOYEE;
            case ROLE_ADMIN -> UserPrincipal.Role.ROLE_ADMIN;
            case ROLE_ACCOUNTANT -> UserPrincipal.Role.ROLE_ACCOUNTANT;
        };
        return new UserPrincipal(user.getId(), user.getEmail(), user.getDisplayName(), role);
    }


}
