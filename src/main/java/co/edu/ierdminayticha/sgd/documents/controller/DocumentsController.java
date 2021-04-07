package co.edu.ierdminayticha.sgd.documents.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import co.edu.ierdminayticha.sgd.documents.api.IDocumentsApi;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentRequestDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentResponseDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentsResponseListDto;
import co.edu.ierdminayticha.sgd.documents.service.IDocumentsService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RefreshScope
@RestController
@RequestMapping(value = "document/v1/document")
public class DocumentsController implements IDocumentsApi {

	@Autowired
	private IDocumentsService service;

	@Override
	public ResponseEntity<DocumentResponseDto> create(DocumentRequestDto request) {

		log.info("IReferenciaApiImplController : create - Creando recurso {}", request);

		DocumentResponseDto response = service.create(request);

		log.info("IReferenciaApiImplController : create - Transacción exitosa, recurso creado: " + "{}", response);

		return buildCreationResponse(response);
	}

	@Override
	public ResponseEntity<DocumentResponseDto> findById(Long id) {

		log.info("IReferenciaApiImplController : findById - Consultando recurso con id " + "{}", id);

		DocumentResponseDto response = this.service.findById(id);

		log.info("IReferenciaApiImplController : findById - Transacción exitosa, recurso: " + "{}", response);

		return ResponseEntity.ok(response);
	}

	@Override
	public ResponseEntity<List<DocumentsResponseListDto>> findAll() {

		log.info("IReferenciaApiImplController : findAll - Consultando todos los registros");

		List<DocumentsResponseListDto> response = service.findAll();

		log.info("IReferenciaApiImplController : findAll - Transacción exitosa, registros " + "consultados: ",
				response);

		return ResponseEntity.ok(response);
	}

	@Override
	public ResponseEntity<?> update(Long id, DocumentRequestDto request) {

		log.info("IReferenciaApiImplController : update - Actualizando recurso con id {}, " + "nuevos valores: {}", id,
				request);

		service.update(id, request);

		log.info("IReferenciaApiImplController : update - Transacción exitosa, registro " + "actualizado");

		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<?> delete(Long id) {

		log.info("IReferenciaApiImplController : delete - Eliminando recurso con id {}", id);

		this.service.delete(id);

		log.info("IReferenciaApiImplController : delete - Transacción exitosa, recurso " + "eliminado");

		return ResponseEntity.ok().build();
	}

	private ResponseEntity<DocumentResponseDto> buildCreationResponse(DocumentResponseDto response) {

		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{referencia-id}")
				.buildAndExpand(response.getId()).toUri();

		return ResponseEntity.created(uri).body(response);

	}

}
