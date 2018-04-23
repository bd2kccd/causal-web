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
package edu.pitt.dbmi.ccd.annotations.ctrl;

import static org.springframework.util.StringUtils.isEmpty;

import javax.servlet.http.HttpServletRequest;

import edu.pitt.dbmi.ccd.annotations.exception.AttributeNotFoundException;
import edu.pitt.dbmi.ccd.annotations.exception.NotFoundException;
import edu.pitt.dbmi.ccd.annotations.exception.SpecificationNotFoundException;
import edu.pitt.dbmi.ccd.annotations.links.SpecificationLinks;
import edu.pitt.dbmi.ccd.annotations.resource.AttributePagedResourcesAssembler;
import edu.pitt.dbmi.ccd.annotations.resource.AttributeResource;
import edu.pitt.dbmi.ccd.annotations.resource.AttributeResourceAssembler;
import edu.pitt.dbmi.ccd.annotations.resource.SpecificationPagedResourcesAssembler;
import edu.pitt.dbmi.ccd.annotations.resource.SpecificationResource;
import edu.pitt.dbmi.ccd.annotations.resource.SpecificationResourceAssembler;
import edu.pitt.dbmi.ccd.db.entity.Attribute;
import edu.pitt.dbmi.ccd.db.entity.Specification;
import edu.pitt.dbmi.ccd.db.service.AttributeService;
import edu.pitt.dbmi.ccd.db.service.SpecificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for Specification endpoints
 *
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
@RestController
@ExposesResourceFor(SpecificationResource.class)
@RequestMapping(value = SpecificationLinks.INDEX)
public class SpecificationController {

    // loggers
    private static final Logger LOGGER = LoggerFactory.getLogger(SpecificationController.class);

    // servlet
    private final HttpServletRequest request;

    // services and components
    private final SpecificationLinks specificationLinks;
    private final SpecificationService specificationService;
    private final AttributeService attributeService;
    private final SpecificationResourceAssembler assembler;
    private final SpecificationPagedResourcesAssembler pageAssembler;
    private final AttributeResourceAssembler attributeAssembler;
    private final AttributePagedResourcesAssembler attributePageAssembler;

    @Autowired(required = true)
    public SpecificationController(
            HttpServletRequest request,
            SpecificationLinks specificationLinks,
            SpecificationService specificationService,
            AttributeService attributeService,
            SpecificationResourceAssembler assembler,
            SpecificationPagedResourcesAssembler pageAssembler,
            AttributeResourceAssembler attributeAssembler,
            AttributePagedResourcesAssembler attributePageAssembler) {
        this.request = request;
        this.specificationLinks = specificationLinks;
        this.specificationService = specificationService;
        this.attributeService = attributeService;
        this.assembler = assembler;
        this.pageAssembler = pageAssembler;
        this.attributeAssembler = attributeAssembler;
        this.attributePageAssembler = attributePageAssembler;
    }

    /* GET requests */
    /**
     * Get all vocabularies
     *
     * @param pageable page request
     * @return page of vocabularies
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PagedResources<SpecificationResource> vocabularies(Pageable pageable) {
        final Page<Specification> page = specificationService.findAll(pageable);
        final PagedResources<SpecificationResource> pagedResources = pageAssembler.toResource(page, assembler, request);
        pagedResources.add(specificationLinks.search());
        return pagedResources;
    }

    /**
     * Get single specification
     *
     * @param id specification id
     * @return specification
     */
    @RequestMapping(value = SpecificationLinks.SPECIFICATION, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SpecificationResource specification(@PathVariable Long id) throws NotFoundException {
        final Specification specification = specificationService.findById(id);
        if (specification == null) {
            throw new SpecificationNotFoundException(id);
        }
        final SpecificationResource resource = assembler.toResource(specification);
        return resource;
    }

    /**
     * Get specification attributes
     *
     * @param id specification id
     * @return page of attributes
     */
    @RequestMapping(value = SpecificationLinks.ATTRIBUTES, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PagedResources<AttributeResource> attributes(
            @PathVariable Long id,
            @RequestParam(value = "level", required = false) String level,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "requirement", required = false) Boolean required,
            @PageableDefault(size = 20, sort = {"id"}) Pageable pageable)
            throws NotFoundException {
        final Specification specification = specificationService.findById(id);
        if (specification == null) {
            throw new SpecificationNotFoundException(id);
        }
        final Page<Attribute> page;
        if (isEmpty(level) && isEmpty(name) && isEmpty(required)) {
            page = attributeService.searchNoParent(specification.getId(), name, level, required, pageable);
        } else {
            page = attributeService.search(specification.getId(), name, level, required, pageable);
        }
        final PagedResources<AttributeResource> pagedResources = attributePageAssembler.toResource(page, attributeAssembler, request);
        return pagedResources;
    }

    /**
     * Get specification attribute
     *
     * @param vId specification id
     * @param aId attribute id
     * @return page of attributes
     */
    @RequestMapping(value = SpecificationLinks.ATTRIBUTE, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AttributeResource attribute(@PathVariable Long vId, @PathVariable Long aId) throws NotFoundException {
        final Specification specification = specificationService.findById(vId);
        if (specification == null) {
            throw new SpecificationNotFoundException(vId);
        }
        final Attribute attribute = specification.getAttributes()
                .stream()
                .filter(a -> a.getId().equals(aId))
                .findFirst()
                .orElseThrow(() -> new AttributeNotFoundException(specification.getId(), aId));
        final AttributeResource resource = attributeAssembler.toResource(attribute);
        return resource;
    }

    /**
     * Search vocabularies
     *
     * @param query search terms (nullable)
     * @param pageable page request
     * @return page of vocabularies matching parameters
     */
    @RequestMapping(value = SpecificationLinks.SEARCH, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PagedResources<SpecificationResource> search(
            @RequestParam(value = "query", required = false) String query,
            Pageable pageable) {
        if (query == null) {
            query = "";
        }
        final Page<Specification> page = specificationService.search(query, pageable);
        final PagedResources<SpecificationResource> pagedResources = pageAssembler.toResource(page, assembler, request);
        return pagedResources;
    }
}
