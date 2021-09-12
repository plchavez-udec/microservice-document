package co.edu.ierdminayticha.sgd.documents.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import co.edu.ierdminayticha.sgd.documents.dto.DocumentListResponseDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentRequestDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentResponseDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentUpdateRequestDto;

public interface IDocumentsService {
	DocumentResponseDto create(DocumentRequestDto dto);
	List<DocumentListResponseDto> findAll(String filter, String filterValue);
	DocumentResponseDto findById(Long id, String location);
	void update(Long id, DocumentUpdateRequestDto dto);
	void delete(Long idParentFolder, Long idDocument);
}
