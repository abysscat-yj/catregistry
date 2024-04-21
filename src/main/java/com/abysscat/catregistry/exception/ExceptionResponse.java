package com.abysscat.catregistry.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * Exception response.
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/21 23:55
 */
@AllArgsConstructor
@Data
public class ExceptionResponse {

	private HttpStatus httpStatus;

	private String message;

}
