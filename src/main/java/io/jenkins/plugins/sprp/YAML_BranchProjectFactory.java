/*
 * The MIT License
 * Orignal class: org.jenkinsci.plugins.workflow.multibranch.AbstractWorkflowBranchProjectFactory
 * Copyright 2018 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.jenkins.plugins.sprp;

import hudson.Extension;
import hudson.model.TaskListener;
import jenkins.scm.api.SCMProbeStat;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;

/**
 * Recognizes and builds {@code Jenkinsfile}.
 */
public class YAML_BranchProjectFactory extends org.jenkinsci.plugins.workflow.multibranch.AbstractWorkflowBranchProjectFactory {
    static final String SCRIPT = "Jenkins.yaml";
    private String scriptPath = SCRIPT;

    public Object readResolve() {
        if (this.scriptPath == null) {
            this.scriptPath = SCRIPT;
        }
        return this;
    }

    @DataBoundConstructor
    public YAML_BranchProjectFactory() { }

    @DataBoundSetter
    public void setScriptPath(String scriptPath) {
        if (StringUtils.isEmpty(scriptPath)) {
            this.scriptPath = SCRIPT;
        } else {
            this.scriptPath = scriptPath;
        }
    }

    public String getScriptPath(){
        return scriptPath;
    }

    @Override protected FlowDefinition createDefinition() {
        return new YAML_FlowDefinition(scriptPath);
    }

    @Override protected SCMSourceCriteria getSCMSourceCriteria(SCMSource source) {
        return new SCMSourceCriteria() {
            @Override public boolean isHead(SCMSourceCriteria.Probe probe, TaskListener listener) throws IOException {
                SCMProbeStat stat = probe.stat(scriptPath);
                switch (stat.getType()) {
                    case NONEXISTENT:
                        if (stat.getAlternativePath() != null) {
                            listener.getLogger().format("      ‘%s’ not found (but found ‘%s’, search is case sensitive)%n", scriptPath, stat.getAlternativePath());
                        } else {
                            listener.getLogger().format("      ‘%s’ not found%n", scriptPath);
                        }
                        return false;
                    case DIRECTORY:
                        listener.getLogger().format("      ‘%s’ found but is a directory not a file%n", scriptPath);
                        return false;
                    default:
                        listener.getLogger().format("      ‘%s’ found%n", scriptPath);
                        return true;

                }
            }

            @Override
            public int hashCode() {
                return getClass().hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return getClass().isInstance(obj);
            }
        };
    }

    @Extension
    public static class DescriptorImpl extends AbstractWorkflowBranchProjectFactoryDescriptor {

        @Override public String getDisplayName() {
            return "by " + SCRIPT;
        }

    }

}