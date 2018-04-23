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

import edu.pitt.dbmi.ccd.annotations.ctrl.SpecificationController;
import edu.pitt.dbmi.ccd.annotations.links.AttributeLinks;
import edu.pitt.dbmi.ccd.annotations.links.SpecificationLinks;
import edu.pitt.dbmi.ccd.db.entity.Attribute;
import edu.pitt.dbmi.ccd.db.entity.Specification;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Assembles Attribute into AttributeResource
 *
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
@Component
public class AttributeResourceAssembler extends ResourceAssemblerSupport<Attribute, AttributeResource> {

    private final SpecificationLinks specLinks;
    private final AttributeLinks attributeLinks;

    @Autowired(required = true)
    public AttributeResourceAssembler(SpecificationLinks specLinks, AttributeLinks attributeLinks) {
        super(SpecificationController.class, AttributeResource.class);
        this.specLinks = specLinks;
        this.attributeLinks = attributeLinks;
    }

    /**
     * convert Attribute to AttributeResource
     *
     * @param attribute entity
     * @return resource
     */
    @Override
    public AttributeResource toResource(Attribute attribute) {
        Assert.notNull(attribute);

        // create resource
        AttributeResource resource = createResourceWithId(attribute.getId(), attribute);

        // make child attributes resources if there are any
        Set<AttributeResource> subAttributes = attribute.getChildren()
                .stream()
                .map(this::toResource)
                .collect(Collectors.toSet());
        if (subAttributes.size() > 0) {
            resource.addSubAttributes(subAttributes);
        }

        // add link to parent attribute if it has one
        if (attribute.getParent() != null) {
            resource.add(attributeLinks.parent(attribute));
        }

        // add link to specification
        Specification specification = attribute.getSpecification();
        resource.add(specLinks.specification(specification));

        return resource;
    }

    /**
     * convert Attributes to AttributeResources
     *
     * @param attributes entities
     * @return List of resources
     */
    @Override
    public List<AttributeResource> toResources(Iterable<? extends Attribute> attributes) {
        // Assert attributes is not empty
        Assert.isTrue(attributes.iterator().hasNext());
        return StreamSupport.stream(attributes.spliterator(), false)
                .map(this::toResource)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new attribute resource with a correct self link Added
     * specification name to self link
     *
     * @param entity must not be {@literal null}.
     * @param id must not be {@literal null}.
     * @return resource
     */
    @Override
    protected AttributeResource createResourceWithId(Object id, Attribute entity, Object... parameters) {
        Assert.notNull(entity);
        Assert.notNull(id);

        AttributeResource instance = instantiateResource(entity);
        instance.add(linkTo(SpecificationController.class, parameters).slash(entity.getSpecification().getName() + "/attributes").slash(id).withSelfRel());
        return instance;
    }

    /**
     * Instantiate AttributeResource with non-default constructor
     *
     * @param attribute entity
     * @return resource
     */
    @Override
    protected AttributeResource instantiateResource(Attribute attribute) {
        Assert.notNull(attribute);
        try {
            return BeanUtils.instantiateClass(AttributeResource.class.getConstructor(Attribute.class), attribute);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            return new AttributeResource();
        }
    }
}
