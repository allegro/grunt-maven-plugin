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

import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Executes grunt.
 *
 * @author Adam Dubiel
 */
@Mojo(name = "grunt", defaultPhase = LifecyclePhase.TEST)
public class ExecGruntMojo extends AbstractExecutableMojo {

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

    @Override
    protected String getExecutable() {
        if (runGruntWithNode) {
            return nodeExecutable;
        }
        return gruntExecutable;
    }

    @Override
    protected Element[] getArguments() {
        List<Element> arguments = new ArrayList<Element>();

        if (runGruntWithNode) {
            arguments.add(element(name("argument"), gruntExecutable));
        }
        if(!showColors) {
            arguments.add(element(name("argument"), "--color=false"));
        }
        if(target != null && !target.isEmpty()) {
            arguments.add(element(name("argument"), target));
        }

        return arguments.toArray(new Element[arguments.size()]);
    }
}
