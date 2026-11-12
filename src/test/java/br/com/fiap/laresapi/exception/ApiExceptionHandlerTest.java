package br.com.fiap.laresapi.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import br.com.fiap.laresapi.dto.ErroResponse;

class ApiExceptionHandlerTest {

	private final ApiExceptionHandler exceptionHandler = new ApiExceptionHandler();

	@Test
	void erroInesperadoRetornaErroPadronizadoSemDetalhesInternos() {
		ResponseEntity<ErroResponse> response = exceptionHandler.handleUnexpectedException(
				new IllegalStateException("detalhe interno"));

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertEquals(500, response.getBody().status());
		assertEquals("Erro interno do servidor", response.getBody().mensagem());
	}

	@Test
	void erroDeClienteDoSpringMvcMantemStatusOriginal() {
		ResponseEntity<ErroResponse> response = exceptionHandler.handleUnexpectedException(
				new HttpRequestMethodNotSupportedException("GET"));

		assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
		assertEquals(405, response.getBody().status());
	}
}
