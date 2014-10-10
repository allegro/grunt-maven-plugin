/**
 * 
 */
package pl.allegro.tdr.gruntmaven;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author loopingz
 * 
 * Allow you to use your maven repository as a bower repository
 * This way you have the JS module and the REST service in a same package
 */
@Mojo(name = "bower-maven", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class BowerRepositoryMojo extends BaseMavenGruntMojo {

	private class WebjarComponent {
		String name;
		String version;
		ZipOutputStream output;
		String zipFileName;
		
		public WebjarComponent(String fullname, String outputDir) throws FileNotFoundException {
			name = fullname.substring(0,fullname.indexOf('/'));
			version = fullname.substring(fullname.indexOf('/')+1);
			zipFileName = outputDir + name + "-" + version + ".zip";
			output = new ZipOutputStream(new FileOutputStream(
					zipFileName));
		}
		
		public String toString() {
			return name + "-" + version + ".zip";
		}
		public String getName() {
			return name;
		}
		public String getZipName() {
			return zipFileName;
		}
		public ZipOutputStream getOutput() {
			return output;
		}
	}
	private static final String BOWER_MAVEN_FOLDER = "bower-maven-repo/";

	private static final String WEBJARS_PATH = "META-INF/resources/webjars/";

	private Map<Object, Object> bowerDependencies = null;

	@Component
	private org.apache.maven.project.MavenProject project;

	/**
	 * The dependency tree builder to use.
	 */
	@Component(hint = "default")
	private DependencyGraphBuilder dependencyGraphBuilder;

    @Component
    private RepositorySystem repository;

	/**
	 * List of Remote Repositories used by the resolver
	 * 
	 * @parameter expression="${project.remoteArtifactRepositories}"
	 * @readonly
	 * @required
	 */
	@Component
	protected List<ArtifactRepository> remoteRepositories;

	/**
	 * Location of the local repository.
	 * 
	 * @parameter expression="${localRepository}"
	 * @readonly
	 * @required
	 */
	@Parameter(defaultValue = "${localRepository}")
	protected ArtifactRepository localRepository;
	/**
	 * The scope to filter by when resolving the dependency tree, or
	 * <code>null</code> to include dependencies from all scopes. Note that this
	 * feature does not currently work due to MNG-3236.
	 * 
	 * @see <a href="http://jira.codehaus.org/browse/MNG-3236">MNG-3236</a>
	 * @since 2.0-alpha-5
	 */
	@Parameter(property = "scope")
	private String scope;

	@Parameter(property = "jarSourceDirectory")
	private String jarSourceDirectory = WEBJARS_PATH;

	protected String getComponentNameFromPath(String path) {
		if (path.length() < 4) {
			return null;
		}
		Integer limit = path.indexOf('/', path.indexOf('/')+1);
		if (limit < 0) {
			return null;
		}
		String componentFullname = path.substring(0,limit);
		if (path.length() == componentFullname.length() + 1) {
			return null;
		}
		return componentFullname;
	}
	
	private void extractWebjar(File jarFile, String outputDir)
			throws FileNotFoundException {
		// Load the jar
		ZipInputStream stream = new ZipInputStream(new FileInputStream(jarFile));
		ZipEntry entry;
		if (!outputDir.endsWith(File.separator)) {
			outputDir += File.separator;
		}
		Map<String,WebjarComponent> components = new HashMap<String,WebjarComponent>();
		/*
		 *  Extract everything from the META-INF/resources/webjars
		 *  
		 *  Then component/version/contents
		 */
		try {
			while ((entry = stream.getNextEntry()) != null) {
				if (entry.getName().startsWith(jarSourceDirectory)
						&& entry.getName().length() > jarSourceDirectory.length()) {
					// component/version/contents
					String path = entry.getName().substring(
							jarSourceDirectory.length());
					String componentFullname = getComponentNameFromPath(path);
					if (componentFullname == null) {
						continue;
					}
					getLog().info(path);
					getLog().info(componentFullname);
					path = path.substring(componentFullname.length()+1);
					ZipEntry outEntry = new ZipEntry(path);
					WebjarComponent component = components.get(componentFullname);
					if (component == null) {
						component = new WebjarComponent(componentFullname, outputDir);
						components.put(componentFullname, component);
					}
					outEntry.setSize(entry.getSize());
					component.getOutput().putNextEntry(outEntry);
					IOUtils.copy(stream, component.getOutput());
					component.getOutput().closeEntry();
				}
			}
			
		} catch (IOException e) {
		} finally {
			if (components.size() > 0) {
				getLog().info("Adding bower dependencies from webjar " + jarFile.getName());
			}
			for (WebjarComponent component : components.values()) {
				IOUtils.closeQuietly(component.getOutput());
				getLog().info("-" + component.toString());
				bowerDependencies.put(jarFile.getName()+"-"+component.getName(),
						new File(component.getZipName()).getAbsolutePath());
			}
			IOUtils.closeQuietly(stream);
		}
	}
	
	private boolean extractBower(Artifact child, String outputDir)
			throws FileNotFoundException {
		// Load the jar
		File jarFile = child.getFile();
		ZipInputStream stream = new ZipInputStream(new FileInputStream(jarFile));
		ZipOutputStream out = null;
		ZipEntry entry;
		if (!outputDir.endsWith(File.separator)) {
			outputDir += File.separator;
		}
		String suffix = "";
		if (child.isSnapshot()) {
			suffix = new Long(new Date().getTime()).toString();
		}
		String zipFileName = outputDir + jarFile.getName() + suffix + ".zip";
		// Extract everything from the META-INF/bower/ repository if it exists
		try {
			while ((entry = stream.getNextEntry()) != null) {
				if (entry.getName().startsWith(jarSourceDirectory)
						&& entry.getName().length() > jarSourceDirectory.length()) {
					ZipEntry outEntry = new ZipEntry(entry.getName().substring(
							jarSourceDirectory.length()));
					if (out == null) {
						out = new ZipOutputStream(new FileOutputStream(
								zipFileName));
					}
					outEntry.setSize(entry.getSize());
					out.putNextEntry(outEntry);
					IOUtils.copy(stream, out);
					out.closeEntry();
				}
			}
			if (out != null) {
				out.flush();
				out.close();
			}
		} catch (IOException e) {
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(stream);
		}
		if (out != null) {
			getLog().info("Adding bower dependency " + jarFile.getName());
			bowerDependencies.put(child.getGroupId() + "." + child.getArtifactId(),
					new File(zipFileName).getAbsolutePath());
			// Might go recursive here
			return true;
		}
		return false;
	}

	@Override
	protected void executeInternal() throws MojoExecutionException,
			MojoFailureException {
		getLog().info("Browse all dependencies to check for bower package");
		if (project == null) {
			getLog().warn("Project is null");
			return;
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		if (!gruntBuildDirectory.endsWith("/")) {
			gruntBuildDirectory += "/";
		}

		// Load the bower.json
		String filename = gruntBuildDirectory;
		filename += "bower.json";
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		try {
			map = gson.fromJson(new FileReader(filename),
					map.getClass());
		} catch (Exception e1) {
			getLog().warn("Cant load bower.json");
		}
		// Get the declared dependencies
		if (!map.containsKey("dependencies")) {
			bowerDependencies = new HashMap<Object, Object>();
			map.put("dependencies", bowerDependencies);
		} else {
			bowerDependencies = (Map<Object, Object>) map.get("dependencies");
		}

		// For each maven dep check for bower tag
		for (Object art : project.getDependencyArtifacts()) {
			Artifact artifact = (Artifact) art;
			if (artifact == null || artifact.getFile() == null) {
				continue;
			}
		}

		if (dependencyGraphBuilder == null) {
			getLog().error("Can't find dep graph builder");
			return;
		}

		try {
			File file = new File(gruntBuildDirectory+BOWER_MAVEN_FOLDER);
			file.mkdirs();
			DependencyNode rootNode = dependencyGraphBuilder
					.buildDependencyGraph(project,
							createResolvingArtifactFilter());
			handleBowerDependency(rootNode);
		} catch (DependencyGraphBuilderException e) {
			getLog().error("The dependency graph can't be buld");
		}

		// Export the new bower file
		try {
			FileWriter writer = new FileWriter(filename);
			writer.write(gson.toJson(map));
			writer.close();
		} catch (IOException e) {
			getLog().error("Can't write the bower.json");
		}
	}

	/**
	 * Gets the artifact filter to use when resolving the dependency tree.
	 * 
	 * @return the artifact filter
	 */
	private ArtifactFilter createResolvingArtifactFilter() {
		ArtifactFilter filter;

		// filter scope
		if (scope != null) {
			getLog().debug(
					"+ Resolving dependency tree for scope '" + scope + "'");
			filter = new ScopeArtifactFilter(scope);
		} else {
			filter = null;
		}
		return filter;
	}

	private void handleBowerDependency(DependencyNode rootNode) {
		if (rootNode == null) {
			return;
		}
		for (DependencyNode child : rootNode.getChildren()) {
			try {
				// Resolve every artifact
				ArtifactResolutionRequest request = new ArtifactResolutionRequest();
				request.setArtifact(child.getArtifact());
				request.setRemoteRepositories(remoteRepositories);
				request.setLocalRepository(localRepository);
				repository.resolve(request);
			} catch (Exception e) {
				getLog().warn(
						"Can't resolve the artifact "
								+ child.getArtifact().getGroupId() + ":"
								+ child.getArtifact().getArtifactId());
			}
			boolean webJarMode = WEBJARS_PATH.equals(jarSourceDirectory);
			if (child.getArtifact() != null
					&& child.getArtifact().getFile() != null) {
				try {
					if (webJarMode) {
						extractWebjar(child.getArtifact().getFile(), gruntBuildDirectory+BOWER_MAVEN_FOLDER);
					} else {
						extractBower(child.getArtifact(), gruntBuildDirectory+BOWER_MAVEN_FOLDER);
					}
				} catch (FileNotFoundException e) {
					getLog().warn("The artifact file is not found");
				}
			}
			handleBowerDependency(child);
		}
	}
}
