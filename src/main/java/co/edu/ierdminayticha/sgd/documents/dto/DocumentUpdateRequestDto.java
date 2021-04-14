package co.edu.ierdminayticha.sgd.documents.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class DocumentUpdateRequestDto implements Serializable {

	private static final long serialVersionUID = 1L;


	private String name;
	private String comment;
	private String location;
	private PreservationInfoUpdate preservationInfo; 
	private List<SpecificMetadataDto> specificMetadata;

}
