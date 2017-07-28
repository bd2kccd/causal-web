/*
 * Copyright (C) 2017 University of Pittsburgh.
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
package edu.pitt.dbmi.ccd.web.domain.algorithm;

import edu.pitt.dbmi.ccd.db.entity.File;
import java.util.List;

/**
 *
 * Jul 27, 2017 12:06:55 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class TetradJobPage {

    private String title;
    private List<File> datasetOpts;
    private List<File> knowledgeOpts;

    public TetradJobPage() {
    }

    public TetradJobPage(String title, List<File> datasetOpts, List<File> knowledgeOpts) {
        this.title = title;
        this.datasetOpts = datasetOpts;
        this.knowledgeOpts = knowledgeOpts;
    }

    public List<File> getKnowledgeOpts() {
        return knowledgeOpts;
    }

    public void setKnowledgeOpts(List<File> knowledgeOpts) {
        this.knowledgeOpts = knowledgeOpts;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<File> getDatasetOpts() {
        return datasetOpts;
    }

    public void setDatasetOpts(List<File> datasetOpts) {
        this.datasetOpts = datasetOpts;
    }

}
