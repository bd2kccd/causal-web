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
package edu.pitt.dbmi.causal.web.model.file;

import javax.validation.constraints.NotNull;

/**
 *
 * Jul 10, 2017 2:44:50 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FileCategorizationForm {

    @NotNull(message = "File format is required.")
    private short formatOpt;

    @NotNull(message = "File delimiter is required.")
    private short delimOpt;

    @NotNull(message = "Variable type is required.")
    private short varOpt;

    private boolean hasHeader;
    private Character quoteChar;
    private String missingValueMarker;
    private String commentMarker;

    public FileCategorizationForm() {
    }

    public FileCategorizationForm(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public short getFormatOpt() {
        return formatOpt;
    }

    public void setFormatOpt(short formatOpt) {
        this.formatOpt = formatOpt;
    }

    public short getDelimOpt() {
        return delimOpt;
    }

    public void setDelimOpt(short delimOpt) {
        this.delimOpt = delimOpt;
    }

    public short getVarOpt() {
        return varOpt;
    }

    public void setVarOpt(short varOpt) {
        this.varOpt = varOpt;
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
