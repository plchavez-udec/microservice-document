package co.edu.ierdminayticha.sgd.documents.service;

import co.edu.ierdminayticha.sgd.documents.dto.DocumentRequestDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentResponseDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentUpdateRequestDto;

public interface IDocumentsService {
	DocumentResponseDto create(DocumentRequestDto dto);
	DocumentResponseDto findById(Long id);
	void update(Long id, DocumentUpdateRequestDto dto);
	void delete(Long id);
}
