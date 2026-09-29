package com.jiostar.watchlist.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jiostar.watchlist.api.dto.ErrorResponse;
import com.jiostar.watchlist.domain.WatchlistErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(WatchlistFullException.class)
	public ResponseEntity<ErrorResponse> watchlistFull(WatchlistFullException ex, HttpServletRequest request) {
		log.warn("Watchlist full: path={} message={}", request.getRequestURI(), ex.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(ErrorResponse.of(WatchlistErrorCode.WATCHLIST_FULL, ex.getMessage(), request.getRequestURI(),
						ex.getMaxSize()));
	}

	@ExceptionHandler({ WatchlistUserNotAllowedException.class, IllegalArgumentException.class })
	public ResponseEntity<ErrorResponse> validation(RuntimeException ex, HttpServletRequest request) {
		log.warn("Client error: path={} message={}", request.getRequestURI(), ex.getMessage());
		return ResponseEntity.badRequest()
				.body(ErrorResponse.of(WatchlistErrorCode.VALIDATION_ERROR, ex.getMessage(), request.getRequestURI(),
						null));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> bodyValidation(MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(error -> error.getField() + " " + error.getDefaultMessage())
				.orElse("Validation failed");
		log.warn("Request body validation failed: path={} message={}", request.getRequestURI(), message);
		return ResponseEntity.badRequest()
				.body(ErrorResponse.of(WatchlistErrorCode.VALIDATION_ERROR, message, request.getRequestURI(), null));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> parameterValidation(ConstraintViolationException ex,
			HttpServletRequest request) {
		String message = ex.getConstraintViolations().stream()
				.findFirst()
				.map(v -> v.getPropertyPath() + " " + v.getMessage())
				.orElse("Validation failed");
		log.warn("Request parameter validation failed: path={} message={}", request.getRequestURI(), message);
		return ResponseEntity.badRequest()
				.body(ErrorResponse.of(WatchlistErrorCode.VALIDATION_ERROR, message, request.getRequestURI(), null));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> unreadableBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
		log.warn("Malformed request body: path={}", request.getRequestURI());
		return ResponseEntity.badRequest()
				.body(ErrorResponse.of(WatchlistErrorCode.VALIDATION_ERROR, "Malformed JSON request body",
						request.getRequestURI(), null));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> internal(Exception ex, HttpServletRequest request) {
		log.error("Unhandled error: path={}", request.getRequestURI(), ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.of(WatchlistErrorCode.INTERNAL_ERROR, "Unexpected error", request.getRequestURI(),
						null));
	}

}
