/*
 * Copyright (C) 2015 University of Pittsburgh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package edu.pitt.dbmi.ccd.annotations.exception;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

// logging
/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
@ControllerAdvice
public final class ErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

    private static final String FORBIDDEN_MESSAGE = "Insufficient permission";
    private static final String SERVER_ERROR = "Internal server exception";
    private static final String REQUEST_FAILED = "Request failed";

    /* 400s */
    // 400
    @ExceptionHandler(PropertyReferenceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorMessage handlePropertyReferenceException(PropertyReferenceException ex, HttpServletRequest req) {
        LOGGER.info(ex.getMessage());
        return new ErrorMessage(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorMessage handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest req) {
        LOGGER.info(ex.getMessage());
        StringBuilder message = new StringBuilder("");

        ex.getConstraintViolations()
                .stream()
                .forEach(e -> {
                    message.append("Property: " + e.getPropertyPath());
                    message.append(" Constraint: " + e.getMessageTemplate() + " ");
                });
        return new ErrorMessage(HttpStatus.BAD_REQUEST, message.toString(), req);
    }

//    @ExceptionHandler(VocabularyMismatchException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ResponseBody
//    public ErrorMessage handleAccessUpdateException(VocabularyMismatchException ex, HttpServletRequest req) {
//        LOGGER.info(ex.getMessage());
//        return new ErrorMessage(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
//    }
    // 403
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorMessage handleForbiddenException(ForbiddenException ex, HttpServletRequest req) {
        LOGGER.info(ex.getMessage());
        return new ErrorMessage(HttpStatus.FORBIDDEN, FORBIDDEN_MESSAGE, req);
    }

    // 404
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorMessage handleNotFoundException(NotFoundException ex, HttpServletRequest req) {
        LOGGER.info(ex.getMessage());
        return new ErrorMessage(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    // 409
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ErrorMessage handleDuplicateKeyException(DuplicateKeyException ex, HttpServletRequest req) {
        LOGGER.info(ex.getMessage());
        return new ErrorMessage(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    /* 500s */
    // 500
    @ExceptionHandler(JpaSystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorMessage handleJpaSystemException(JpaSystemException ex, HttpServletRequest req) {
        LOGGER.error(ex.getMessage(), ex);
        return new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, REQUEST_FAILED, req);
    }
}
