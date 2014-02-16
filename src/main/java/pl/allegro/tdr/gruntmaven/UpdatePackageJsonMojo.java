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
import pl.allegro.tdr.gruntmaven.resources.ResourceFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Synchronises <code>package.json</code> and <code>bower.json</code> with <code>pom.xml</code>
 */
@Mojo(name = "update-package-json", defaultPhase = LifecyclePhase.VALIDATE)
public class UpdatePackageJsonMojo extends BaseMavenGruntMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		String path = sourceDirectory + jsSourceDirectory;
		ResourceFactory.createOrUpdatePackageJson( path, gruntVersion, mavenProject, getLog() );
		ResourceFactory.createOrUpdateBowerJson( path, mavenProject, getLog() );
	}
}
