package com.ronney.finance.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    private final HttpServletRequest request = createRequest(
            "/api/v1/test"
    );

    @Test
    void shouldReturnStandardErrorResponseForNotFound() {

        var response = handler.handleNotFound(
                new ResourceNotFoundException("Resource not found."),
                request
        );

        assertErrorResponse(
                response.getBody(),
                HttpStatus.NOT_FOUND,
                "Resource not found."
        );
    }

    @Test
    void shouldReturnStandardErrorResponseForBadRequest() {

        var response = handler.handleBadRequest(
                new IllegalArgumentException("Invalid request."),
                request
        );

        assertErrorResponse(
                response.getBody(),
                HttpStatus.BAD_REQUEST,
                "Invalid request."
        );
    }

    @Test
    void shouldReturnStandardErrorResponseForValidationError()
            throws NoSuchMethodException {

        var bindingResult = new BeanPropertyBindingResult(
                new Object(),
                "request"
        );

        bindingResult.addError(
                new FieldError(
                        "request",
                        "name",
                        "must not be blank"
                )
        );

        MethodParameter parameter =
                new MethodParameter(
                        GlobalExceptionHandlerTest.class
                                .getDeclaredMethod(
                                        "validationMethod",
                                        String.class
                                ),
                        0
                );

        var exception = new MethodArgumentNotValidException(
                parameter,
                bindingResult
        );

        var response = handler.handleValidation(
                exception,
                request
        );

        assertErrorResponse(
                response.getBody(),
                HttpStatus.BAD_REQUEST,
                "name: must not be blank"
        );
    }

    @Test
    void shouldReturnStandardErrorResponseForMalformedJson() {

        var response = handler.handleMalformedJson(
                null,
                request
        );

        assertErrorResponse(
                response.getBody(),
                HttpStatus.BAD_REQUEST,
                "Malformed JSON request."
        );
    }

    @Test
    void shouldReturnStandardErrorResponseForDataIntegrityViolation() {

        var cause = new RuntimeException(
                "fk_transaction_financial_account"
        );

        var exception = new DataIntegrityViolationException(
                "constraint violation",
                cause
        );

        var response = handler.handleDataIntegrityViolation(
                exception,
                request
        );

        assertErrorResponse(
                response.getBody(),
                HttpStatus.CONFLICT,
                """
                This financial account cannot be deleted because it is associated with one or more transactions.
                Remove or reassign the related transactions before deleting the account.
                """.strip()
        );
    }

    @Test
    void shouldReturnStandardErrorResponseForBadCredentials() {

        var response = handler.handleBadCredentials(
                new org.springframework.security.authentication
                        .BadCredentialsException("invalid"),
                request
        );

        assertErrorResponse(
                response.getBody(),
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password."
        );
    }

    @Test
    void shouldReturnStandardErrorResponseForUsernameNotFound() {

        var response = handler.handleUsernameNotFound(
                new org.springframework.security.core.userdetails
                        .UsernameNotFoundException("user"),
                request
        );

        assertErrorResponse(
                response.getBody(),
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password."
        );
    }

    @Test
    void shouldReturnStandardErrorResponseForIllegalState() {

        var response = handler.handleIllegalStateException(
                new IllegalStateException("Operation cannot be performed."),
                request
        );

        assertErrorResponse(
                response.getBody(),
                HttpStatus.CONFLICT,
                "Operation cannot be performed."
        );
    }

    @Test
    void shouldReturnStandardErrorResponseForBusinessException() {

        var response = handler.handleBusinessException(
                new BusinessException(
                        "Business rule violation.",
                        HttpStatus.UNPROCESSABLE_ENTITY
                ),
                request
        );

        assertErrorResponse(
                response.getBody(),
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Business rule violation."
        );
    }

    private void validationMethod(String value) {
    }

    private static MockHttpServletRequest createRequest(
            String uri
    ) {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI(uri);

        return request;
    }

    private static void assertErrorResponse(
            ErrorResponse response,
            HttpStatus expectedStatus,
            String expectedMessage
    ) {
        assertNotNull(response);

        assertNotNull(response.timestamp());
        assertEquals(
                expectedStatus.value(),
                response.status()
        );
        assertEquals(
                expectedStatus.getReasonPhrase(),
                response.error()
        );
        assertEquals(
                expectedMessage,
                response.message()
        );
        assertEquals(
                "/api/v1/test",
                response.path()
        );
    }
}