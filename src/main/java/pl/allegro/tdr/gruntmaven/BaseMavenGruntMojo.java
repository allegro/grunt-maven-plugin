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

import java.io.IOException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

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
    @Parameter(property = "sourceDirectory", defaultValue = "src/main/webapp/")
    protected String sourceDirectory;

    /**
     * Path to dir from where to copy all files that add to grunt environment -
     * has to include package.json and Gruntfile.js, defaults to "static".
     */
    @Parameter(property = "jsSourceDirectory", defaultValue = "static")
    protected String jsSourceDirectory;

    @Parameter(property = "project", readonly = true, required = true)
    protected MavenProject mavenProject;

    @Parameter(property = "session", readonly = true, required = true)
    protected MavenSession mavenSession;

    @Component
    protected BuildPluginManager pluginManager;

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
}
