package com.abysscat.catregistry.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception handler.
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/21 23:54
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ExceptionResponse handleException(Exception e) {
		return new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
	}
}
