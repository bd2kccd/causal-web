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

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import edu.pitt.dbmi.ccd.annotations.exception.AccessNotFoundException;
import edu.pitt.dbmi.ccd.annotations.exception.AccessUpdateException;
import edu.pitt.dbmi.ccd.annotations.exception.AnnotationDataNotFoundException;
import edu.pitt.dbmi.ccd.annotations.exception.AnnotationNotFoundException;
import edu.pitt.dbmi.ccd.annotations.exception.AnnotationTargetNotFoundException;
import edu.pitt.dbmi.ccd.annotations.exception.AttributeNotFoundException;
import edu.pitt.dbmi.ccd.annotations.exception.ForbiddenException;
import edu.pitt.dbmi.ccd.annotations.exception.GroupNotFoundException;
import edu.pitt.dbmi.ccd.annotations.exception.NotFoundException;
import edu.pitt.dbmi.ccd.annotations.exception.SpecificationNotFoundException;
import edu.pitt.dbmi.ccd.annotations.links.AnnotationLinks;
import edu.pitt.dbmi.ccd.annotations.model.AnnotationDataForm;
import edu.pitt.dbmi.ccd.annotations.model.AnnotationForm;
import edu.pitt.dbmi.ccd.annotations.resource.AnnotationDataPagedResourcesAssembler;
import edu.pitt.dbmi.ccd.annotations.resource.AnnotationDataResource;
import edu.pitt.dbmi.ccd.annotations.resource.AnnotationDataResourceAssembler;
import edu.pitt.dbmi.ccd.annotations.resource.AnnotationPagedResourcesAssembler;
import edu.pitt.dbmi.ccd.annotations.resource.AnnotationResource;
import edu.pitt.dbmi.ccd.annotations.resource.AnnotationResourceAssembler;
import edu.pitt.dbmi.ccd.db.entity.Access;
import edu.pitt.dbmi.ccd.db.entity.Annotation;
import edu.pitt.dbmi.ccd.db.entity.AnnotationData;
import edu.pitt.dbmi.ccd.db.entity.AnnotationTarget;
import edu.pitt.dbmi.ccd.db.entity.Attribute;
import edu.pitt.dbmi.ccd.db.entity.Group;
import edu.pitt.dbmi.ccd.db.entity.Specification;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.AnnotationDataService;
import edu.pitt.dbmi.ccd.db.service.AnnotationService;
import edu.pitt.dbmi.ccd.db.service.AttributeService;
import edu.pitt.dbmi.ccd.db.service.SpecificationService;
import edu.pitt.dbmi.ccd.security.userDetails.UserAccountDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// logging
/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
@RestController
@ExposesResourceFor(AnnotationResource.class)
@RequestMapping(AnnotationLinks.INDEX)
public class AnnotationController {

    // loggers
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationController.class);

    // servlet
    private final HttpServletRequest request;

    // services and components
    private final AnnotationLinks annotationLinks;
    private final AnnotationService annotationService;
    private final AnnotationDataService annotationDataService;
    private final SpecificationService specificationService;
    private final AttributeService attributeService;
    private final AnnotationResourceAssembler assembler;
    private final AnnotationPagedResourcesAssembler pageAssembler;
    private final AnnotationDataResourceAssembler dataAssembler;
    private final AnnotationDataPagedResourcesAssembler dataPageAssembler;

    @Autowired(required = true)
    public AnnotationController(
            HttpServletRequest request,
            AnnotationLinks annotationLinks,
            AnnotationService annotationService,
            AnnotationDataService annotationDataService,
            SpecificationService specificationService,
            AttributeService attributeService,
            AnnotationResourceAssembler assembler,
            AnnotationPagedResourcesAssembler pageAssembler,
            AnnotationDataResourceAssembler dataAssembler,
            AnnotationDataPagedResourcesAssembler dataPageAssembler) {
        this.request = request;
        this.annotationLinks = annotationLinks;
        this.annotationService = annotationService;
        this.annotationDataService = annotationDataService;
        this.specificationService = specificationService;
        this.attributeService = attributeService;
        this.assembler = assembler;
        this.pageAssembler = pageAssembler;
        this.dataAssembler = dataAssembler;
        this.dataPageAssembler = dataPageAssembler;
    }

    /* GET requests */
    /**
     * Get all annotations
     *
     * @param principal authenticated user
     * @param user username (optional)
     * @param group group name (nullable)
     * @param target target id (nullable)
     * @param spec specification name (nullable)
     * @param attributeLevel attribute level (nullable)
     * @param attributeName attribute name (nullable)
     * @param attributeRequirementLevel attribute requirement level (nullable)
     * @param pageable page request
     * @return page of annotations
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PagedResources<AnnotationResource> annotations(
            @AuthenticationPrincipal UserAccountDetails principal,
            @RequestParam(value = "user", required = false) String user,
            @RequestParam(value = "group", required = false) String group,
            @RequestParam(value = "target", required = false) Long target,
            @RequestParam(value = "spec", required = false) String spec,
            @RequestParam(value = "level", required = false) String attributeLevel,
            @RequestParam(value = "name", required = false) String attributeName,
            @RequestParam(value = "requirement", required = false) String attributeRequirementLevel,
            @RequestParam(value = "showRedacted", required = false, defaultValue = "false") Boolean showRedacted,
            @RequestParam(value = "parentless", required = false, defaultValue = "false") Boolean parentless,
            @RequestParam(value = "createdBefore", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createdBefore,
            @RequestParam(value = "createdAfter", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createdAfter,
            @RequestParam(value = "modifiedBefore", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date modifiedBefore,
            @RequestParam(value = "modifiedAfter", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date modifiedAfter,
            Pageable pageable) {
        final UserAccount requester = principal.getUserAccount();
        final Page<Annotation> page = annotationService.filter(requester, user, group, target, spec, attributeLevel, attributeName, attributeRequirementLevel, showRedacted, parentless, createdBefore, createdAfter, modifiedBefore, modifiedAfter, pageable);
        final PagedResources<AnnotationResource> pagedResources = pageAssembler.toResource(page, assembler, request);
        pagedResources.add(annotationLinks.search());
        return pagedResources;
    }

    /**
     * Get annotation by id
     *
     * @param principal authenticated user
     * @param id annotation id
     * @return annotation
     */
    @RequestMapping(value = AnnotationLinks.ANNOTATION, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AnnotationResource annotation(@AuthenticationPrincipal UserAccountDetails principal, @PathVariable Long id) throws NotFoundException {
        final UserAccount requester = principal.getUserAccount();
        final Annotation annotation = annotationService.findById(requester, id);
        if (annotation == null) {
            throw new AnnotationNotFoundException(id);
        }
        final AnnotationResource resource = assembler.toResource(annotation);
        return resource;
    }

    /**
     * Get annotation data
     *
     * @param principal authenticated user
     * @param id annotation id
     * @return page of annotation data
     */
    @RequestMapping(value = AnnotationLinks.ANNOTATION_DATA, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PagedResources<AnnotationDataResource> annotationDataCollection(
            @AuthenticationPrincipal UserAccountDetails principal,
            @PathVariable Long id,
            @RequestParam(value = "attribute", required = false) Long attributeId,
            @PageableDefault(size = 20, sort = {"id"}) Pageable pageable) throws NotFoundException {
        final UserAccount requester = principal.getUserAccount();
        final Annotation annotation = annotationService.findById(requester, id);
        if (annotation == null) {
            throw new AnnotationNotFoundException(id);
        }
        final Page<AnnotationData> page = annotationDataService.findByAnnotation(annotation, pageable);
        final PagedResources<AnnotationDataResource> pagedResources = dataPageAssembler.toResource(page, dataAssembler, request);
        return pagedResources;
    }

    /**
     * Get annotation data by id
     *
     * @param principal authenticated user
     * @param id annotation id
     * @param dataId annotation data id
     * @return annotation data
     */
    @RequestMapping(value = AnnotationLinks.ANNOTATION_DATA_ID, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AnnotationDataResource annotationData(@AuthenticationPrincipal UserAccountDetails principal, @PathVariable Long id, @PathVariable Long dataId) throws NotFoundException {
        final UserAccount requester = principal.getUserAccount();
        final Annotation annotation = annotationService.findById(requester, id);
        if (annotation == null) {
            throw new AnnotationNotFoundException(id);
        }
        final AnnotationData data = annotation.getData()
                .stream()
                .filter(d -> d.getId().equals(dataId))
                .findFirst()
                .orElseThrow(() -> new AnnotationDataNotFoundException(dataId));
        final AnnotationDataResource resource = dataAssembler.toResource(data);
        return resource;
    }

    /**
     * Get child annotations by parent
     *
     * @param principal authenticated user
     * @param id parent annotation id
     * @return page of annotations
     */
    @RequestMapping(value = AnnotationLinks.CHILDREN, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PagedResources<AnnotationResource> children(@AuthenticationPrincipal UserAccountDetails principal, @PathVariable Long id, @RequestParam(name = "showRedacted", required = false) boolean showRedacted, Pageable pageable) throws NotFoundException {
        final UserAccount requester = principal.getUserAccount();
        final Annotation annotation = annotationService.findById(requester, id);
        if (annotation == null) {
            throw new AnnotationNotFoundException(id);
        }
        final Page<Annotation> page = annotationService.findByParent(requester, annotation, showRedacted, pageable);
        final PagedResources<AnnotationResource> pagedResources = pageAssembler.toResource(page, assembler, request);
        return pagedResources;
    }

    /**
     * Search for annotations
     *
     * @param principal authenticated user (required)
     * @param user username (nullable)
     * @param group group name (nullable)
     * @param target target id (nullable)
     * @param spec specification name (nnullable)
     * @param attributeLevel attribute level (nullable)
     * @param attributeName attribute name (nullable)
     * @param attributeRequirementLevel attribute requirement level (nullable)
     * @param query search terms (nullable)
     * @param not negated search terms (nullable)
     * @param pageable page request
     * @return page of annotations matching parameters
     */
    @RequestMapping(value = AnnotationLinks.SEARCH, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PagedResources<AnnotationResource> search(
            @AuthenticationPrincipal UserAccountDetails principal,
            @RequestParam(value = "user", required = false) String user,
            @RequestParam(value = "group", required = false) String group,
            @RequestParam(value = "dataset", required = false) Long target,
            @RequestParam(value = "spec", required = false) String spec,
            @RequestParam(value = "level", required = false) String attributeLevel,
            @RequestParam(value = "name", required = false) String attributeName,
            @RequestParam(value = "requirement", required = false) String attributeRequirementLevel,
            @RequestParam(value = "showRedacted", required = false, defaultValue = "false") Boolean showRedacted,
            @RequestParam(value = "parentless", required = false, defaultValue = "false") Boolean parentless,
            @RequestParam(value = "createdBefore", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createdBefore,
            @RequestParam(value = "createdAfter", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createdAfter,
            @RequestParam(value = "modifiedBefore", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date modifiedBefore,
            @RequestParam(value = "modifiedAfter", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date modifiedAfter,
            @RequestParam(value = "terms", required = false) String query,
            @RequestParam(value = "not", required = false) String not,
            Pageable pageable) {
        final UserAccount requester = principal.getUserAccount();
        final Set<String> matches = (query != null) ? new HashSet<>(Arrays.asList(query.trim().split("\\s+")))
                : null;
        final Set<String> nots = (not != null) ? new HashSet<>(Arrays.asList(not.trim().split("\\s+")))
                : null;
        final Page<Annotation> page = annotationService.search(requester, user, group, target, spec, attributeLevel, attributeName, attributeRequirementLevel, showRedacted, parentless, createdBefore, createdAfter, modifiedBefore, modifiedAfter, matches, nots, pageable);
        final PagedResources<AnnotationResource> pagedResources = pageAssembler.toResource(page, assembler, request);
        return pagedResources;
    }

    /* POST requests */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public AnnotationResource newAnnotation(@AuthenticationPrincipal UserAccountDetails principal, @RequestBody @Valid AnnotationForm form) throws NotFoundException {
        final UserAccount requester = principal.getUserAccount();
        // get target (nullable)
        final Long targetId = form.getTarget();
        AnnotationTarget annotationTarget = null;
        if (targetId != null) {
            annotationTarget = annotationTargetService.findById(targetId);
            if (annotationTarget == null) {
                throw new AnnotationTargetNotFoundException(targetId);
            }
        }

        // get parent (nullable)
        final Long parentId = form.getParent();
        Annotation parent = null;

        if (parentId != null) {
            parent = annotationService.findById(requester, parentId);
            if (parent == null) {
                throw new AnnotationNotFoundException(parentId);
            }
        }

        // get access
        final Access access = accessService.findByName(form.getAccess());
        if (access == null) {
            throw new AccessNotFoundException(form.getAccess());
        }

        // get group (nullable)
        final String groupName = form.getGroup();
        Group group = null;
        if (groupName != null) {
            group = groupService.findByName(groupName);
            if (group == null) {
                throw new GroupNotFoundException(groupName);
            }
        }

        // get specification
        final Specification specification = specificationService.findByName(form.getSpecification());
        if (specification == null) {
            throw new SpecificationNotFoundException(form.getSpecification());
        }
        Annotation annotation = new Annotation(requester, annotationTarget, parent, access, group, specification);
        annotation = annotationService.save(annotation);
        annotation = newAnnotationData(annotation, form.getData());
        annotation = annotationService.saveAndFlush(annotation);
        final AnnotationResource resource = assembler.toResource(annotation);
        return resource;
    }

    private Annotation newAnnotationData(Annotation annotation, @Valid List<AnnotationDataForm> data) {
        IntStream.range(0, data.size())
                .forEach(i -> {
                    final Long attributeId = data.get(i).getAttribute();
                    final Attribute attribute = attributeService.findById(attributeId);
                    if (attribute == null) {
                        throw new AttributeNotFoundException(attributeId);
                    }
                    final String value = data.get(i).getValue();
                    final List<AnnotationDataForm> subData = data.get(i).getChildren();
                    AnnotationData annoData = new AnnotationData(annotation, attribute, value);
                    annoData = annotationDataService.save(annoData);
                    if (subData.size() > 0) {
                        newAnnotationDataSubData(annotation, annoData, subData);
                    }
                });
        return annotation;
    }

    private void newAnnotationDataSubData(Annotation annotation, AnnotationData parent, @Valid List<AnnotationDataForm> children) {
        IntStream.range(0, children.size())
                .forEach(i -> {
                    final Long attributeId = children.get(i).getAttribute();
                    final Attribute attribute = attributeService.findById(attributeId);
                    if (attribute == null) {
                        throw new AttributeNotFoundException(attributeId);
                    }
                    final String value = children.get(i).getValue();
                    final List<AnnotationDataForm> subData = children.get(i).getChildren();
                    AnnotationData annotationData = new AnnotationData(annotation, attribute, value);
                    annotationData = annotationDataService.save(annotationData);
                    if (subData.size() > 0) {
                        newAnnotationDataSubData(annotation, annotationData, subData);
                    }
                });
    }

    /**
     * Redact an annotation
     *
     * @param principal authenticated user
     * @param id annotation id
     */
    @RequestMapping(value = AnnotationLinks.ANNOTATION_REDACT, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void redactAnnotation(@AuthenticationPrincipal UserAccountDetails principal, @PathVariable Long id) throws NotFoundException, ForbiddenException {
        final UserAccount requester = principal.getUserAccount();
        final Annotation annotation = annotationService.findById(requester, id);
        if (annotation == null) {
            throw new AnnotationNotFoundException(id);
        }
        if (annotation.getUser().getId().equals(requester.getId())) {
            annotation.redact();
            annotationService.save(annotation);
        } else {
            throw new ForbiddenException(requester, request);
        }
    }

    /* PUT requests */
//    public AnnotationResource newAnnotationPUT(@AuthenticationPrincipal UserAccount principal, @RequestBody @Valid AnnotationForm form) {
//        return newAnnotation(principal, form);
//    }

    /* PATCH requests */
    @RequestMapping(value = AnnotationLinks.ANNOTATION, method = RequestMethod.PATCH)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AnnotationResource editAnnotation(@AuthenticationPrincipal UserAccountDetails principal, @PathVariable Long id, @RequestBody AnnotationForm form) throws NotFoundException, ForbiddenException, AccessUpdateException {
        final UserAccount requester = principal.getUserAccount();
        Annotation annotation = annotationService.findById(requester, id);
        if (annotation == null) {
            throw new AnnotationNotFoundException(id);
        }
        if (annotation.getUser().getId().equals(requester.getId())) {
            final String accessName = form.getAccess();
            Access access = null;
            if (accessName != null) {
                access = accessService.findByName(accessName);
                if (access == null) {
                    throw new AccessNotFoundException(accessName);
                }
            }
            final String groupName = form.getGroup();
            Group group = null;
            if (groupName != null) {
                group = groupService.findByName(groupName);
                if (group == null) {
                    throw new GroupNotFoundException(groupName);
                }
            }
            annotation = updateAnnotation(annotation, access, group);
            annotation = annotationService.save(annotation);
            final AnnotationResource resource = assembler.toResource(annotation);
            return resource;
        } else {
            throw new ForbiddenException(requester, request);
        }
    }

    private Annotation updateAnnotation(Annotation annotation, Access access, Group group) {
        // no changes, don't update
        if (annotation.getAccess().getId().equals(access.getId()) && annotation.getGroup().getId().equals(group.getId())) {
            return annotation;
            // update just group
        } else if (annotation.getAccess().getId().equals(access.getId()) && access.getName().equalsIgnoreCase("GROUP") && !annotation.getGroup().getId().equals(group.getId())) {
            annotation.setGroup(group);
            return annotationService.save(annotation);
            // update access from private to group and update group
        } else if (annotation.getAccess().getName().equalsIgnoreCase("PRIVATE") && access.getName().equalsIgnoreCase("GROUP")) {
            if (group == null) {
                throw new AccessUpdateException(true);
            } else {
                annotation.setAccess(access);
                annotation.setGroup(group);
                return annotationService.save(annotation);
            }
            // update access from private to public
        } else if (annotation.getAccess().getName().equalsIgnoreCase("PRIVATE") && access.getName().equalsIgnoreCase("PUBLIC")) {
            annotation.setAccess(access);
            return annotationService.save(annotation);
            // update access from group to public and remove group
        } else if (annotation.getAccess().getName().equalsIgnoreCase("GROUP") && access.getName().equalsIgnoreCase("PUBLIC")) {
            annotation.setAccess(access);
            annotation.setGroup(null);
            return annotationService.save(annotation);
        } else {
            throw new AccessUpdateException(annotation.getAccess(), access);
        }
    }

    @RequestMapping(value = AnnotationLinks.ANNOTATION_DATA, method = RequestMethod.PATCH)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AnnotationDataResource editAnnotationData(@AuthenticationPrincipal UserAccountDetails principal, @PathVariable Long id, @PathVariable Long dataId, @RequestBody AnnotationDataForm form) throws NotFoundException, ForbiddenException {
        final UserAccount requester = principal.getUserAccount();
        final Annotation annotation = annotationService.findById(requester, id);
        if (annotation == null) {
            throw new AnnotationNotFoundException(id);
        }
        if (annotation.getUser().getId().equals(requester.getId())) {
            AnnotationData data = annotation.getData()
                    .stream()
                    .filter(d -> d.getId().equals(dataId))
                    .findFirst()
                    .orElseThrow(() -> new AnnotationDataNotFoundException(dataId));
            final Long attributeId = form.getAttribute();
            if (!isEmpty(attributeId) && !data.getAttribute().getId().equals(attributeId)) {
                final Attribute attribute = attributeService.findById(attributeId);
                if (attribute == null) {
                    throw new AttributeNotFoundException(attributeId);
                }
                data.setAttribute(attribute);
            }
            final String value = form.getValue();
            if (!isEmpty(value) && !data.getValue().equals(value)) {
                data.setValue(value);
            }
            data = annotationDataService.save(data);
            final AnnotationDataResource resource = dataAssembler.toResource(data);
            return resource;
        } else {
            throw new ForbiddenException(requester, request);
        }
    }
}
