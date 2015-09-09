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
package edu.pitt.dbmi.ccd.web.service.result.compare;

import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.ResultFileInfo;
import edu.pitt.dbmi.ccd.web.model.result.ResultComparison;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * Sep 8, 2015 11:07:02 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public interface ResultComparisonService {

    public List<ResultFileInfo> getUserResultComparisonFiles(AppUser appUser);

    public ResultComparison readInResultComparisonFile(String fileName, boolean remote, AppUser appUser);

    public void downloadResultComparisonFile(String fileName, boolean remote, AppUser appUser, HttpServletRequest request, HttpServletResponse response);

    public void deleteResultComparisonFile(List<String> fileNames, AppUser appUser);

}
