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
import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * MOJO executing maven-resources-plugin to create target/{jsTargetDir} directory
 * containing all statics.
 *
 * @author Adam Dubiel
 */
@Mojo(name = "create-resources", defaultPhase = LifecyclePhase.VALIDATE)
public class CreateResourcesMojo extends BaseMavenGruntMojo {

    /**
     * Resources plugin groupId.
     */
    private static final String RESOURCES_MAVEN_GROUP = "org.apache.maven.plugins";

    /**
     * Reosurces plugin artefactId.
     */
    private static final String RESOURCES_MAVEN_ARTIFACT = "maven-resources-plugin";

    /**
     * Resources plugin version.
     */
    private static final String RESOURCES_GOAL = "copy-resources";

    /**
     * Change resources plugin version if needed, defaults to 2.6.
     */
    @Parameter(property = "mavenResourcesPluginVersion", defaultValue = "2.6")
    private String mavenResourcesPluginVersion;

    /**
     * Should copy-resources plugin overwrite resources even if target has newer
     * version (see maven-resources-plugin documentation for more details),
     * defaults to true.
     */
    @Parameter(property = "overwriteResources", defaultValue="true")
    private boolean overwriteResources;


    public void execute() throws MojoExecutionException, MojoFailureException {
        executeMojo(plugin(
                groupId(RESOURCES_MAVEN_GROUP),
                artifactId(RESOURCES_MAVEN_ARTIFACT),
                version(mavenResourcesPluginVersion)),
                goal(RESOURCES_GOAL),
                configuration(
                element(name("overwrite"), Boolean.toString(overwriteResources)),
                element(name("outputDirectory"), gruntBuildDirectory),
                element(name("resources"), element(name("resource"), element(name("directory"), sourceDirectory + "/" + jsSourceDirectory)))),
                executionEnvironment(mavenProject, mavenSession, pluginManager));
    }
}
