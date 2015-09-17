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
package edu.pitt.dbmi.ccd.web.service.result.algorithm;

import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.ResultFileInfo;
import edu.pitt.dbmi.ccd.web.model.d3.Node;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * Sep 15, 2015 12:17:45 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public interface AlgorithmResultService {

    public List<ResultFileInfo> listResultFileInfo(AppUser appUser);

    public void deleteResultFile(List<String> fileNames, AppUser appUser);

    public void downloadResultFile(String fileName, boolean remote, AppUser appUser, HttpServletRequest request, HttpServletResponse response);

    public Map<String, String> getPlotParameters(String fileName, boolean remote, AppUser appUser);

    public List<Node> getGraphNodes(String fileName, boolean remote, AppUser appUser);

    public List<String> getErrorMessages(String fileName, boolean remote, AppUser appUser);

}
