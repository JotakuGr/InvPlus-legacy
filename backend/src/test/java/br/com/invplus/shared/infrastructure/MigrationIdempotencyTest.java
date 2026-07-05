package br.com.invplus.shared.infrastructure;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Garante que seeds tecnicos em migrations sejam seguros para ambientes recriados.
 */
class MigrationIdempotencyTest {

    /**
     * Toda migration com INSERT deve declarar uma estrategia para nao duplicar dados.
     */
    @Test
    void migrationsComInsertDevemSerIdempotentes() throws IOException {
        Path migrationsDir = Path.of(System.getProperty("user.dir"))
                .resolve("../database/migrations/database")
                .normalize();

        assertTrue(Files.isDirectory(migrationsDir), "Diretorio de migrations nao encontrado: " + migrationsDir);

        List<Path> migrations = Files.walk(migrationsDir)
                .filter(path -> path.toString().endsWith(".sql"))
                .toList();

        for (Path migration : migrations) {
            String sql = Files.readString(migration).toUpperCase();
            boolean possuiInsert = sql.contains("INSERT INTO");
            boolean possuiProtecao = sql.contains("ON CONFLICT") || sql.contains("WHERE NOT EXISTS");

            assertTrue(!possuiInsert || possuiProtecao,
                    "Migration com INSERT sem idempotencia explicita: " + migration.getFileName());
        }
    }
}
