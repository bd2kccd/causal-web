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
package edu.pitt.dbmi.ccd.web.model.file;

import javax.validation.constraints.NotNull;

/**
 *
 * Jul 10, 2017 2:44:50 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FileCategorizeForm {

    @NotNull(message = "File format is required.")
    private Long fileFormatId;

    @NotNull(message = "File delimiter is required.")
    private Long fileDelimiterId;

    @NotNull(message = "Variable type is required.")
    private Long variableTypeId;

    private boolean hasHeader;
    private Character quoteChar;
    private String missingValueMarker;
    private String commentMarker;

    public FileCategorizeForm() {
    }

    public FileCategorizeForm(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public Long getFileFormatId() {
        return fileFormatId;
    }

    public void setFileFormatId(Long fileFormatId) {
        this.fileFormatId = fileFormatId;
    }

    public Long getFileDelimiterId() {
        return fileDelimiterId;
    }

    public void setFileDelimiterId(Long fileDelimiterId) {
        this.fileDelimiterId = fileDelimiterId;
    }

    public Long getVariableTypeId() {
        return variableTypeId;
    }

    public void setVariableTypeId(Long variableTypeId) {
        this.variableTypeId = variableTypeId;
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public Character getQuoteChar() {
        return quoteChar;
    }

    public void setQuoteChar(Character quoteChar) {
        this.quoteChar = quoteChar;
    }

    public String getMissingValueMarker() {
        return missingValueMarker;
    }

    public void setMissingValueMarker(String missingValueMarker) {
        this.missingValueMarker = missingValueMarker;
    }

    public String getCommentMarker() {
        return commentMarker;
    }

    public void setCommentMarker(String commentMarker) {
        this.commentMarker = commentMarker;
    }

}
