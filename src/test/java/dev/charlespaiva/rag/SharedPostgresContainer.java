package dev.charlespaiva.rag;

import org.testcontainers.containers.PostgreSQLContainer;
import java.time.Duration;

public final class SharedPostgresContainer {

    @SuppressWarnings("resource")
    public static final PostgreSQLContainer<?> INSTANCE =
        new PostgreSQLContainer<>("pgvector/pgvector:pg16")
            .withDatabaseName("rag_test")
            .withUsername("rag")
            .withPassword("rag")
            .withStartupTimeout(Duration.ofMinutes(5));

    static {
        INSTANCE.start();
    }

    private SharedPostgresContainer() {}
}
