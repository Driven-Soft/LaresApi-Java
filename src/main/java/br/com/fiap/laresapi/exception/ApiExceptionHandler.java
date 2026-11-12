package br.com.fiap.laresapi.exception;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiap.laresapi.dto.ErroResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErroResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
		Map<String, String> campos = new TreeMap<>();
		for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
			campos.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
		}
		return erro(HttpStatus.BAD_REQUEST, "Dados inválidos", campos);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErroResponse> handleConstraintViolation(ConstraintViolationException exception) {
		Map<String, String> campos = new TreeMap<>();
		for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
			String campo = violation.getPropertyPath().toString();
			int ultimoSeparador = campo.lastIndexOf('.');
			if (ultimoSeparador >= 0) {
				campo = campo.substring(ultimoSeparador + 1);
			}
			campos.putIfAbsent(campo, violation.getMessage());
		}
		return erro(HttpStatus.BAD_REQUEST, "Dados inválidos", campos);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErroResponse> handleUnreadableMessage(HttpMessageNotReadableException exception) {
		return erro(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido", Map.of());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErroResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
		return erro(
				HttpStatus.BAD_REQUEST,
				"Parâmetro inválido",
				Map.of(exception.getName(), "Valor inválido ou incompatível com o tipo esperado"));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErroResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
		return erro(HttpStatus.CONFLICT, "Os dados informados conflitam com um registro existente", Map.of());
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ErroResponse> handleResponseStatusException(ResponseStatusException exception) {
		HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
		String message = exception.getReason() == null ? status.getReasonPhrase() : exception.getReason();
		return erro(status, message, Map.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErroResponse> handleUnexpectedException(Exception exception) {
		LOGGER.error("Erro inesperado ao processar requisição", exception);
		return erro(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"Erro interno do servidor",
				Map.of());
	}

	private ResponseEntity<ErroResponse> erro(HttpStatus status, String mensagem, Map<String, String> campos) {
		ErroResponse body = new ErroResponse(status.value(), status.getReasonPhrase(), mensagem, campos);
		return ResponseEntity.status(status).body(body);
	}
}
