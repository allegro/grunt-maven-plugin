/*
 * Copyright 2014 original author or authors.
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

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.archiver.tar.TarInputStream;
import org.codehaus.plexus.util.IOUtil;
import pl.allegro.tdr.gruntmaven.archive.TarUtil;
import pl.allegro.tdr.gruntmaven.executable.Executable;

/**
 * Run NPM rebuild.
 *
 * @author Adam Dubiel
 */
@Mojo(name = "npm-offline", defaultPhase = LifecyclePhase.TEST)
public class ExecNpmOfflineMojo extends ExecNpmMojo {

    private static final String NODE_MODULES_DIR_NAME = "node_modules";

    /**
     * Name of packed node_modules TAR file, defaults to node_modules.tar.
     */
    @Parameter(property = "npmOfflineModulesFile", defaultValue = "node_modules.tar")
    private String npmOfflineModulesFile;

    /**
     * Path to packed node_modules TAR file directory relative to basedir,
     * defaults to statics directory (ex webapp/static/).
     */
    @Parameter(property = "npmOfflineModulesFilePath", defaultValue = "")
    private String npmOfflineModulesFilePath;

    @Override
    protected List<Executable> getExecutables() {
        unpackModules();
        return new ArrayList<Executable>();
    }

    private void unpackModules() {
        String nodeModulesPath = gruntBuildDirectory + File.separator + NODE_MODULES_DIR_NAME;
        File targetModulesPath = new File(nodeModulesPath);
        getLog().info("checking for existence of " + nodeModulesPath);
        if (targetModulesPath.exists()) {
            getLog().info("Found existing node_modules at " + nodeModulesPath + " , not going to overwrite them.");
            return;
        }

        if(npmOfflineModulesFilePath == null) {
            npmOfflineModulesFilePath = relativeJsSourceDirectory();
        }

        File offlineModules = new File(basedir() + File.separator + npmOfflineModulesFilePath + File.separator + npmOfflineModulesFile);
        File targetPath = new File(gruntBuildDirectory);

        TarUtil.untar(offlineModules, targetPath, getLog());
    }

}
