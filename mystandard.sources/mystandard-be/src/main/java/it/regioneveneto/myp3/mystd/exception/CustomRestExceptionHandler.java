/**
 *     My Standard
 *     Copyright (C) 2022  Regione Veneto
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package it.regioneveneto.myp3.mystd.exception;

import it.regioneveneto.myp3.mystd.bean.MyStandardResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomRestExceptionHandler.class);

    // 400

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        //
        final List<String> errors = new ArrayList<String>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
            logger.error("MethodArgumentNotValidException: " + error.getField() + ": " + error.getDefaultMessage(), ex);
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
            logger.error("MethodArgumentNotValidException: " + error.getObjectName() + ": " + error.getDefaultMessage(), ex);

        }


        final MyStandardResult myStandardResult = new MyStandardResult(false, "errore di validazione", errors);
        return handleExceptionInternal(ex, myStandardResult, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Throwable mostSpecificCause = ex.getMostSpecificCause();
        MyStandardResult errorMessage;
        if (mostSpecificCause != null) {
            String exceptionName = mostSpecificCause.getClass().getName();
            String message = mostSpecificCause.getMessage();
            logger.error("HttpMessageNotReadableException: {} Message: {}", exceptionName, message, ex);
            errorMessage = new MyStandardResult(false, "Errore nella deserializzazione del messaggio");
        } else {
            logger.error("HttpMessageNotReadableException. Message: {}", ex.getMessage(), ex);
            errorMessage = new MyStandardResult(false, "Errore nella deserializzazione del messaggio");
        }
        return new ResponseEntity(errorMessage, headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(final BindException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        //
        final List<String> errors = new ArrayList<String>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }

        logger.error("BindException. Message: {}", ex.getLocalizedMessage(), ex);
        final MyStandardResult myStandardResult = new MyStandardResult(false, ex.getLocalizedMessage(), errors);
        return handleExceptionInternal(ex, myStandardResult, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(final TypeMismatchException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        //
        final String error = ex.getValue() + " value for " + ex.getPropertyName() + " should be of type " + ex.getRequiredType();

        logger.error("TypeMismatchException: {}", error, ex);
        final MyStandardResult myStandardResult = new MyStandardResult(false, error);
        return new ResponseEntity<>(myStandardResult, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(final MissingServletRequestPartException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        //
        final String error = ex.getRequestPartName() + " part is missing";

        logger.error("MissingServletRequestPartException: {}", error, ex);
        final MyStandardResult myStandardResult = new MyStandardResult(false, error);
        return new ResponseEntity<>(myStandardResult, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(final MissingServletRequestParameterException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        //
        final String error = ex.getParameterName() + " parameter is missing";
        logger.error("MissingServletRequestParameterException: {}", error, ex);
        final MyStandardResult myStandardResult = new MyStandardResult(false, error);
        return new ResponseEntity<>(myStandardResult, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    //

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class })
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(final MethodArgumentTypeMismatchException ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        //
        final String error = ex.getName() + " should be of type " + ex.getRequiredType().getName();

        logger.error("MethodArgumentTypeMismatchException: {}", error, ex);
        final MyStandardResult myStandardResult = new MyStandardResult(false, error);
        return new ResponseEntity<>(myStandardResult, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ ConstraintViolationException.class })
    public ResponseEntity<Object> handleConstraintViolation(final ConstraintViolationException ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        //
        final List<String> errors = new ArrayList<String>();
        for (final ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getRootBeanClass().getName() + " " + violation.getPropertyPath() + ": " + violation.getMessage());
            logger.error("ConstraintViolationException: {}", violation.getRootBeanClass().getName() + " " + violation.getPropertyPath() + ": " + violation.getMessage(), ex);
        }

        final MyStandardResult myStandardResult = new MyStandardResult(false, "Errore constraint", errors);
        return new ResponseEntity<>(myStandardResult, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    // 404

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(final NoHandlerFoundException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        //
        final String error = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();

        logger.error("NoHandlerFoundException: {}", error, ex);
        final MyStandardResult myStandardResult = new MyStandardResult(false, error);
        return new ResponseEntity<>(myStandardResult, new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    // 405

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(final HttpRequestMethodNotSupportedException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        //
        final StringBuilder builder = new StringBuilder();
        builder.append(ex.getMethod());
        builder.append(" method is not supported for this request. Supported methods are ");
        ex.getSupportedHttpMethods().forEach(t -> builder.append(t + " "));

        logger.error("HttpRequestMethodNotSupportedException: {}", builder.toString());

        final MyStandardResult myStandardResult = new MyStandardResult(false, builder.toString());
        return new ResponseEntity<>(myStandardResult, new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    // 415

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(final HttpMediaTypeNotSupportedException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        //
        final StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t + " "));

        logger.error("HttpMediaTypeNotSupportedException: {}", builder.toString());

        final MyStandardResult myStandardResult = new MyStandardResult(false, builder.substring(0, builder.length() - 2));
        return new ResponseEntity<>(myStandardResult, new HttpHeaders(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }


    @ExceptionHandler({ MyStandardException.class })
    public ResponseEntity<Object> handleMyStandardException(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error("MyStandardException: {}", ex.getMessage(), ex);
        //
        final MyStandardResult myStandardResult = new MyStandardResult(false ,"Errore nelle operazioni su MyStandard");
        return new ResponseEntity<>(myStandardResult, new HttpHeaders(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 500

    @ExceptionHandler({ Exception.class })
    public ResponseEntity<Object> handleAll(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error("Exception: {}", ex.getMessage(),  ex);
        //
        final MyStandardResult myStandardResult = new MyStandardResult(false, "Errore generico nelle operazioni su MyStandard");
        return new ResponseEntity<>(myStandardResult, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity handleMaxSizeException(Exception e) {
        logger.info(e.getClass().getName());
        logger.error("handleMaxSizeException: Errore per superamento limite massimo per la dimensione dei file in upload",e);

        final MyStandardResult myStandardResult = new MyStandardResult(false, "Errore per superamento limite massimo per la dimensione dei file in upload");
        return new ResponseEntity<>(myStandardResult,  HttpStatus.INTERNAL_SERVER_ERROR);
    }
}