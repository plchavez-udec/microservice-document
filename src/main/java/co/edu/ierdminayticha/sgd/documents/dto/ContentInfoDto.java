package co.edu.ierdminayticha.sgd.documents.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class ContentInfoDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String contentType;
	private String documentFamily;
	private Integer size;;

}
