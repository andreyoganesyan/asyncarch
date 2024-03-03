package org.example.auth.provider;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.example.models.auth.User;
import org.example.models.auth.UserCudEvent;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.UUID;

@AutoConfiguration
@ComponentScan
@EnableKafkaStreams
public class AuthProviderAutoConfig {
    @Bean
    public KTable<UUID, User> userKTable(StreamsBuilder streamsBuilder) {
        return streamsBuilder.stream(UserCudEvent.TOPIC, Consumed.with(new Serdes.UUIDSerde(), new JsonSerde<>(UserCudEvent.class)).withOffsetResetPolicy(Topology.AutoOffsetReset.EARLIEST))
                .groupBy(((key, value) -> value.user().id()))
                .reduce((agg, event) ->
                        switch (event.eventType()) {
                            case CREATED, UPDATED -> event;
                            case DELETED -> null;
                        }
                ).filter((key, event) -> event.eventType() != UserCudEvent.EventType.DELETED)
                .mapValues(UserCudEvent::user, Materialized.<UUID, User, KeyValueStore<Bytes, byte[]>>as("auth_provider_user_ktable")
                        .withValueSerde(new JsonSerde<>(User.class))
                        .withKeySerde(new Serdes.UUIDSerde()));
    }


}
