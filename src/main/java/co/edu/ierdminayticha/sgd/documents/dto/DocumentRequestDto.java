package co.edu.ierdminayticha.sgd.documents.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class DocumentRequestDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long parent;	
	private DocumentaryUnitDto documentaryUnit;
	private DocumentInfoDto documentInfo;
	private ContentInfoDto contentInfo;
	private BinaryInfoDto binaryInfo;
	private List<SpecificMetadataDto> specificMetadata;

}
