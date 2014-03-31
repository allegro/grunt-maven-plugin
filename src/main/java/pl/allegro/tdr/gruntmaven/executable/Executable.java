/*
 * Copyright 2014 Adam Dubiel.
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
package pl.allegro.tdr.gruntmaven.executable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;

/**
 *
 * @author Adam Dubiel
 */
public class Executable {

    /**
     * Pattern for detecting options with whitespace characters. Anything after
     * first whitespace is ignored by exec-maven-plugin, so they need to be transformed, ex:
     * --option true == --option=true
     */
    private static final Pattern WHITESPACED_OPTION_PATTERN = Pattern.compile("^-{1,2}?[\\w-]*\\s+");

    private static final String ARGUMENT_NAME = "argument";

    private final String executableName;

    private List<MojoExecutor.Element> arguments = new ArrayList<MojoExecutor.Element>();

    private final String[] successCodes;

    public Executable(String executableName, String[] successCodes) {
        this.executableName = executableName;
        this.successCodes = successCodes;
    }

    public Executable(String executableName) {
        this(executableName, null);
    }

    Executable(Executable executable) {
        this.executableName = executable.executableName();
        this.arguments = new ArrayList<MojoExecutor.Element>(executable.arguments());
        this.successCodes = executable.successCodes();
    }

    public void addArgument(String value) {
        arguments.add(element(name(ARGUMENT_NAME), value));
    }

    /**
     * Normalization checks if argument contains whitespace character between
     * argument name and it's value, if so it replaces the whitespace with provided
     * replacement. Normalization is needed, because mojo-exec discards all
     * characters after first whitespace.
     */
    public void addNormalizedArgument(String value, String whitespaceReplacement) {
        arguments.add(element(name(ARGUMENT_NAME), normalizeArgument(value, whitespaceReplacement)));
    }

    private String normalizeArgument(String argument, String whitespaceReplacement) {
        Matcher matcher = WHITESPACED_OPTION_PATTERN.matcher(argument);
        if (matcher.find()) {
            return argument.replaceFirst("\\s+", "=");
        }
        return argument;
    }

    public String executableName() {
        return executableName;
    }

    public List<MojoExecutor.Element> arguments() {
        return Collections.unmodifiableList(arguments);
    }

    public MojoExecutor.Element[] argumentsArray() {
        return arguments.toArray(new MojoExecutor.Element[arguments.size()]);
    }

    public boolean overrideSuccessCodes() {
        return successCodes != null;
    }

    public String[] successCodes() {
        return successCodes;
    }
}
