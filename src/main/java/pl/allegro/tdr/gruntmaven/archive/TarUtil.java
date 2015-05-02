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

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;

import java.io.*;

/**
 *
 * @author Adam Dubiel
 */
public final class TarUtil {

    private TarUtil() {
    }

    public static void untar(File source, File target, Log logger) {
        TarArchiveInputStream tarInput = null;
        ArchiveEntry entry;
        OutputStream output = null;

        try {
            tarInput = new TarArchiveInputStream(new FileInputStream(source));

            entry = tarInput.getNextEntry();
            while (entry != null) {
                File outputFile = new File(target.getCanonicalPath() + File.separator + entry.getName());
                if (entry.isDirectory()) {
                    logger.debug("creating dir at: " + outputFile.getCanonicalPath());
                    outputFile.mkdirs();
                } else {
                    logger.debug("creating file at: " + outputFile.getCanonicalPath());
                    output = new FileOutputStream(outputFile);
                    IOUtils.copy(tarInput, output);
                    output.flush();
                    output.close();
                }

                entry = tarInput.getNextEntry();
            }
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        } finally {
            IOUtils.closeQuietly(tarInput);
            IOUtils.closeQuietly(output);
        }
    }
}
