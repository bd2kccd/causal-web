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
import edu.pitt.dbmi.ccd.db.entity.Attribute;
import java.util.HashSet;
import java.util.Set;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.Relation;

/**
 * Attribute entity DTO representation
 *
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
@Relation(value = "attribute", collectionRelation = "attributes")
@JsonPropertyOrder({"id", "level", "name", "requirementLevel", "subAttributes"})
public final class AttributeResource extends ResourceSupport {

    // content
    private final Long id;
    private final String level;
    private final String name;
    private final String requirementLevel;
    private final Set<AttributeResource> subAttributes = new HashSet<>(0);

    /**
     * Empty constructor
     *
     * @return new AttributeResource with empty/null variables
     */
    protected AttributeResource() {
        this.id = null;
        this.level = "";
        this.name = "";
        this.requirementLevel = "";
    }

    /**
     * Constructor
     *
     * @param attribute content
     * @return new AttributeResource
     */
    public AttributeResource(Attribute attribute) {
        this.id = attribute.getId();
        this.level = attribute.getLevel();
        this.name = attribute.getName();
        this.requirementLevel = attribute.getRequirementLevel();
    }

    /**
     * Constructor
     *
     * @param attribute content
     * @param links (optional) links to include
     * @return new AttributeResource
     */
    public AttributeResource(Attribute attribute, Link... links) {
        this(attribute);
        this.add(links);
    }

    /**
     * get attribute id relative to specification
     *
     * @return id
     */
    @JsonProperty("id")
    public Long getIdentifer() {
        return id;
    }

    /**
     * get attribute level
     *
     * @return level
     */
    @JsonInclude(Include.NON_NULL)
    public String getLevel() {
        return level;
    }

    /**
     * get attribute name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * get attribute requirement level
     *
     * @return requirement level
     */
    @JsonInclude(Include.NON_NULL)
    public String getRequirementLevel() {
        return requirementLevel;
    }

    /**
     * Get sub attributes
     *
     * @return child attributes
     */
    @JsonInclude(Include.NON_EMPTY)
    public Set<AttributeResource> getSubAttributes() {
        return subAttributes;
    }

    /**
     * Add attribute resource
     *
     * @param subAttribute attribute resource
     */
    public void addSubAttribute(AttributeResource subAttribute) {
        this.subAttributes.add(subAttribute);
    }

    /**
     * Add multiple attribute resources
     *
     * @param subAttribute attribute resources
     */
    public void addSubAttributes(AttributeResource... subAttribute) {
        for (AttributeResource a : subAttribute) {
            addSubAttribute(a);
        }
    }

    /**
     * Add multiple attribute resources
     *
     * @param subAttribute attribute resources
     */
    public void addSubAttributes(Set<AttributeResource> subAttribute) {
        this.subAttributes.addAll(subAttribute);
    }
}
