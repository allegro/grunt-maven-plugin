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

import java.util.Arrays;
import java.util.List;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import pl.allegro.tdr.gruntmaven.executable.Executable;

/**
 * Executes grunt.
 *
 * @author Adam Dubiel
 */
@Mojo(name = "grunt", defaultPhase = LifecyclePhase.COMPILE)
public class ExecGruntMojo extends AbstractExecutableMojo {

    /**
     * Custom success codes to be registered in maven-exec plugin when we want to ignore
     * Grunt errors.
     */
    private static final String[] IGNORE_GRUNT_TASKS_ERRORS_CUSTOM_CODES = {"0", "3", "6"};

    private static final String[] IGNORE_ALL_GRUNT_ERRORS_CUSTOM_CODES = {"0", "1", "2", "3", "4", "5", "6"};

    /**
     * Name of grunt target, will be passed directly to grunt.
     */
    @Parameter(property = "target", defaultValue = "")
    private String target;

    /**
     * Name of node executable in PATH, defaults to node.
     */
    @Parameter(property = "nodeExecutable", defaultValue = "node")
    private String nodeExecutable;

    /**
     * Path to local grunt executable, defaults to grunt (global PATH).
     */
    @Parameter(property = "gruntExecutable", defaultValue = "grunt")
    private String gruntExecutable;

    /**
     * Should grunt be run as node module (ex: node node_modules/grunt-cli/bin/grunt).
     * By default grunt is run as independent command line executable.
     */
    @Parameter(property = "runGruntWithNode", defaultValue = "false")
    private boolean runGruntWithNode;

    /**
     * List of options passed to grunt.
     */
    @Parameter(property = "gruntOptions")
    private String[] gruntOptions;

    /**
     * Should Maven ignore grunt task errors (for example failing tests) and always finish grunt execution with success.
     */
    @Parameter(property = "ignoreTasksErrors", defaultValue = "false")
    private boolean ignoreTasksErrors;

    /**
     * Should Maven ignore all grunt errors including fatal ones and always finish grunt execution with success.
     */
    @Parameter(property = "ignoreAllErrors", defaultValue = "false")
    private boolean ignoreAllErrors;

    @Override
    protected List<Executable> getExecutables() {
        Executable executable;
        if (runGruntWithNode) {
            executable = new Executable(nodeExecutable, customSuccessCodes());
        } else {
            executable = new Executable(gruntExecutable, customSuccessCodes());
        }

        appendArguments(executable);

        return Arrays.asList(executable);
    }

    private void appendArguments(Executable executable) {
        if (runGruntWithNode) {
            executable.addArgument(gruntExecutable);
        }
        if (target != null && !target.isEmpty()) {
            executable.addArgument(target);
        }
        if (!showColors) {
            executable.addArgument("--no-color");
        }
        if (gruntOptions != null) {
            for (String option : gruntOptions) {
                executable.addNormalizedArgument(option, "=");
            }
        }
    }

    private String[] customSuccessCodes() {
        if (ignoreAllErrors) {
            return IGNORE_ALL_GRUNT_ERRORS_CUSTOM_CODES;
        } else if (ignoreTasksErrors) {
            return IGNORE_GRUNT_TASKS_ERRORS_CUSTOM_CODES;
        }
        return null;
    }

}
