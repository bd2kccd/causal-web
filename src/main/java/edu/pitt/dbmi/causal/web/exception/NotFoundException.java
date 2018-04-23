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

import java.util.stream.IntStream;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class NotFoundException extends RuntimeException {

    private static final String NOT_FOUND = "%s not found";
    private static final String WITH = " with ";
    private static final String AND = " and ";
    private static final String SEP = ": ";

    private final String entity;
    private final String[] fields;
    private final Object[] values;
    private final String message;

    /**
     * Constructor
     *
     * @param entity name of entity not found
     */
    public NotFoundException(String entity) {
        super();
        this.entity = entity;
        this.fields = null;
        this.values = null;
        this.message = buildMessage();
    }

    /**
     * Constructor
     *
     * @param entity name of entity not found
     * @param fields fields of entity used
     * @param values values looked for
     */
    public NotFoundException(String entity, String[] fields, Object[] values) {
        super();
        this.entity = entity;
        this.fields = fields;
        this.values = values;
        this.message = buildMessage();
    }

    /**
     * Constructor
     *
     * @param entity name of entity not found
     * @param field field of entity used
     * @param value value looked for
     */
    public NotFoundException(String entity, String field, Object value) {
        this(entity, new String[]{field}, new Object[]{value});
    }

    /**
     * Get entity
     *
     * @return entity
     */
    public String getEntity() {
        return entity;
    }

    /**
     * Get field
     *
     * @return field
     */
    public String[] getFields() {
        return fields;
    }

    /**
     * Get value
     *
     * @return value
     */
    public Object[] getValues() {
        return values;
    }

    /**
     * Build exception message
     */
    private String buildMessage() {
        final StringBuilder builder = new StringBuilder(String.format(NOT_FOUND, entity));
        final int len = (isEmpty(fields) || isEmpty(values))
                ? 0
                : Math.min(fields.length, values.length);
        if (len > 0) {
            builder.append(WITH).append(fields[0]).append(SEP).append(values[0]);
            if (len > 1) {
                IntStream.range(1, len)
                        .forEach(i -> {
                            final String f = fields[i];
                            final Object v = values[i];
                            if (!isEmpty(f) && !isEmpty(v)) {
                                builder.append(AND).append(f).append(SEP).append(v);
                            }
                        });
            }
        }
        return builder.toString();
    }

    /**
     * Get message
     *
     * @return message
     */
    @Override
    public String getMessage() {
        return message;
    }

}
