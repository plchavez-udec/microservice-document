package co.edu.ierdminayticha.sgd.documents.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class DocumentInfoDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private String creationUser;
	private String comment;
	private Date creationDate;
	private Date lastModifiedDate;

}
