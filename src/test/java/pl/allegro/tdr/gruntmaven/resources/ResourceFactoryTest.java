package pl.allegro.tdr.gruntmaven.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.Developer;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.junit.AfterClass;
import org.junit.Test;

import javax.xml.soap.Text;

import static org.junit.Assert.*;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

public class ResourceFactoryTest {
	private String name = "grunt-maven-plugin";
	private String version = "1.2.3";
	private String description = "Testing, 1-2-3...";
	private String url = "https://github.com/allegro/grunt-maven-plugin";
	private Developer dev = new Developer();
	private Contributor dev2 = new Developer();
	private Contributor dev3 = new Developer();

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
	}

	@AfterClass
	public static void cleanup() {
		new File("test", "Gruntfile.js").delete();
		new File("test", "package.json").delete();
		new File("test", "bower.json").delete();
		new File("test").delete();
	}


	@Test
	public void testCreateGruntfile() {
		String fileName = "test/Gruntfile.js";
		ResourceFactory.createGruntfile( fileName, new SystemStreamLog() );
		File file = new File( fileName );
		assertTrue( fileName + " doesn't even exist", file.exists() );
		try {
			boolean foundModuleExports = false;
			BufferedReader in = new BufferedReader( new FileReader( file ) );
			String line;
			while( (line = in.readLine()) != null ) {
				if( "module.exports = function(grunt) {".equals( line ) ) {
					foundModuleExports = true;
					break;
				}
			}
			assertTrue( "Didn't find 'module.exports = function(grunt) {'", foundModuleExports );
		} catch( Exception e ) {
			assertNull( "unexpected exception: " + e.getMessage(), e );
			e.printStackTrace();
		}
	}

	@Test
	public void testCreateOrUpdatePackageJson() {
		SystemStreamLog log = new SystemStreamLog();
		String gruntVersion =  "0.4.2";

		try {
			File packageJsonFile = new File( "test", "package.json" );
			// ------ First, create the file from scratch ------
			packageJsonFile.delete();

			MavenProject mavenProject = getProjectConfig1();
			ResourceFactory.createOrUpdatePackageJson( "test", gruntVersion, mavenProject, log );
			assertTrue( "package.json was not generated", packageJsonFile.exists() );

			JsonNodeFactory factory = new JsonNodeFactory(false);
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode rootNode = (ObjectNode)mapper.readValue( packageJsonFile, JsonNode.class );
			assertTextNodeEquals( rootNode, "name", "empty-project" );
			assertTextNodeEquals( rootNode, "version", "0" );
			assertTextNodeEquals( rootNode, "description", "" );
			assertTextNodeEquals( rootNode, "homepage", "" );
			assertTextNodeEquals( (ObjectNode)rootNode.get("devDependencies"),
									"grunt", "~" + gruntVersion );
			assertTextNodeEquals( (ObjectNode)rootNode.get("devDependencies"),
									"matchdep", "~0.3.0" );
			assertTextNodeEquals( (ObjectNode)rootNode.get("devDependencies"),
									"grunt-maven", "~1.1.0" );

			// ------ Now update the project and run again ------
			mavenProject = getProjectConfig2();
			ResourceFactory.createOrUpdatePackageJson( "test", gruntVersion, mavenProject, log );
			rootNode = (ObjectNode)mapper.readValue( packageJsonFile, JsonNode.class );

			// Does the package name really need to follow the maven artifactId?  ...probably not
			// assertTextNodeEquals( rootNode, "name", name );
			assertTextNodeEquals( rootNode, "version", version );
			assertTextNodeEquals( rootNode, "description", description );
			assertTextNodeEquals( rootNode, "homepage", url );

			ObjectNode authorNode = (ObjectNode)rootNode.get("author");
			ArrayNode contributors = (ArrayNode)rootNode.get("contributors");
			assertEquals( "should be two contributors", 2, contributors.size() );
			ObjectNode dev2Node = (ObjectNode)contributors.get( 0 );
			ObjectNode dev3Node = (ObjectNode)contributors.get( 1 );

			assertTextNodeEquals( authorNode, "name", "Grupa Allegro" );
			assertTextNodeEquals( authorNode, "email", "grupa@domain.com" );
			assertTextNodeEquals( authorNode, "url", "https://github.com/allegro" );

			assertTextNodeEquals( dev2Node, "name", "Nicholas Albion" );
			assertTextNodeEquals( dev2Node, "email", "nalbion@domain.com" );
			assertTextNodeEquals( dev2Node, "url", "https://github.com/nalbion" );

			assertTextNodeEquals( dev3Node, "name", "Developer Three" );
			assertTextNodeEquals( dev3Node, "email", "dev3@domain.com" );
			assertTextNodeEquals( dev3Node, "url", "https://github.com/dev3" );

			assertTextNodeEquals( (ObjectNode)rootNode.get("devDependencies"),
					"grunt", "~" + gruntVersion );
			assertTextNodeEquals( (ObjectNode)rootNode.get("devDependencies"),
					"matchdep", "~0.3.0" );
			assertTextNodeEquals( (ObjectNode)rootNode.get("devDependencies"),
					"grunt-maven", "~1.1.0" );

			// ------ Now try a different configuration of devs/contributors ------
			mavenProject = getProjectConfig3();
			packageJsonFile.delete();
			ResourceFactory.createOrUpdatePackageJson( "test", gruntVersion, mavenProject, log );
			rootNode = (ObjectNode)mapper.readValue( packageJsonFile, JsonNode.class );

			authorNode = (ObjectNode)rootNode.get("author");
			contributors = (ArrayNode)rootNode.get("contributors");
			assertEquals( "should be 2 contributors", 2, contributors.size() );
			dev2Node = (ObjectNode)contributors.get( 0 );
			dev3Node = (ObjectNode)contributors.get( 1 );

			assertTextNodeEquals( authorNode, "name", "Grupa Allegro" );
			assertTextNodeEquals( authorNode, "email", "grupa@domain.com" );
			assertTextNodeEquals( authorNode, "url", "https://github.com/allegro" );

			assertTextNodeEquals( dev2Node, "name", "Nicholas Albion" );
			assertTextNodeEquals( dev2Node, "email", "nalbion@domain.com" );
			assertTextNodeEquals( dev2Node, "url", "https://github.com/nalbion" );

			assertTextNodeEquals( dev3Node, "name", "Developer Three" );
			assertTextNodeEquals( dev3Node, "email", "dev3@domain.com" );
			assertTextNodeEquals( dev3Node, "url", "https://github.com/dev3" );
		} catch( Exception e ) {
			e.printStackTrace();
			assertNull( "unexpected exception: " + e.getMessage(), e );
		}
	}

	@Test
	public void testCreateOrUpdateBowerJson() {
		SystemStreamLog log = new SystemStreamLog();

		try {
			File packageJsonFile = new File( "test", "bower.json" );
			// ------ First, create the file from scratch ------
			packageJsonFile.delete();

			MavenProject mavenProject = getProjectConfig1();
			ResourceFactory.createOrUpdateBowerJson( "test", mavenProject, log );
			assertTrue( "package.json was not generated", packageJsonFile.exists() );

			JsonNodeFactory factory = new JsonNodeFactory(false);
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode rootNode = (ObjectNode)mapper.readValue( packageJsonFile, JsonNode.class );
			assertTextNodeEquals( rootNode, "name", "empty-project" );
			assertTextNodeEquals( rootNode, "version", "0" );
			assertTextNodeEquals( rootNode, "description", "" );
			assertTextNodeEquals( rootNode, "homepage", "" );

			// ------ Now update the project and run again ------
			mavenProject = getProjectConfig2();
			ResourceFactory.createOrUpdateBowerJson( "test", mavenProject, log );
			rootNode = (ObjectNode)mapper.readValue( packageJsonFile, JsonNode.class );

			// Does the package name really need to follow the maven artifactId?  ...probably not
			// assertTextNodeEquals( rootNode, "name", name );
			assertTextNodeEquals( rootNode, "version", version );
			assertTextNodeEquals( rootNode, "description", description );
			assertTextNodeEquals( rootNode, "homepage", url );

			ArrayNode authorsNode = (ArrayNode)rootNode.get("authors");
			ArrayNode contributors = (ArrayNode)rootNode.get("contributors");
			assertEquals( "should be one contributors", 1, contributors.size() );
			TextNode dev3Node = (TextNode)contributors.get( 0 );

			TextNode authorNode = (TextNode)authorsNode.get( 0 );
			assertEquals( "Grupa Allegro <grupa@domain.com>", authorNode.textValue() );

			TextNode dev2Node = (TextNode)authorsNode.get( 1 );
			assertEquals( "Nicholas Albion <nalbion@domain.com>", dev2Node.textValue() );
			assertEquals( "Developer Three <dev3@domain.com>", dev3Node.textValue() );

			// ------ Now try a different configuration of devs/contributors ------
			mavenProject = getProjectConfig3();
			packageJsonFile.delete();
			ResourceFactory.createOrUpdateBowerJson( "test", mavenProject, log );
			rootNode = (ObjectNode)mapper.readValue( packageJsonFile, JsonNode.class );

			authorsNode = (ArrayNode)rootNode.get("authors");
			authorNode = (TextNode)authorsNode.get(0);
			contributors = (ArrayNode)rootNode.get("contributors");
			assertEquals( "should be 2 contributors", 2, contributors.size() );
			dev2Node = (TextNode)contributors.get( 0 );
			dev3Node = (TextNode)contributors.get( 1 );

			assertEquals( "Grupa Allegro <grupa@domain.com>", authorNode.textValue() );

			assertEquals( "Nicholas Albion <nalbion@domain.com>", dev2Node.textValue() );
			assertEquals( "Developer Three <dev3@domain.com>", dev3Node.textValue() );
		} catch( Exception e ) {
			e.printStackTrace();
			assertNull( "unexpected exception: " + e.getMessage(), e );
		}
	}

	private MavenProject getProjectConfig1() {
		return new MavenProject();
	}

	private MavenProject getProjectConfig2() {
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

	private MavenProject getProjectConfig3() {
		MavenProject mavenProject = getProjectConfig2();

		mavenProject.setDevelopers( Arrays.asList(dev) );
		mavenProject.setContributors( Arrays.asList(dev2, dev3) );

		return mavenProject;
	}

	private void assertTextNodeEquals( ObjectNode rootNode, String fieldName, String expectedValue ) {
		assertEquals( "incorrect value for " + fieldName,
						expectedValue,
					((TextNode)rootNode.get(fieldName)).textValue() );
	}
}
