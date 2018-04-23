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
package edu.pitt.dbmi.ccd.annotations.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import edu.pitt.dbmi.ccd.db.entity.AnnotationData;
import java.util.HashSet;
import java.util.Set;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.Relation;

/**
 * AnnotationData entity DTO representation
 *
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
@Relation(value = "data", collectionRelation = "data")
@JsonPropertyOrder({"id", "attribute", "attributeResource", "value", "subData"})
public final class AnnotationDataResource extends ResourceSupport {

    // content
    private final Long id;
    private final Long attribute;
    private final String value;
    private final Set<AnnotationDataResource> subData = new HashSet<>(0);
    private AttributeResource attributeResource = null;

    /**
     * Empty constructor
     *
     * @return new AnnotationDataResource with empty/null variables
     */
    public AnnotationDataResource() {
        this.id = null;
        this.attribute = null;
        this.value = "";
    }

    /**
     * Constructor
     *
     * @param data content
     */
    public AnnotationDataResource(AnnotationData data) {
        this.id = data.getId();
        this.attribute = (data.getAttribute() != null) ? data.getAttribute().getId() : null;
        this.value = data.getValue();
    }

    /**
     * Constructor
     *
     * @param data content
     * @param links (optional) links to include
     */
    public AnnotationDataResource(AnnotationData data, Link... links) {
        this(data);
        this.add(links);
    }

    /**
     * Get annotation data id
     *
     * @return data id
     */
    @JsonProperty("id")
    public Long getIdentifier() {
        return id;
    }

    /**
     * Get attribute
     *
     * @return attribute id
     */
    @JsonInclude(Include.NON_NULL)
    public Long getAttribute() {
        return attribute;
    }

    public AttributeResource getAttributeResource() {
        return attributeResource;
    }

    public void setAttributeResource(AttributeResource attributeResource) {
        this.attributeResource = attributeResource;
    }

    /**
     * Get attribute value
     *
     * @return value
     */
    @JsonInclude(Include.NON_NULL)
    public String getValue() {
        return value;
    }

    /**
     * Get subData data
     *
     * @return subData data
     */
    @JsonInclude(Include.NON_EMPTY)
    @JsonProperty("subData")
    public Set<AnnotationDataResource> getSubData() {
        return subData;
    }

    /**
     * Add annotation data resource
     *
     * @param subData annotation data resource
     */
    public void addSubData(AnnotationDataResource subData) {
        this.subData.add(subData);
    }

    /**
     * Add multiple annotation data resources
     *
     * @param subData annotation data resources
     */
    public void addSubData(AnnotationDataResource... subData) {
        for (AnnotationDataResource d : subData) {
            addSubData(d);
        }
    }

    /**
     * Add multiple annotation data resources
     *
     * @param subData annotation data resources
     */
    public void addSubData(Set<AnnotationDataResource> subData) {
        this.subData.addAll(subData);
    }
}
