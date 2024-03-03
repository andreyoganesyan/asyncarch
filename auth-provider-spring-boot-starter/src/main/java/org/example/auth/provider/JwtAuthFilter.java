package org.example.auth.provider;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.example.models.auth.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@Log4j2
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JWTVerifier jwtVerifier;
    private final KTable<UUID, User> userKTable;
    private final StreamsBuilderFactoryBean streamsBuilder;

    public JwtAuthFilter(@Value("security.jwt.secret") String jwtSecret, KTable<UUID, User> userKTable, StreamsBuilderFactoryBean streamsBuilder) {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        this.jwtVerifier = JWT.require(algorithm).build();
        this.userKTable = userKTable;
        this.streamsBuilder = streamsBuilder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var userStore = streamsBuilder.getKafkaStreams().store(StoreQueryParameters.fromNameAndType(this.userKTable.queryableStoreName(), QueryableStoreTypes.<UUID, User>keyValueStore()));
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authorization.split(" ")[1];
        try {
            DecodedJWT decodedJwt = jwtVerifier.verify(token);
            log.info("Decoded user JWT, subject: {}", decodedJwt.getSubject());
            User user = userStore.get(UUID.fromString(decodedJwt.getSubject()));
            if (user != null) {
                Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of(user.role()));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JWTVerificationException e) {
            log.error("Could not verify the JWT", e);
            // do nothing, the authentication is not set, other filters will take care of it
        }
        filterChain.doFilter(request, response);
    }
}
