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
package edu.pitt.dbmi.ccd.annotations.links;

import edu.pitt.dbmi.ccd.annotations.resource.AnnotationResource;
import edu.pitt.dbmi.ccd.annotations.resource.AttributeResource;
import edu.pitt.dbmi.ccd.annotations.resource.SpecificationResource;
import edu.pitt.dbmi.ccd.db.entity.Attribute;
import edu.pitt.dbmi.ccd.db.entity.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RelProvider;
import org.springframework.stereotype.Component;

/**
 * Specification links
 *
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
@Component
public class SpecificationLinks implements ResourceLinks {

    // specification links
    public static final String INDEX = "/specifications";
    public static final String SPECIFICATION = "/{id}";
    public static final String ATTRIBUTES = "/{id}/attributes";
    public static final String ATTRIBUTE = "/{vId}/attributes/{aId}";

    // specification rels
    public final String REL_SPECIFICATION;
    public final String REL_SPECIFICATIONS;
    public final String REL_ATTRIBUTE;
    public final String REL_ATTRIBUTES;
    public final String REL_ANNOS;

    // query parameters
    private static final String NAME = "name";
    private static final String LEVEL = "level";
    private static final String REQUIRED = "required";
    private static final String QUERY = "query";
    private static final String NOT = "not";

    // dependencies
    private final EntityLinks entityLinks;
    private final RelProvider relProvider;

    @Autowired(required = true)
    public SpecificationLinks(EntityLinks entityLinks, RelProvider relProvider) {
        this.entityLinks = entityLinks;
        this.relProvider = relProvider;
        REL_SPECIFICATION = relProvider.getItemResourceRelFor(SpecificationResource.class);
        REL_SPECIFICATIONS = relProvider.getCollectionResourceRelFor(SpecificationResource.class);
        REL_ATTRIBUTE = relProvider.getItemResourceRelFor(AttributeResource.class);
        REL_ATTRIBUTES = relProvider.getCollectionResourceRelFor(AttributeResource.class);
        REL_ANNOS = relProvider.getCollectionResourceRelFor(AnnotationResource.class);
    }

    /**
     * Get link to specification resource collection
     *
     * @return link to collection
     */
    public Link specifications() {
        String template = toTemplate(entityLinks.linkFor(SpecificationResource.class).toString(), PAGEABLE);
        return new Link(template, REL_SPECIFICATIONS);
    }

    /**
     * Get link to specification resource
     *
     * @param specification specification
     * @return link to resource
     */
    public Link specification(Specification specification) {
        return entityLinks.linkForSingleResource(SpecificationResource.class, specification.getId()).withRel(REL_SPECIFICATION);
    }

    /**
     * Get link to vocbulary attributes
     *
     * @param vocab specification
     */
    public Link attributes(Specification vocab) {
        String template = toTemplate(entityLinks.linkForSingleResource(SpecificationResource.class, vocab.getId()).slash(REL_ATTRIBUTES).toString(), NAME, LEVEL, REQUIRED, PAGEABLE);
        return new Link(template, REL_ATTRIBUTES);
    }

    /**
     * Get link to vocbulary attribute
     *
     * @param vocab specification
     * @param attribute attribute id
     */
    public Link attribute(Specification vocab, Attribute attribute) {
        return entityLinks.linkForSingleResource(SpecificationResource.class, vocab.getId()).slash(REL_ATTRIBUTES).slash(attribute.getId()).withRel(REL_ATTRIBUTE);
    }

    /**
     * Get link to specification search page
     *
     * @return link to search
     */
    public Link search() {
        String template = toTemplate(entityLinks.linkFor(SpecificationResource.class).slash(SEARCH).toString(), QUERY, NOT, PAGEABLE);
        return new Link(template, REL_SEARCH);
    }

    /**
     * Get link to vocab's annotations
     *
     * @return link to annotations
     */
    public Link annotations(Specification specification) {
        String template = linkToCollection(entityLinks.linkFor(AnnotationResource.class).toString(), AnnotationLinks.VOCAB, specification.getId().toString());
        return new Link(template, REL_ANNOS);
    }
}
