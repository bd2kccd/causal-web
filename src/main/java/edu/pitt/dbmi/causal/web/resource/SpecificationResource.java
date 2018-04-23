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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import edu.pitt.dbmi.ccd.db.entity.Specification;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.Relation;

/**
 * Specification entity DTO representation
 *
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
@Relation(value = "specification", collectionRelation = "vocabularies")
@JsonPropertyOrder({"id", "name", "description"})
public final class SpecificationResource extends ResourceSupport {

    // content
    private final Long id;
    private final String name;
    private final String description;

    /**
     * Empty constructor
     *
     * @return SpecificationResource with empty variables
     */
    protected SpecificationResource() {
        this.id = null;
        this.name = "";
        this.description = "";
    }

    /**
     * Constructor
     *
     * @param vocab content
     * @return new SpecificationResource
     */
    public SpecificationResource(Specification vocab) {
        this.id = vocab.getId();
        this.name = vocab.getName();
        this.description = vocab.getDescription();
    }

    /**
     * Constructor
     *
     * @param vocab content
     * @param links (optional) links to include
     * @return new SpecificationResource
     */
    public SpecificationResource(Specification vocab, Link... links) {
        this(vocab);
        this.add(links);
    }

    /**
     * Get id
     *
     * @return id
     */
    @JsonProperty("id")
    public Long getIdentifier() {
        return id;
    }

    /**
     * Get name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Get description
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }
}
