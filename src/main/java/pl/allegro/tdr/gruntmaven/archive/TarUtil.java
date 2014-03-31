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
package pl.allegro.tdr.gruntmaven.archive;

import java.io.*;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.archiver.tar.TarEntry;
import org.codehaus.plexus.archiver.tar.TarInputStream;
import org.codehaus.plexus.util.IOUtil;

/**
 *
 * @author Adam Dubiel
 */
public final class TarUtil {

    private TarUtil() {
    }

    public static void untar(File source, File target, Log logger) {
        TarInputStream tarInput = null;
        TarEntry entry;
        OutputStream output = null;

        try {
            tarInput = new TarInputStream(new FileInputStream(source));

            entry = tarInput.getNextEntry();
            while (entry != null) {
                File outputFile = new File(target.getCanonicalPath() + File.separator + entry.getName());
                if (entry.isDirectory()) {
                    logger.debug("creating dir at: " + outputFile.getCanonicalPath());
                    outputFile.mkdirs();
                } else {
                    logger.debug("creating file at: " + outputFile.getCanonicalPath());
                    output = new FileOutputStream(outputFile);
                    IOUtil.copy(tarInput, output);
                    output.flush();
                    output.close();
                }

                entry = tarInput.getNextEntry();
            }
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        } finally {
            IOUtil.close(tarInput);
            IOUtil.close(output);
        }
    }
}
