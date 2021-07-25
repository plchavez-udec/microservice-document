package co.edu.ierdminayticha.sgd.documents.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import co.edu.ierdminayticha.sgd.documents.dto.ErrorDto;
import co.edu.ierdminayticha.sgd.documents.exception.GeneralException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestControllerAdvice
public class HandlerExceptionsController extends ResponseEntityExceptionHandler {

	@ExceptionHandler({ Exception.class })
	public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {

		log.info("HandlerExceptionsController : handleAll {}", ex);
		
		ErrorDto apiError = new ErrorDto(LocalDateTime.now(), 
										 HttpStatus.INTERNAL_SERVER_ERROR.value(),
										 HttpStatus.INTERNAL_SERVER_ERROR, 
										 ex.getLocalizedMessage(), 
										 Arrays.asList(ex.getMessage()));		

		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	@ExceptionHandler({ GeneralException.class })
	public ResponseEntity<Object> generalException(GeneralException ex, WebRequest request) {

		log.info("HandlerExceptionsController : generalException {}", ex.getMessage());
		
		String error = ex.getMessage();

		ErrorDto apiError = new ErrorDto(LocalDateTime.now(), 
										 HttpStatus.BAD_REQUEST.value(), 
										 HttpStatus.BAD_REQUEST,
										 error, Arrays.asList(error));

		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());

	}

	@ExceptionHandler({ NoSuchElementException.class })
	public ResponseEntity<Object> noSuchElementException(NoSuchElementException ex, 
														 WebRequest request) {
		
		log.info("HandlerExceptionsController : noSuchElementException {}", ex.getMessage());

		String error = ex.getMessage();

		ErrorDto apiError = new ErrorDto(LocalDateTime.now(), 
										 HttpStatus.BAD_REQUEST.value(), 
										 HttpStatus.BAD_REQUEST,
										 error, 
										 null);

		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());

	}

	@Override
	protected ResponseEntity<Object> handleBindException(BindException ex, 
														 HttpHeaders headers, 
														 HttpStatus status,
														 WebRequest request) {
		
		log.info("HandlerExceptionsController : handleBindException {}", ex);

		List<String> errors = new ArrayList<>();

		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.add(error.getField() + ": " + error.getDefaultMessage());
		}

		for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
			errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
		}

		ErrorDto apiError = new ErrorDto(LocalDateTime.now(), 
										 HttpStatus.BAD_REQUEST.value(), 
										 HttpStatus.BAD_REQUEST,
										 ex.getLocalizedMessage(), 
										 errors);

		return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
																  HttpHeaders headers, 
																  HttpStatus status, 
																  WebRequest request) {
		
		log.info("HandlerExceptionsController : handleMethodArgumentNotValid {}", ex);
		
		List<String> errors = new ArrayList<>();

		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.add(error.getField() + ": " + error.getDefaultMessage());
		}

		for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
			errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
		}

		ErrorDto apiError = new ErrorDto(LocalDateTime.now(), 
										 HttpStatus.BAD_REQUEST.value(), 
										 HttpStatus.BAD_REQUEST,
										 ex.getLocalizedMessage(), 
										 errors);
		
		return super.handleExceptionInternal(ex, apiError, 
				headers, apiError.getStatus(), request);
	}

	
	

}
