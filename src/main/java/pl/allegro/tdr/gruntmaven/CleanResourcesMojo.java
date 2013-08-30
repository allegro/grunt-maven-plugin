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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

/**
 * Mojo executing clean task on Grunt build directory.
 *
 * @author Adam Dubiel
 */
@Mojo(name = "clean", defaultPhase = LifecyclePhase.CLEAN)
public class CleanResourcesMojo extends BaseMavenGruntMojo {

    /**
     * Clean plugin groupId.
     */
    private static final String CLEAN_MAVEN_GROUP = "org.apache.maven.plugins";

    /**
     * Clean plugin artefactId.
     */
    private static final String CLEAN_MAVEN_ARTIFACT = "maven-clean-plugin";

    /**
     * Clean plugin goal.
     */
    private static final String CLEAN_GOAL = "clean";

    /**
     * Change clean plugin version if needed, defaults to 2.5.
     */
    @Parameter(property = "mavenCleanPluginVersion", defaultValue = "2.5")
    protected String mavenCleanPluginVersion;

    public void execute() throws MojoExecutionException, MojoFailureException {
        executeMojo(plugin(
                groupId(CLEAN_MAVEN_GROUP),
                artifactId(CLEAN_MAVEN_ARTIFACT),
                version(mavenCleanPluginVersion)),
                goal(CLEAN_GOAL),
                configuration(
                element(
                name("filesets"),
                element(name("fileset"),
                element(name("directory"), gruntBuildDirectory))),
                element(name("excludeDefaultDirectories"), "true")),
                executionEnvironment(mavenProject, mavenSession, pluginManager));
    }
}
