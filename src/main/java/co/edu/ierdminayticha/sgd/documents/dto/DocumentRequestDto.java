package co.edu.ierdminayticha.sgd.documents.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class DocumentRequestDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long parent;
	private Long documentaryType;
	private DocumentaryUnitDto documentaryUnit;
	private Long securityLevel;
	private DocumentInfoDto documentInfo;
	private ContentInfoDto dontentInfo;
	private BinaryInfoDto binaryInfo;
	private List<SpecificMetadataDto> specificMetadata;

}
