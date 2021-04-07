package co.edu.ierdminayticha.sgd.documents.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private LocalDateTime timeStamp;
	private int codeStatus;
	private HttpStatus status;
	private String message;
	private List<String> errors;

}
