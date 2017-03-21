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
package edu.pitt.dbmi.ccd.web.prop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 *
 * Oct 4, 2016 10:48:59 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Component
@PropertySource("classpath:ccd.properties")
public class CcdProperties {

    @Value("${ccd.jar.algorithm}")
    private String algoJar;

    @Value("${ccd.algorithm.fges.cont}")
    private String algoFgesCont;

    @Value("${ccd.algorithm.fges.disc}")
    private String algoFgesDisc;

    @Value("${ccd.algorithm.gfci.cont}")
    private String algoGfciCont;

    @Value("${ccd.algorithm.gfci.disc}")
    private String algoGfciDisc;

    @Value("${ccd.queue.size:5}")
    private String jobQueueSize;

    @Value("${ccd.server.workspace:}")
    private String workspaceDir;

    @Value("${ccd.folder.data}")
    private String dataFolder;

    @Value("${ccd.folder.lib:lib}")
    private String libFolder;

    @Value("${ccd.folder.tmp:tmp}")
    private String tmpFolder;

    @Value("${ccd.folder.results:results}")
    private String resultFolder;

    @Value("${ccd.folder.results.algorithm:algorithm}")
    private String resultAlgorithmFolder;

    @Value("${ccd.folder.results.comparison:comparison}")
    private String resultComparisonFolder;

    @Value("${ccd.acct.reg.activ.required:false}")
    private boolean requireActivation;

    @Value("${ccd.app.title}")
    private String title;

    @Value("${ccd.app.copyright}")
    private String copyright;

    @Value("${ccd.app.agreement}")
    private String agreement;

    public CcdProperties() {
    }

    public String getAlgoJar() {
        return algoJar;
    }

    public void setAlgoJar(String algoJar) {
        this.algoJar = algoJar;
    }

    public String getAlgoFgesCont() {
        return algoFgesCont;
    }

    public void setAlgoFgesCont(String algoFgesCont) {
        this.algoFgesCont = algoFgesCont;
    }

    public String getAlgoFgesDisc() {
        return algoFgesDisc;
    }

    public void setAlgoFgesDisc(String algoFgesDisc) {
        this.algoFgesDisc = algoFgesDisc;
    }

    public String getAlgoGfciCont() {
        return algoGfciCont;
    }

    public void setAlgoGfciCont(String algoGfciCont) {
        this.algoGfciCont = algoGfciCont;
    }

    public String getAlgoGfciDisc() {
        return algoGfciDisc;
    }

    public void setAlgoGfciDisc(String algoGfciDisc) {
        this.algoGfciDisc = algoGfciDisc;
    }

    public String getJobQueueSize() {
        return jobQueueSize;
    }

    public void setJobQueueSize(String jobQueueSize) {
        this.jobQueueSize = jobQueueSize;
    }

    public String getWorkspaceDir() {
        return workspaceDir;
    }

    public void setWorkspaceDir(String workspaceDir) {
        this.workspaceDir = workspaceDir;
    }

    public String getDataFolder() {
        return dataFolder;
    }

    public void setDataFolder(String dataFolder) {
        this.dataFolder = dataFolder;
    }

    public String getLibFolder() {
        return libFolder;
    }

    public void setLibFolder(String libFolder) {
        this.libFolder = libFolder;
    }

    public String getTmpFolder() {
        return tmpFolder;
    }

    public void setTmpFolder(String tmpFolder) {
        this.tmpFolder = tmpFolder;
    }

    public String getResultFolder() {
        return resultFolder;
    }

    public void setResultFolder(String resultFolder) {
        this.resultFolder = resultFolder;
    }

    public String getResultAlgorithmFolder() {
        return resultAlgorithmFolder;
    }

    public void setResultAlgorithmFolder(String resultAlgorithmFolder) {
        this.resultAlgorithmFolder = resultAlgorithmFolder;
    }

    public String getResultComparisonFolder() {
        return resultComparisonFolder;
    }

    public void setResultComparisonFolder(String resultComparisonFolder) {
        this.resultComparisonFolder = resultComparisonFolder;
    }

    public boolean isRequireActivation() {
        return requireActivation;
    }

    public void setRequireActivation(boolean requireActivation) {
        this.requireActivation = requireActivation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getAgreement() {
        return agreement;
    }

    public void setAgreement(String agreement) {
        this.agreement = agreement;
    }

}
