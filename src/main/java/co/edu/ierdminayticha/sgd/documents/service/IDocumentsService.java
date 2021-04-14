package co.edu.ierdminayticha.sgd.documents.service;

import java.util.List;

import co.edu.ierdminayticha.sgd.documents.dto.DocumentRequestDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentResponseDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentUpdateRequestDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentsResponseListDto;

public interface IDocumentsService {

	DocumentResponseDto create(DocumentRequestDto dto);

	DocumentResponseDto findById(Long id);

	List<DocumentsResponseListDto> findAll();

	void update(Long id, DocumentUpdateRequestDto dto);

	void delete(Long id);

}
