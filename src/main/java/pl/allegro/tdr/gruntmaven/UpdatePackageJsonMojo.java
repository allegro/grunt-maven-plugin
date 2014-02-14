package pl.allegro.tdr.gruntmaven;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.maven.model.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import pl.allegro.tdr.gruntmaven.resources.Resource;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Synchronises <code>package.json</code> with <code>pom.xml</code>
 */
@Mojo(name = "update-package-json", defaultPhase = LifecyclePhase.VALIDATE)
public class UpdatePackageJsonMojo extends BaseMavenGruntMojo {

	private static final String PACKAGE_JSON_RESOURCE_NAME = "package.json";
	private static final String GRUNT_FILE_RESOURCE_NAME = "Gruntfile.js";

	@Parameter(property = "gruntVersion", defaultValue = "0.4.2")
	private String gruntVersion;



	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		String targetFileName = sourceDirectory + jsSourceDirectory + File.separator + PACKAGE_JSON_RESOURCE_NAME;
		File targetFile = new File(targetFileName);
		if( !targetFile.exists() ) {
			createPackageJson(targetFileName);
		}
		updatePackageJson(targetFile);

		targetFileName = sourceDirectory + jsSourceDirectory + File.separator + GRUNT_FILE_RESOURCE_NAME;
		targetFile = new File(targetFileName);
		if( !targetFile.exists() ) {
			createGruntfile(targetFileName);
		}
	}

	private void createGruntfile( String targetFileName ) {
		Resource.from( "/" + GRUNT_FILE_RESOURCE_NAME, getLog() )
				.copy( targetFileName );
		getLog().info("Created grunt file: " + targetFileName);
	}

	private void createPackageJson( String targetFileName ) {
		String url = mavenProject.getUrl();
		if( url == null ) { url = ""; }

		String description = mavenProject.getDescription();
		if( description == null ) { description = ""; }

		Resource.from( "/" + PACKAGE_JSON_RESOURCE_NAME, getLog() )
				.withFilter("project.artifactId", mavenProject.getArtifactId() )
				.withFilter("project.version", mavenProject.getVersion() )
				.withFilter("project.description", description)
				.withFilter("project.url", url)
				.withFilter("grunt.version", gruntVersion )
				.copy( targetFileName );
		getLog().info("Created grunt project file: " + targetFileName);
	}

	private void updatePackageJson( File targetFile ) throws MojoExecutionException {
		try {
			JsonNodeFactory factory = new JsonNodeFactory(false);
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode rootNode = (ObjectNode)mapper.readValue( targetFile, JsonNode.class );

			rootNode.set( "version", new TextNode( mavenProject.getVersion()) );

			String url = mavenProject.getUrl();
			if( url != null ) {
				rootNode.set("homepage", new TextNode(url));
			}

			String description = mavenProject.getDescription();
			if( description != null ) {
				rootNode.set("description", new TextNode(description));
			}

			ArrayNode contributorsNode = new ArrayNode( factory );
			List<Developer> developers = mavenProject.getDevelopers();
			if( developers != null && !developers.isEmpty() ) {
//				for( Developer developer : developers ) {
//					if( developer.getRoles().contains("lead") ) {}
//				}
				Developer developer = developers.get(0);
				ObjectNode authorNode = createContributorNode( factory, developer );
				rootNode.set("author", authorNode);

				for( int i = 1; i < developers.size(); i++ ) {
					developer = developers.get(i);
					contributorsNode.add( createContributorNode( factory, developer ) );
				}
			}

			List<Contributor> contributors = mavenProject.getContributors();
			if( contributors != null && !contributors.isEmpty() ) {
				for( Contributor contributor : contributors ) {
					contributorsNode.add( createContributorNode( factory, contributor ) );
				}
			}
			if( contributorsNode.size() != 0 ) {
				rootNode.set("contributors", contributorsNode );
			}

			Scm scm = mavenProject.getScm();
			if( scm != null ) {
				ObjectNode repoNode = new ObjectNode( factory );
				url = scm.getUrl();
				if( url != null ) {
					repoNode.set("url", new TextNode(url));
				}
				String connection = scm.getConnection();
				if( connection != null ) {
					repoNode.set("type", new TextNode(connection.substring(4, connection.indexOf(':', 4))));
				}
				rootNode.set("repository", repoNode);
			}

			IssueManagement issueManagement = mavenProject.getIssueManagement();
			if( issueManagement != null ) {
				url = issueManagement.getUrl();
				if( url != null ) {
					rootNode.set( "bugs.url", new TextNode(url) );
				}
			}

			List<License> licenses = mavenProject.getLicenses();
			if( licenses != null && !licenses.isEmpty() ) {
				String name = licenses.get(0).getName();
				if( name != null ) {
					rootNode.set("license", new TextNode(name));
				}
			}

			mapper.configure( SerializationFeature.INDENT_OUTPUT, true );
			mapper.writeValue( targetFile, rootNode );
			getLog().info("Updated grunt project file: " + targetFile);
		} catch( IOException e ) {
			throw new MojoExecutionException("Failed to update " + targetFile, e);
		}
	}

	private ObjectNode createContributorNode( JsonNodeFactory factory, Contributor contributor ) {
		ObjectNode contributorNode = new ObjectNode( factory );

		String name = contributor.getName();
		String email = contributor.getEmail();
		String url = contributor.getUrl();
		if( name != null ) {
			contributorNode.set( "name", new TextNode( name ) );
		}
		if( email != null ) {
			contributorNode.set( "email", new TextNode( email ) );
		}
		if( url != null ) {
			contributorNode.set( "url", new TextNode( url ) );
		}

		return contributorNode;
	}
}
