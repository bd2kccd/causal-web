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
package edu.pitt.dbmi.ccd.annotations.model;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.Size;

/**
 * Annotation data entity POST request
 *
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class AnnotationDataForm {

    private Long attribute = null;

    private String value = null;

    private List<AnnotationDataForm> children = new ArrayList<>(0);

    public AnnotationDataForm() {
    }

    public AnnotationDataForm(String value) {
        this.value = value;
    }

    public AnnotationDataForm(Long attribute, String value) {
        this.attribute = attribute;
        this.value = value;
    }

    public AnnotationDataForm(Long attribute, @Size(min = 1) List<AnnotationDataForm> children) {
        this.attribute = attribute;
        this.children = new ArrayList<>(children);
    }

    public Long getAttribute() {
        return attribute;
    }

    public void setAttribute(Long attribute) {
        this.attribute = attribute;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<AnnotationDataForm> getChildren() {
        return children;
    }

    public void setChildren(@Size(min = 1) List<AnnotationDataForm> children) {
        this.children = children;
    }
}
