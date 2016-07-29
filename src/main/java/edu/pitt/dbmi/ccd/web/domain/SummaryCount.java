/*
 * Copyright (C) 2016 University of Pittsburgh.
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
package edu.pitt.dbmi.ccd.web.domain;

/**
 *
 * Jul 28, 2016 7:58:16 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class SummaryCount {

    private String name;

    private Long count;

    private String panel;

    private String glyphicon;

    private String uri;

    public SummaryCount() {
    }

    public SummaryCount(String name, Long count, String panel, String glyphicon, String uri) {
        this.name = name;
        this.count = count;
        this.panel = panel;
        this.glyphicon = glyphicon;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getPanel() {
        return panel;
    }

    public void setPanel(String panel) {
        this.panel = panel;
    }

    public String getGlyphicon() {
        return glyphicon;
    }

    public void setGlyphicon(String glyphicon) {
        this.glyphicon = glyphicon;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}
