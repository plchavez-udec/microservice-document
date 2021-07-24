package co.edu.ierdminayticha.sgd.documents.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class DocumentInfoDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String creationUser; //po debajo
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date creationDate;
	private Date lastModifiedDate;
	private String name;
	private String comment;

}
