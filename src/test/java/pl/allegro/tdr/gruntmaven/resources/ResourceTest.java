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
package pl.allegro.tdr.gruntmaven.resources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 *
 * @author Adam Dubiel
 */
public class ResourceTest {

    public static final String TMP_DIR_NAME = "grunt-maven-plugin";

    private String baseTargetPath;

    @BeforeMethod
    public void setUpEnv() throws IOException {
        baseTargetPath = Files.createTempDirectory(TMP_DIR_NAME).toString();
    }

    @AfterMethod
    public void tearDownEnv() throws IOException {
        FileUtils.forceDelete(new File(baseTargetPath));
    }

    @Test
    public void shouldCopyContentsOfClasspathFileToTmpDirectory() {
        // given
        Resource resource = new Resource("/test-resource", mock(Log.class));

        // when
        resource.copy(baseTargetPath + File.separator + "test-resource");

        // then
        assertThat(new File(baseTargetPath + File.separator + "test-resource")).exists();
    }

    @Test
    public void shouldApplyRegisteredFiltersWhenCopying() {
        // given
        Resource resource = new Resource("/test-resource", mock(Log.class)).withFilter("placeholder", "world");

        // when
        resource.copy(baseTargetPath + File.separator + "test-resource");

        // then
        assertThat(new File(baseTargetPath + File.separator + "test-resource")).hasContent("hello world");
    }

    @Test
    public void shouldChangeWindowsPathBackslashToUnixSlashWhenApplyingFilters() {
        // given
        Resource resource = new Resource("/test-resource", mock(Log.class)).withFilter("placeholder", "c:\\windows\\is\\wierd");

        // when
        resource.copy(baseTargetPath + File.separator + "test-resource");

        // then
        assertThat(new File(baseTargetPath + File.separator + "test-resource")).hasContent("hello c:/windows/is/wierd");
    }

    @Test
    public void shouldLogDebugButLeaveFileAloneWhenFileAlreadyExistsAndNotOverwriting() {
        // given
        Log logger = mock(Log.class);
        Resource resource = new Resource("/test-resource", logger);
        resource.copy(baseTargetPath + File.separator + "test-resource");

        // when
        resource.copy(baseTargetPath + File.separator + "test-resource");

        // then
        verify(logger).debug(anyString());
    }

    @Test
    public void shouldOverwriteExitingFileWhenCopyingWithOverwriting() {
        // given
        Resource resource = new Resource("/test-resource", mock(Log.class));
        resource.copy(baseTargetPath + File.separator + "test-resource");

        // when
        resource.copyAndOverwrite(baseTargetPath + File.separator + "test-resource");

        // then
        assertThat(new File(baseTargetPath + File.separator + "test-resource")).exists();
    }
}
