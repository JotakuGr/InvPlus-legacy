package br.com.invplus.shared.api.controller;

import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

/**
 * Base leve para controllers CRUD criarem respostas REST consistentes.
 */
public abstract class BaseCrudController {

    /**
     * Monta resposta 201 usando o caminho do recurso e o identificador criado.
     */
    protected <RES> ResponseEntity<RES> created(String resourcePath, UUID id, RES response) {
        return ResponseEntity.created(URI.create(resourcePath + "/" + id)).body(response);
    }
}
