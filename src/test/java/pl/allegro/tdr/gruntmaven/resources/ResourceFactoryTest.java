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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.Developer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class ResourceFactoryTest {
	private static final SystemStreamLog mavenLog = new SystemStreamLog();
	private static final String testDir = "target/test";
	private static final String gruntVersion =  "0.4.2";

	private JsonNodeFactory factory = new JsonNodeFactory(false);
	private ObjectMapper mapper = new ObjectMapper();

	private String name = "grunt-maven-plugin";
	private String version = "1.2.3";
	private String description = "Testing, 1-2-3...";
	private String url = "https://github.com/allegro/grunt-maven-plugin";
	private Developer dev = new Developer();
	private Contributor dev2 = new Developer();
	private Contributor dev3 = new Developer();
	private Contributor devNameless = new Developer();
	private Contributor devHomeless = new Developer();
	private Contributor devNoEmail = new Developer();

	public ResourceFactoryTest() {
		dev.setName( "Grupa Allegro" );
		dev.setUrl( "https://github.com/allegro" );
		dev.setEmail( "grupa@domain.com" );

		dev2.setName( "Nicholas Albion" );
		dev2.setUrl( "https://github.com/nalbion" );
		dev2.setEmail( "nalbion@domain.com" );

		dev3.setName( "Developer Three" );
		dev3.setUrl( "https://github.com/dev3" );
		dev3.setEmail( "dev3@domain.com" );

		devNameless.setUrl( "https://github.com/nameless" );
		devNameless.setEmail( "anon@domain.com" );

		devHomeless.setName( "Homeless" );
		devHomeless.setEmail( "homeless@domain.com" );

		devNoEmail.setName( "No Email" );
		devNoEmail.setUrl( "https://github.com/noemail" );
	}

	@AfterClass
	public static void cleanup() {
		new File(testDir, "Gruntfile.js").delete();
		new File(testDir, "package.json").delete();
		new File(testDir, "bower.json").delete();
		new File(testDir).delete();
	}

	@Test
	public void shouldCreateAValidGruntfile() throws IOException {
		// given
		String fileName = "Gruntfile.js";
		File file = new File( testDir, fileName );
		file.delete();

		// when
		ResourceFactory.createGruntfile( testDir, new SystemStreamLog() );

		// then
		assertThat( file ).exists();
		assertFileContainsLine( file, "module.exports = function(grunt) {" );
	}

	@Test
	public void shouldCreateAValidPackageJsonFile() throws IOException, MojoExecutionException {
		// given
		MavenProject mavenProject = getProjectConfigBasic();
		File packageJsonFile = new File( testDir, "package.json" );
		packageJsonFile.delete();

		// when
		ResourceFactory.createOrUpdatePackageJson( testDir, gruntVersion, mavenProject, mavenLog );

		// then
		assertThat( packageJsonFile ).exists();
		ObjectNode rootNode = (ObjectNode)mapper.readValue( packageJsonFile, JsonNode.class );
		assertTextNodeEquals( rootNode, "name", "empty-project" );
		assertTextNodeEquals( rootNode, "version", "0" );
		assertTextNodeEquals( rootNode, "description", "" );
		assertTextNodeEquals( rootNode, "homepage", "" );
		assertTextNodeEquals( (ObjectNode)rootNode.get("devDependencies"), "grunt", "~" + gruntVersion );
		assertTextNodeEquals( (ObjectNode)rootNode.get("devDependencies"), "matchdep", "~0.3.0" );
		assertTextNodeEquals( (ObjectNode)rootNode.get("devDependencies"), "grunt-maven", "~1.1.0" );
	}

	@Test
	public void shouldUpdateExistingPackageJsonFileFromMavenProjectModel() throws IOException, MojoExecutionException {
		// given package.json already exists but is out of sync with project config 2
		MavenProject mavenProject = getProjectConfigBasic();
		ResourceFactory.createOrUpdatePackageJson( testDir, gruntVersion, mavenProject, mavenLog );

		// when Maven model is updated and createOrUpdate is run again
		mavenProject = getProjectConfigWith2Developers();
		ResourceFactory.createOrUpdatePackageJson( testDir, gruntVersion, mavenProject, mavenLog );

		// then
		File packageJsonFile = new File( testDir, "package.json" );
		ObjectNode rootNode = (ObjectNode)mapper.readValue( packageJsonFile, JsonNode.class );
		// Does the package name really need to follow the maven artifactId?  ...probably not
		// assertTextNodeEquals( rootNode, "name", name );
		assertTextNodeEquals( rootNode, "version", version );
		assertTextNodeEquals( rootNode, "description", description );
		assertTextNodeEquals( rootNode, "homepage", url );
	}

	@Test
	public void shouldFormatContributorDetailsInPackageJsonFile() throws IOException, MojoExecutionException {
		// given
		MavenProject mavenProject = getProjectConfigWithMissingContributorDetails();
		File packageJsonFile = new File( testDir, "package.json" );

		// when
		ResourceFactory.createOrUpdatePackageJson( testDir, gruntVersion, mavenProject, mavenLog );

		// then
		ObjectNode rootNode = (ObjectNode)mapper.readValue( packageJsonFile, JsonNode.class );
		ArrayNode contributors = (ArrayNode)rootNode.get("contributors");
		assertNodeContributorEquals( (ObjectNode)rootNode.get("author"), devNameless );
		assertNodeContributorEquals( (ObjectNode)contributors.get( 0 ), devHomeless );
		assertNodeContributorEquals( (ObjectNode)contributors.get( 1 ), devNoEmail );
	}

	@Test
	public void shouldListContributorsAsArrayInPackageJsonFile() throws IOException, MojoExecutionException {
		// given a different configuration of devs/contributors
		MavenProject mavenProject = getProjectConfigWith2Contributors();
		File packageJsonFile = new File( testDir, "package.json" );

		// when
		ResourceFactory.createOrUpdatePackageJson( testDir, gruntVersion, mavenProject, mavenLog );

		// then
		ObjectNode rootNode = (ObjectNode)mapper.readValue( packageJsonFile, JsonNode.class );
		ArrayNode contributors = (ArrayNode)rootNode.get("contributors");
		assertEquals( contributors.size(), 2, "should be 2 contributors" );
		assertNodeContributorEquals( (ObjectNode)rootNode.get("author"), dev );
		assertNodeContributorEquals( (ObjectNode)contributors.get( 0 ), dev2 );
		assertNodeContributorEquals( (ObjectNode)contributors.get( 1 ), dev3 );
	}

	@Test
	public void shouldListSecondaryDevelopersInPackageJsonFileContributorsArray() throws IOException, MojoExecutionException {
		// given
		MavenProject mavenProject = getProjectConfigWith2Developers();

		// when
		ResourceFactory.createOrUpdatePackageJson( testDir, gruntVersion, mavenProject, mavenLog );

		// then
		File packageJsonFile = new File( testDir, "package.json" );
		ObjectNode rootNode = (ObjectNode)mapper.readValue( packageJsonFile, JsonNode.class );
		ArrayNode contributors = (ArrayNode)rootNode.get("contributors");
		assertEquals( contributors.size(), 2, "should be two contributors" );
		assertNodeContributorEquals( (ObjectNode)rootNode.get("author"), dev );
		assertNodeContributorEquals( (ObjectNode)contributors.get( 0 ), dev2 );
		assertNodeContributorEquals( (ObjectNode)contributors.get( 1 ), dev3 );

		assertTextNodeEquals( (ObjectNode)rootNode.get("devDependencies"), "grunt", "~" + gruntVersion );
		assertTextNodeEquals( (ObjectNode)rootNode.get("devDependencies"), "matchdep", "~0.3.0" );
		assertTextNodeEquals( (ObjectNode)rootNode.get("devDependencies"), "grunt-maven", "~1.1.0" );
	}

	@Test
	public void shouldCreateAValidBowerJsonFile() throws IOException, MojoExecutionException {
		// given
		MavenProject mavenProject = getProjectConfigBasic();
		File bowerJsonFile = new File( testDir, "bower.json" );
		bowerJsonFile.delete();

		// when
		ResourceFactory.createOrUpdateBowerJson( testDir, mavenProject, mavenLog );

		// then
		assertThat( bowerJsonFile ).exists();
		ObjectNode rootNode = (ObjectNode)mapper.readValue( bowerJsonFile, JsonNode.class );
		assertTextNodeEquals( rootNode, "name", "empty-project" );
		assertTextNodeEquals( rootNode, "version", "0" );
		assertTextNodeEquals( rootNode, "description", "" );
		assertTextNodeEquals( rootNode, "homepage", "" );
	}

	@Test
	public void shouldFormatContributorDetailsInBowerJsonFile() throws IOException, MojoExecutionException {
		// given
		MavenProject mavenProject = getProjectConfigWithMissingContributorDetails();

		// when
		ResourceFactory.createOrUpdateBowerJson( testDir, mavenProject, mavenLog );

		// then
		File bowerJsonFile = new File( testDir, "bower.json" );
		ObjectNode rootNode = (ObjectNode)mapper.readValue( bowerJsonFile, JsonNode.class );
		ArrayNode authorsNode = (ArrayNode)rootNode.get("authors");
		ArrayNode contributors = (ArrayNode)rootNode.get("contributors");
		assertBowerContributorEquals( (TextNode)authorsNode.get(0), devNameless );
		assertBowerContributorEquals( (TextNode)authorsNode.get(1), devHomeless );
		assertBowerContributorEquals( (TextNode)authorsNode.get(2), devNoEmail );
	}

	@Test
	public void shouldListContributorsAsArrayInBowerJsonFile() throws IOException, MojoExecutionException {
		// given
		MavenProject mavenProject = getProjectConfigWith2Contributors();

		// when
		ResourceFactory.createOrUpdateBowerJson( testDir, mavenProject, mavenLog );

		// then
		File bowerJsonFile = new File( testDir, "bower.json" );
		ObjectNode rootNode = (ObjectNode)mapper.readValue( bowerJsonFile, JsonNode.class );
		ArrayNode authorsNode = (ArrayNode)rootNode.get("authors");
		ArrayNode contributors = (ArrayNode)rootNode.get("contributors");
		assertEquals( contributors.size(), 2, "should be 2 contributors" );
		assertBowerContributorEquals( (TextNode)authorsNode.get(0), dev );
		assertBowerContributorEquals( (TextNode)contributors.get(0), dev2 );
		assertBowerContributorEquals( (TextNode)contributors.get(1), dev3 );
	}

	@Test
	public void shouldListSecondaryDevelopersInBowerJsonFileDevelopersArray() throws IOException, MojoExecutionException {
		// given
		MavenProject mavenProject = getProjectConfigWith2Developers();

		// when
		ResourceFactory.createOrUpdateBowerJson( testDir, mavenProject, mavenLog );

		// then
		File bowerJsonFile = new File( testDir, "bower.json" );
		assertThat( bowerJsonFile ).exists();
		ResourceFactory.createOrUpdateBowerJson( testDir, mavenProject, mavenLog );
		ObjectNode rootNode = (ObjectNode)mapper.readValue( bowerJsonFile, JsonNode.class );

		// Does the package name really need to follow the maven artifactId?  ...probably not
		// assertTextNodeEquals( rootNode, "name", name );
		assertTextNodeEquals( rootNode, "version", version );
		assertTextNodeEquals( rootNode, "description", description );
		assertTextNodeEquals( rootNode, "homepage", url );

		ArrayNode authorsNode = (ArrayNode)rootNode.get("authors");
		ArrayNode contributors = (ArrayNode)rootNode.get("contributors");
		assertEquals( contributors.size(), 1, "should be one contributors" );
		assertBowerContributorEquals( (TextNode)authorsNode.get( 0 ), dev );
		assertBowerContributorEquals( (TextNode)authorsNode.get( 1 ), dev2 );
		assertBowerContributorEquals( (TextNode)contributors.get( 0 ), dev3 );
	}

	private MavenProject getProjectConfigBasic() {
		return new MavenProject();
	}

	private MavenProject getProjectConfigWith2Developers() {
		MavenProject mavenProject = new MavenProject();

		mavenProject.setName( name );
		mavenProject.setVersion( version );
		mavenProject.setDescription( description );
		mavenProject.setUrl( url );
		mavenProject.addDeveloper( dev );
		mavenProject.addDeveloper( (Developer)dev2 );
		mavenProject.addContributor( dev3 );

		return mavenProject;
	}

	private MavenProject getProjectConfigWithMissingContributorDetails() {
		MavenProject mavenProject = new MavenProject();

		mavenProject.setName( name );
		mavenProject.setVersion( version );
		mavenProject.setDescription( description );
		mavenProject.setUrl( url );
		mavenProject.addDeveloper( (Developer)devNameless );
		mavenProject.addDeveloper( (Developer)devHomeless );
		mavenProject.addDeveloper( (Developer)devNoEmail );

		return mavenProject;
	}

	private MavenProject getProjectConfigWith2Contributors() {
		MavenProject mavenProject = getProjectConfigWith2Developers();

		mavenProject.setDevelopers( Arrays.asList(dev) );
		mavenProject.setContributors( Arrays.asList(dev2, dev3) );

		return mavenProject;
	}

	private void assertTextNodeEquals( ObjectNode rootNode, String fieldName, String expectedValue ) {
		TextNode node = (TextNode)rootNode.get(fieldName);
		if( expectedValue == null ) {
			assertNull( node, fieldName + " should be null" );
		} else {
			assertEquals( node.textValue(), expectedValue, "incorrect value for " + fieldName );
		}
	}

	private void assertNodeContributorEquals( ObjectNode node, Contributor contributor ) {
		assertTextNodeEquals( node, "name", contributor.getName() );
		assertTextNodeEquals( node, "email", contributor.getEmail() );
		assertTextNodeEquals( node, "url", contributor.getUrl() );
	}

	private void assertBowerContributorEquals( TextNode node, Contributor contributor ) {
		if( contributor.getName() != null ) {
			if( contributor.getEmail() != null ) {
				assertEquals( node.textValue(), contributor.getName() + " <" + contributor.getEmail() + ">" );
			} else {
				assertEquals( node.textValue(), contributor.getName() );
			}
		} else {
			assertEquals( node.textValue(), contributor.getEmail() );
		}
	}

	private void assertFileContainsLine( File file, String expectedLine ) throws IOException {
		BufferedReader in = null;
		try {
			boolean foundModuleExports = false;
			in = new BufferedReader( new FileReader( file ) );
			String line;
			while( (line = in.readLine()) != null ) {
				if( expectedLine.equals( line ) ) {
					foundModuleExports = true;
					break;
				}
			}
			assertTrue( foundModuleExports, "Didn't find '" + expectedLine + "'" );
		} finally {
			if( in != null ) {
				in.close();
			}
		}
	}
}
