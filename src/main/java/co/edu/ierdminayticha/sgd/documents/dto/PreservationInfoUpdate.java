package co.edu.ierdminayticha.sgd.documents.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class PreservationInfoUpdate implements Serializable {

	private static final long serialVersionUID = 1L;

	private String partDate;
	private int time;

}
