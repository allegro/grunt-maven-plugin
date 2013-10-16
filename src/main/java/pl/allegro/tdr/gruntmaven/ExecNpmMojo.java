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
 * Executes npm install to download all dependencies declared in
 * package.json.
 *
 * @author Adam Dubiel
 */
@Mojo(name = "npm", defaultPhase = LifecyclePhase.TEST)
public class ExecNpmMojo extends AbstractExecutableMojo {

    private static final String NPM_INSTALL_COMMAND = "install";

    /**
     * Name of npm executable in PATH, defaults to npm.
     */
    @Parameter(property = "npmExecutable", defaultValue = "npm")
    private String npmExecutable;

    @Override
    protected String getExecutable() {
        return npmExecutable;
    }

    @Override
    protected Element[] getArguments() {
        List<Element> arguments = new ArrayList<Element>();

        arguments.add(element(name("argument"), NPM_INSTALL_COMMAND));

        if (!showColors) {
            arguments.add(element(name("argument"), "--color=false"));
        }

        return arguments.toArray(new Element[arguments.size()]);
    }
}
