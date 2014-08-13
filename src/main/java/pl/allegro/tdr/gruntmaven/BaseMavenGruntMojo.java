/*
 * Copyright 2013 original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.allegro.tdr.gruntmaven;

import java.io.File;
import java.io.IOException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginManager;
import org.apache.maven.plugin.lifecycle.Execution;
import org.apache.maven.plugin.lifecycle.Phase;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.twdata.maven.mojoexecutor.MojoExecutor;

/**
 * Common properties for all maven-grunt goals.
 *
 * @author Adam Dubiel
 */
public abstract class BaseMavenGruntMojo extends AbstractMojo {

    /**
     * Path to build directory (target for grunt sources), defaults to ${basedir}/target-grunt.
     */
    @Parameter(property = "gruntBuildDirectory", defaultValue = "${basedir}/target-grunt")
    protected String gruntBuildDirectory;

    /**
     * Path to dir, where jsSourceDir is located, defaults to src/main/webapp.
     */
    @Parameter(property = "sourceDirectory", defaultValue = "src/main/webapp")
    protected String sourceDirectory;

    /**
     * Path to dir from where to copy all files that add to grunt environment -
     * has to include package.json and Gruntfile.js, defaults to "static".
     */
    @Parameter(property = "jsSourceDirectory", defaultValue = "static")
    protected String jsSourceDirectory;

    /**
     * Name of packed node_modules TAR file, defaults to node_modules.tar.
     */
    @Parameter(property = "npmOfflineModulesFile", defaultValue = "node_modules.tar")
    protected String npmOfflineModulesFile;

    @Parameter(property = "disabled", defaultValue = "false")
    private boolean disabled;
    
    /**
     * Path to packed node_modules TAR file directory relative to basedir,
     * defaults to statics directory (ex webapp/static/).
     */
    @Parameter(property = "npmOfflineModulesFilePath", defaultValue = "")
    protected String npmOfflineModulesFilePath;

    @Parameter(property = "project", readonly = true, required = true)
    private MavenProject mavenProject;

    @Parameter(property = "session", readonly = true, required = true)
    private MavenSession mavenSession;

    
    /**
     * Maven 2.x compatibility.
     */
    @Component
    @SuppressWarnings("deprecation")
    private PluginManager pluginManager;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if(!disabled) {
            executeInternal();
        }
        else {
            getLog().info("Execution disabled using configuration option.");
        }
    }
    
    protected abstract void executeInternal()  throws MojoExecutionException, MojoFailureException;
    
    protected String basedir() {
        try {
            return mavenProject.getBasedir().getCanonicalPath();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not extract basedir of project.", exception);
        }
    }

    protected String target() {
        return mavenProject.getBuild().getDirectory();
    }

    protected String fullJsSourceDirectory() {
        return basedir() + File.separator + sourceDirectory + File.separator + jsSourceDirectory;
    }

    protected String relativeJsSourceDirectory() {
        return sourceDirectory + File.separator + jsSourceDirectory;
    }

    protected MojoExecutor.ExecutionEnvironment pluginExecutionEvnironment() {
        MojoExecutor.ExecutionEnvironment environment;
        try {
            Object o = mavenSession.lookup("org.apache.maven.plugin.BuildPluginManager");
            environment = MojoExecutor.executionEnvironment(mavenProject, mavenSession, (BuildPluginManager) o);
        } catch (ComponentLookupException e) {
            environment = MojoExecutor.executionEnvironment(mavenProject, mavenSession, pluginManager);
        }
        return environment;
    }
}
