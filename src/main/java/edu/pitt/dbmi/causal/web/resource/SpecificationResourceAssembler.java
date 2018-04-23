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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import edu.pitt.dbmi.ccd.annotations.ctrl.SpecificationController;
import edu.pitt.dbmi.ccd.annotations.links.SpecificationLinks;
import edu.pitt.dbmi.ccd.db.entity.Specification;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Assembles Specification into SpecificationResource
 *
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
@Component
public class SpecificationResourceAssembler extends ResourceAssemblerSupport<Specification, SpecificationResource> {

    private final SpecificationLinks specificationLinks;

    @Autowired(required = true)
    public SpecificationResourceAssembler(SpecificationLinks specificationLinks) {
        super(SpecificationController.class, SpecificationResource.class);
        this.specificationLinks = specificationLinks;
    }

    /**
     * convert Specification to SpecificationResource
     *
     * @param vocabulary entity
     * @return resource
     */
    @Override
    public SpecificationResource toResource(Specification vocabulary) throws IllegalArgumentException {
        Assert.notNull(vocabulary);
        SpecificationResource resource = createResourceWithId(vocabulary.getId(), vocabulary);
        resource.add(specificationLinks.attributes(vocabulary));
        resource.add(specificationLinks.annotations(vocabulary));
        return resource;
    }

    /**
     * convert Vocabularies to SpecificationResources
     *
     * @param vocabularies entities
     * @return List of resources
     */
    @Override
    public List<SpecificationResource> toResources(Iterable<? extends Specification> vocabularies) throws IllegalArgumentException {
        // Assert vocabularies is not empty
        Assert.isTrue(vocabularies.iterator().hasNext());
        return StreamSupport.stream(vocabularies.spliterator(), false)
                .map(this::toResource)
                .collect(Collectors.toList());
    }

    /**
     * Instantiate SpecificationResource with non-default constructor
     *
     * @param vocabulary entity
     * @return resource
     */
    @Override
    protected SpecificationResource instantiateResource(Specification vocabulary) throws IllegalArgumentException {
        Assert.notNull(vocabulary);
        try {
            return BeanUtils.instantiateClass(SpecificationResource.class.getConstructor(Specification.class), vocabulary);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            return new SpecificationResource();
        }
    }
}
