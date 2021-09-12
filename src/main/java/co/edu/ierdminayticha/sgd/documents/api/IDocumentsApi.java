package co.edu.ierdminayticha.sgd.documents.api;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import co.edu.ierdminayticha.sgd.documents.dto.DocumentListResponseDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentRequestDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentResponseDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentUpdateRequestDto;
import co.edu.ierdminayticha.sgd.documents.dto.IRequestCreateValidation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "Api Microservicio para la gestion de documentos", 
     tags = "Api Microservicio para la gestion de documentos")
public interface IDocumentsApi {
	
	@ApiOperation(value = "Crear documento",
		          response = DocumentResponseDto.class)
	@PostMapping(value = "", 
				 consumes = MediaType.APPLICATION_JSON_VALUE, 
				 produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<DocumentResponseDto> create( 
			@Validated(IRequestCreateValidation.class)
			@RequestBody DocumentRequestDto request);
	
	@ApiOperation(value = "Obtener lista de documentos",
            response = DocumentResponseDto.class)
	@GetMapping(value = "", 
		    produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<List<DocumentListResponseDto>> findAll( @RequestParam String filter, 
			@RequestParam String filterValue);

	@ApiOperation(value = "Obtener documento por Id",
	              response = DocumentResponseDto.class)
	@GetMapping(value = "/{documento-id}", 
			    produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<DocumentResponseDto> findById(
			@PathVariable("documento-id") Long id,
			@RequestParam String location);

	@ApiOperation(value = "Actualización parcial del documento",
      	  		  response = DocumentRequestDto.class)
	@PostMapping(value = "{document-id}", 
			 	  consumes = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> update(
			@PathVariable("document-id")Long id,
			@RequestBody DocumentUpdateRequestDto request);

	@ApiOperation(value = "Eliminación de un documento",
	  		  response = DocumentRequestDto.class)
	@DeleteMapping(value = "{document-id}/parent-folder/{id-parent-folder}")
	ResponseEntity<String> delete( 
			@PathVariable("id-parent-folder") Long idParentFolder, 
			@PathVariable("document-id") Long idDocument);

}
