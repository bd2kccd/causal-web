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

import edu.pitt.dbmi.ccd.annotations.links.AnnotationLinks;
import edu.pitt.dbmi.ccd.annotations.links.SpecificationLinks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class IndexResourceAssembler {

    // Message to display
    private static final String message = "CCD Annotations API v0.8.0 (beta)";

    private final AnnotationLinks annotationLinks;
    private final SpecificationLinks specificationLinks;

    @Autowired(required = true)
    public IndexResourceAssembler(
            AnnotationLinks annotationLinks,
            SpecificationLinks specificationLinks) {
        this.annotationLinks = annotationLinks;
        this.specificationLinks = specificationLinks;
    }

    /**
     * Build index
     *
     * @return index
     */
    public IndexResource buildIndex() {
        final IndexResource resource = new IndexResource(
                message,
                annotationLinks.annotations(),
                specificationLinks.specifications()
        );
        return resource;
    }
}
