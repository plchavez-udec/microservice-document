package co.edu.ierdminayticha.sgd.documents.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class DocumentResponseDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private Long parent;
	private DocumentaryTypeOutDto documentaryType;	
	private DocumentInfoDto documentInfo;
	private ContentInfoDto contentInfo;
	private BinaryInfoDto binaryInfo;
	private PreservationInfo preservationInfo;
	private List<SpecificMetadataDto> specificMetadata;

}
