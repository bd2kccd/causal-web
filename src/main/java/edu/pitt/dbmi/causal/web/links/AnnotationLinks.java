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
import edu.pitt.dbmi.ccd.db.entity.Annotation;
import edu.pitt.dbmi.ccd.db.entity.AnnotationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RelProvider;
import org.springframework.stereotype.Component;

/**
 * Annotation links
 *
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
@Component
public class AnnotationLinks implements ResourceLinks {

    // annotation links
    public static final String INDEX = "/annotations";
    public static final String ANNOTATION = "/{id}";
    public static final String ANNOTATION_REDACT = "/{id}/redact";
    public static final String ANNOTATION_DATA = "/{id}/data";
    public static final String ANNOTATION_DATA_ID = "/{id}/data/{dataId}";
    public static final String CHILDREN = "/{id}/children";

    // annotations rels
    private final String REL_ANNOTATION;
    private final String REL_ANNOTATIONS;
    private final String REL_PARENT = "parent";
    private final String REL_CHILDREN = "children";
    private final String REL_DATA = "data";

    // query parameters
    // filter
    public static final String USER = "user";
    public static final String GROUP = "group";
    public static final String UPLOAD = "upload";
    public static final String VOCAB = "specification";
    public static final String LEVEL = "level";
    public static final String NAME = "name";
    public static final String REQUIREMENT = "requirement";
    public static final String REDACTED = "showRedacted";

    //search
    public static final String QUERY = "query";
    public static final String NOT = "not";

    // dependencies
    private final EntityLinks entityLinks;
    private final RelProvider relProvider;

    @Autowired(required = true)
    public AnnotationLinks(EntityLinks entityLinks, RelProvider relProvider) {
        this.entityLinks = entityLinks;
        this.relProvider = relProvider;
        REL_ANNOTATION = relProvider.getItemResourceRelFor(AnnotationResource.class);
        REL_ANNOTATIONS = relProvider.getCollectionResourceRelFor(AnnotationResource.class);
    }

    /**
     * Get link to annotation resource collection
     *
     * @return link to collection
     */
    public Link annotations() {
        String template = toTemplate(entityLinks.linkFor(AnnotationResource.class).toString(), USER, GROUP, UPLOAD, VOCAB, LEVEL, NAME, REQUIREMENT, REDACTED, PAGEABLE);
        return new Link(template, REL_ANNOTATIONS);
    }

    /**
     * Get link to annotation resource
     *
     * @param annotation entity
     * @return link to resource
     */
    public Link annotation(Annotation annotation) {
        return entityLinks.linkForSingleResource(AnnotationResource.class, annotation.getId()).withRel(REL_ANNOTATION);
    }

    /**
     * Get link to annotation data resource
     *
     * @param data annotation data entity
     * @return link to resource
     */
    public Link annotationData(AnnotationData data) {
        return entityLinks.linkForSingleResource(AnnotationResource.class, data.getAnnotation().getId()).slash(REL_DATA).slash(data.getId()).withRel(REL_DATA);
    }

    public Link parent(Annotation annotation) {
        return entityLinks.linkForSingleResource(AnnotationResource.class, annotation.getParent().getId()).withRel(REL_PARENT);
    }

    public Link children(Annotation annotation) {
        return entityLinks.linkFor(AnnotationResource.class).slash(annotation.getId()).slash(REL_CHILDREN).withRel(REL_CHILDREN);
    }

    /**
     * Get link to annotation search page
     *
     * @return link to search
     */
    public Link search() {
        String template = toTemplate(entityLinks.linkFor(AnnotationResource.class).slash(SEARCH).toString(), USER, GROUP, UPLOAD, VOCAB, LEVEL, NAME, REQUIREMENT, REDACTED, QUERY, NOT, PAGEABLE);
        return new Link(template, REL_SEARCH);
    }
}
