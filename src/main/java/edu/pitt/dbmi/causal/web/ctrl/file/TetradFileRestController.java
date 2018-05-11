/*
 * Copyright (C) 2018 University of Pittsburgh.
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
package edu.pitt.dbmi.causal.web.ctrl.file;

import edu.pitt.dbmi.causal.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.ccd.db.domain.ListItem;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Mar 8, 2018 5:02:48 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@RestController
@SessionAttributes("appUser")
@RequestMapping(value = "secured/file/tetrad")
public class TetradFileRestController {

    private final AppUserService appUserService;
    private final TetradDataFileService tetradDataFileService;
    private final VariableTypeService variableTypeService;

    @Autowired
    public TetradFileRestController(AppUserService appUserService, TetradDataFileService tetradDataFileService, VariableTypeService variableTypeService) {
        this.appUserService = appUserService;
        this.tetradDataFileService = tetradDataFileService;
        this.variableTypeService = variableTypeService;
    }

    @GetMapping(value = "data/{varTypeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listDataFilesByVarId(@PathVariable final Long varTypeId, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        VariableType varType = variableTypeService.findById(varTypeId);
        if (varType == null) {
            throw new ResourceNotFoundException();
        }

        List<ListItem> list = tetradDataFileService.getRepository()
                .getTetradDataListItem(userAccount, varType);

        return ResponseEntity.ok(list);
    }

}
