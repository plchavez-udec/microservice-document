package co.edu.ierdminayticha.sgd.documents.dto;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ChildrenInDto implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long id;
	private Long nodeType;
	private String name;

}