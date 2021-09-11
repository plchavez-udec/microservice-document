package co.edu.ierdminayticha.sgd.documents.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
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
import co.edu.ierdminayticha.sgd.documents.entity.MessageEntity;
import co.edu.ierdminayticha.sgd.documents.exception.GeneralException;
import co.edu.ierdminayticha.sgd.documents.repository.IMessageRepository;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestControllerAdvice
public class HandlerExceptionsController extends ResponseEntityExceptionHandler {
	
	@Autowired
	private IMessageRepository messageRepository;

	@ExceptionHandler({ Exception.class })
	public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
		log.info("HandlerExceptionsController : handleAll {}", ex);
		
		MessageEntity message = 
				this.messageRepository
				.findById(ResponseCodeConstants.ERROR_TECHNICAL_TRANSACTION_PROCESSING)
				.orElseThrow(NoSuchElementException::new);

		ErrorDto apiError = new ErrorDto(LocalDateTime.now(), 
					HttpStatus.INTERNAL_SERVER_ERROR.value(),
					HttpStatus.INTERNAL_SERVER_ERROR,
					message.getCodeMessage(),
					message.getDescripction(), 
					Arrays.asList(ex.getMessage()));

		return new ResponseEntity<>(apiError, new HttpHeaders(), 
				apiError.getStatus());
	}

	@ExceptionHandler({ GeneralException.class })
	public ResponseEntity<Object> generalException(GeneralException e, WebRequest request) {

		MessageEntity message = 
				this.messageRepository.findById(e.getMessage())
				.orElseThrow(NoSuchElementException::new);

		ErrorDto apiError = new ErrorDto(LocalDateTime.now(), 
				HttpStatus.BAD_REQUEST.value(), 
				HttpStatus.BAD_REQUEST,
				message.getCodeMessage(),
				message.getDescripction(),
				Arrays.asList(message.getDescripction()));
		
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());

	}

	@Override
	protected ResponseEntity<Object> handleBindException(BindException ex, 
			HttpHeaders headers, HttpStatus status,
			WebRequest request) {

		MessageEntity message = this.messageRepository
				.findById(ResponseCodeConstants.ERROR_TECHNICAL_INPUT_FILE_VALIDATION)
				.orElseThrow(NoSuchElementException::new);
		
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
				message.getCodeMessage(),
				message.getDescripction(), 
				errors);

		return handleExceptionInternal(ex, apiError, headers, 
				apiError.getStatus(), request);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex, HttpHeaders headers, 
			HttpStatus status, WebRequest request) {
		
		MessageEntity message = this.messageRepository
				.findById(ResponseCodeConstants.ERROR_TECHNICAL_INPUT_FILE_VALIDATION)
				.orElseThrow(NoSuchElementException::new);
		
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
				message.getCodeMessage(),
				message.getDescripction(), 
				errors);

		return super.handleExceptionInternal(ex, apiError, headers, 
				apiError.getStatus(), request);
	}
}
