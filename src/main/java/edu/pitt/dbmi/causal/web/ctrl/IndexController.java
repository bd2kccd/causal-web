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

import edu.pitt.dbmi.ccd.annotations.resource.IndexResource;
import edu.pitt.dbmi.ccd.annotations.resource.IndexResourceAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

// logging
/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
@RestController
@RequestMapping(value = "/")
public class IndexController {

    // logger
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

    // components
    private final IndexResourceAssembler assembler;

    @Autowired(required = true)
    public IndexController(IndexResourceAssembler assembler) {
        this.assembler = assembler;
    }

    /**
     * Application index endpoint
     *
     * @return links to endpoints
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<IndexResource> index() {
        return ResponseEntity.ok(assembler.buildIndex());
//        return new ResponseEntity<>(assembler.buildIndex(), HttpStatus.OK);
    }
}
