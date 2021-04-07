package co.edu.ierdminayticha.sgd.documents.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class DocumentsResponseListDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private Date creationDate;
	private Date lastModifiedDate;

}
