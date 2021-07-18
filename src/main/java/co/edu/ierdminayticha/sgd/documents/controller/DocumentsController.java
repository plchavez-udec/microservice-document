package co.edu.ierdminayticha.sgd.documents.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import co.edu.ierdminayticha.sgd.documents.api.IDocumentsApi;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentRequestDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentResponseDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentUpdateRequestDto;
import co.edu.ierdminayticha.sgd.documents.service.IDocumentsService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RefreshScope
@RestController
@RequestMapping(value = "v1/document")
public class DocumentsController implements IDocumentsApi {

	@Autowired
	private IDocumentsService service;

	@Override
	public ResponseEntity<DocumentResponseDto> create(DocumentRequestDto request) {
		log.info("create - Crear documento {}", request);
		DocumentResponseDto response = service.create(request);
		log.info("create - Crear documento. Transacción exitosa {}", response);
		return buildCreationResponse(response);
	}

	@Override
	public ResponseEntity<DocumentResponseDto> findById(Long id) {
		log.info("findById - Consultar carpeta con id " + "{}", id);
		DocumentResponseDto response = this.service.findById(id);
		log.info("findById - Transacción exitosa, carpeta: {}", response);
		return ResponseEntity.ok(response);
	}

	@Override
	public ResponseEntity<String> update(Long id, DocumentUpdateRequestDto request) {
		log.info("update - Actualizar carpeta con id {}, nuevos valores: {}", id, request);
		this.service.update(id, request);
		log.info("update - Transacción exitosa, registro actualizado");
		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<String> delete(Long id) {
		log.info("delete - Eliminar carpeta con id {}", id);
		this.service.delete(id);
		log.info("delete - Transacción exitosa, carpeta eliminado");
		return ResponseEntity.ok().build();
	}

	private ResponseEntity<DocumentResponseDto> buildCreationResponse(DocumentResponseDto response) {
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{referencia-id}")
				.buildAndExpand(response.getId()).toUri();
		return ResponseEntity.created(uri).body(response);

	}

}
