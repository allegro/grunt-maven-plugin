package pl.allegro.tdr.gruntmaven.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.maven.model.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ResourceFactory {
	private static final String GRUNT_FILE_RESOURCE_NAME = "Gruntfile.js";
	private static final String PACKAGE_JSON_RESOURCE_NAME = "package.json";
	private static final String BOWER_JSON_RESOURCE_NAME = "bower.json";


	public static void createGruntfile( String targetFileName, Log log ) {
		Resource.from( "/" + GRUNT_FILE_RESOURCE_NAME, log )
				.copy( targetFileName );
		log.info( "Created grunt file: " + targetFileName );
	}

	/**
	 * Creates or updates a "package.json" file under the specified <code>sourceDirectory</code>
	 * @param sourceDirectory - <code>sourceDirectory + jsSourceDirectory</code> eg: src/main/webapp/static
	 * @param gruntVersion
	 * @param mavenProject
	 * @param log
	 * @throws MojoExecutionException if an IOException was encountered updating the file
	 */
	public static void createOrUpdatePackageJson( String sourceDirectory, String gruntVersion, MavenProject mavenProject, Log log )
			throws MojoExecutionException
	{
		String targetFileName = generateTargetFileName( sourceDirectory, PACKAGE_JSON_RESOURCE_NAME );
		File targetFile = new File(targetFileName);
		if( !targetFile.exists() ) {
			createPackageJson(targetFileName, gruntVersion, mavenProject, log);
		}
		updatePackageJson(targetFile, gruntVersion, mavenProject, log);
	}

	/**
	 * Creates or updates a "bower.json" file under the specified <code>sourceDirectory</code>
	 * @param sourceDirectory - <code>sourceDirectory + jsSourceDirectory</code> eg: src/main/webapp/static
	 * @param mavenProject
	 * @param log
	 * @throws MojoExecutionException if an IOException was encountered updating the file
	 */
	public static void createOrUpdateBowerJson( String sourceDirectory, MavenProject mavenProject, Log log )
			throws MojoExecutionException
	{
		String targetFileName = generateTargetFileName( sourceDirectory, BOWER_JSON_RESOURCE_NAME );
		File targetFile = new File(targetFileName);
		if( !targetFile.exists() ) {
			createBowerJson( targetFileName, mavenProject, log );
		}
		updateBowerJson( targetFile, mavenProject, log );
	}

	/**
	 * @param targetFileName - absolute path of package.json
	 * @param gruntVersion
	 * @param mavenProject
	 * @param log
	 */
	private static void createPackageJson( String targetFileName, String gruntVersion, MavenProject mavenProject, Log log ) {
		String url = mavenProject.getUrl();
		if( url == null ) { url = ""; }

		String description = mavenProject.getDescription();
		if( description == null ) { description = ""; }

		Resource.from( "/" + PACKAGE_JSON_RESOURCE_NAME, log )
				.withFilter("project.artifactId", mavenProject.getArtifactId())
				.withFilter("project.version", mavenProject.getVersion())
				.withFilter("project.description", description)
				.withFilter("project.url", url)
				.withFilter("grunt.version", gruntVersion)
				.copy( targetFileName );
		log.info( "Created grunt project file: " + targetFileName );
	}

	private static void updatePackageJson( File targetFile, String gruntVersion, MavenProject mavenProject, Log log )
			throws MojoExecutionException
	{
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
				ObjectNode bugsNode = new ObjectNode( factory );
				url = issueManagement.getUrl();
				if( url != null ) {
					bugsNode.set( "url", new TextNode(url) );
				}
				rootNode.set( "bugs", bugsNode );
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
			log.info( "Updated grunt project file: " + targetFile );
		} catch( IOException e ) {
			throw new MojoExecutionException("Failed to update " + targetFile, e);
		}
	}

	/**
	 * Creates a template bower.json file.
	 * "main" is set to "js/${project.artifactId}.js", "private" is set to true
	 * It is up to the user to edit these values, updateBowerJson() will not make any changes to them
	 * @param targetFileName - - absolute path of bower.json
	 * @param mavenProject
	 * @param log
	 */
	private static void createBowerJson( String targetFileName, MavenProject mavenProject, Log log ) {
		String url = mavenProject.getUrl();
		if( url == null ) { url = ""; }

		String description = mavenProject.getDescription();
		if( description == null ) { description = ""; }

		Resource.from( "/" + BOWER_JSON_RESOURCE_NAME, log )
				.withFilter("project.artifactId", mavenProject.getArtifactId())
				.withFilter("project.version", mavenProject.getVersion())
				.withFilter("project.description", description)
				.withFilter("project.url", url)
				.copy( targetFileName );
		log.info( "Created bower.json" );
	}

	private static void updateBowerJson( File targetFile, MavenProject mavenProject, Log log )
			throws MojoExecutionException
	{
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

			ArrayNode authorsNode = new ArrayNode( factory );
			List<Developer> developers = mavenProject.getDevelopers();
			if( developers != null && !developers.isEmpty() ) {
				for( Developer developer : developers ) {
					TextNode contributorNode = createBowerContributorNode( factory, developer );
					if( contributorNode != null ) {
						authorsNode.add( contributorNode );
					}
				}
			}
			if( authorsNode.size() != 0 ) {
				rootNode.set("authors", authorsNode);
			}

			ArrayNode contributorsNode = new ArrayNode( factory );
			List<Contributor> contributors = mavenProject.getContributors();
			if( contributors != null && !contributors.isEmpty() ) {
				for( Contributor contributor : contributors ) {
					TextNode contributorNode = createBowerContributorNode( factory, contributor );
					if( contributorNode != null ) {
						contributorsNode.add( contributorNode );
					}
				}
			}
			if( contributorsNode.size() != 0 ) {
				rootNode.set("contributors", contributorsNode );
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
			log.info( "Updated bower.json" );
		} catch( IOException e ) {
			throw new MojoExecutionException("Failed to update " + targetFile, e);
		}
	}

	private static String generateTargetFileName( String sourceDirectory, String resourceName ) {
		StringBuilder str = new StringBuilder( sourceDirectory );
//		if( str.charAt( str.length() - 1 ) != File.separatorChar ) {
		if( !sourceDirectory.endsWith(File.separator) ) {
			str.append( File.separator );
		}

		return str.append(resourceName).toString();
	}

	/**
	 * "bower init" generates "Author Name &lt;email@domain.com>"
	 * @param factory
	 * @param contributor
	 * @return
	 */
	private static TextNode createBowerContributorNode( JsonNodeFactory factory, Contributor contributor ) {
		String name = contributor.getName();
		String email = contributor.getEmail();

		StringBuilder str = null;
		if( name != null ) {
			str = new StringBuilder(name);
		}
		if( email != null ) {
			if( str == null ) {
				return new TextNode(email);
			}
			str.append(" <").append(email).append(">");
		}
		return str == null ? null : new TextNode( str.toString() );
	}

	private static ObjectNode createContributorNode( JsonNodeFactory factory, Contributor contributor ) {
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
