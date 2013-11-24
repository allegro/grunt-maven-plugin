/*
 * Copyright 2013 Adam Dubiel, Przemek Hertel.
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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 *
 * @author Adam Dubiel
 */
@Mojo(name = "link-node-modules", defaultPhase = LifecyclePhase.TEST)
public class LinkNodeModulesMojo extends BaseMavenGruntMojo {

    /**
     * Name of node modules directory to look for.
     */
    private static final String NODE_MODULES_DIR = "node_modules";

    /**
     * maven-junction-plugin groupId.
     */
    private static final String JUNCTION_MAVEN_GROUP = "com.pyx4j";

    /**
     * maven-junction-plugin artefactId.
     */
    private static final String JUNCTION_MAVEN_ARTIFACT = "maven-junction-plugin";

    /**
     * Clean plugin goal.
     */
    private static final String LINK_GOAL = "link";

    /**
     * Change maven-junction-plugin version if needed, defaults to 1.0.3.
     */
    @Parameter(property = "mavenJunctionPluginVersion", defaultValue = "1.0.3")
    protected String mavenJunctionPluginVersion;

    /**
     * Path to directory containing node_modules that should be used by grunt (Maven variables
     * will be interpreted), defaults to "". Goal will be only executed if
     * this variable is nonempty.
     */
    @Parameter(property = "nodeModulesPath", defaultValue = "")
    protected String nodeModulesPath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (nodeModulesPath != null && !nodeModulesPath.isEmpty()) {
            executeMojo(plugin(
                    groupId(JUNCTION_MAVEN_GROUP),
                    artifactId(JUNCTION_MAVEN_ARTIFACT),
                    version(mavenJunctionPluginVersion)),
                    goal(LINK_GOAL),
                    configuration(
                            element(name("links"),
                                    element(name("link"),
                                            element(name("src"), nodeModulesPath + File.separator + NODE_MODULES_DIR),
                                            element(name("dst"), gruntBuildDirectory + File.separator + NODE_MODULES_DIR)
                                    ))),
                    executionEnvironment(mavenProject, mavenSession, pluginManager));
        }
    }

}
