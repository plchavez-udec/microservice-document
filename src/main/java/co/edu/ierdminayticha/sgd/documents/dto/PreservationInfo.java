package co.edu.ierdminayticha.sgd.documents.dto;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Data;

@Data
public class PreservationInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	private LocalDate preservatoinDate;
	private int remainingStorageTime;

}
