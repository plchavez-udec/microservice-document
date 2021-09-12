package co.edu.ierdminayticha.sgd.documents.dto;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Data;

@Data
public class DocumentListResponseDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	private String location;
	private String nodeType;
	private LocalDate creationDate;
	private String creationUser;
	private String name;

}