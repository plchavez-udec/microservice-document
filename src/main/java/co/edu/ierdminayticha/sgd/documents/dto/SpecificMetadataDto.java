package co.edu.ierdminayticha.sgd.documents.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class SpecificMetadataDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String name;
	private String value;
}