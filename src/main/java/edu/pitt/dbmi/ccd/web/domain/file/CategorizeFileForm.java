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
package edu.pitt.dbmi.ccd.web.domain.file;

import javax.validation.constraints.NotNull;

/**
 *
 * May 14, 2017 12:05:20 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class CategorizeFileForm {

    @NotNull
    private Long fileTypeId;

    @NotNull
    private Long dataFileFormatId;

    @NotNull
    private Long variableFileFormatId;

    @NotNull
    private Long knowledgeFileFormatId;

    @NotNull
    private Long resultFileFormatId;

    @NotNull
    private Long fileDelimiterTypeId;

    @NotNull
    private Long fileVariableTypeId;

    private Character quoteChar;
    private String missingValueMarker;
    private String commentMarker;

    public CategorizeFileForm() {
        this.fileTypeId = 1L;
        this.dataFileFormatId = 1L;
        this.variableFileFormatId = 1L;
        this.knowledgeFileFormatId = 1L;
        this.resultFileFormatId = 1L;
        this.fileDelimiterTypeId = 1L;
        this.fileVariableTypeId = 1L;
        this.quoteChar = '"';
        this.missingValueMarker = "*";
        this.commentMarker = "//";
    }

    public Long getFileTypeId() {
        return fileTypeId;
    }

    public void setFileTypeId(Long fileTypeId) {
        this.fileTypeId = fileTypeId;
    }

    public Long getDataFileFormatId() {
        return dataFileFormatId;
    }

    public void setDataFileFormatId(Long dataFileFormatId) {
        this.dataFileFormatId = dataFileFormatId;
    }

    public Long getVariableFileFormatId() {
        return variableFileFormatId;
    }

    public void setVariableFileFormatId(Long variableFileFormatId) {
        this.variableFileFormatId = variableFileFormatId;
    }

    public Long getKnowledgeFileFormatId() {
        return knowledgeFileFormatId;
    }

    public void setKnowledgeFileFormatId(Long knowledgeFileFormatId) {
        this.knowledgeFileFormatId = knowledgeFileFormatId;
    }

    public Long getResultFileFormatId() {
        return resultFileFormatId;
    }

    public void setResultFileFormatId(Long resultFileFormatId) {
        this.resultFileFormatId = resultFileFormatId;
    }

    public Long getFileDelimiterTypeId() {
        return fileDelimiterTypeId;
    }

    public void setFileDelimiterTypeId(Long fileDelimiterTypeId) {
        this.fileDelimiterTypeId = fileDelimiterTypeId;
    }

    public Long getFileVariableTypeId() {
        return fileVariableTypeId;
    }

    public void setFileVariableTypeId(Long fileVariableTypeId) {
        this.fileVariableTypeId = fileVariableTypeId;
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
