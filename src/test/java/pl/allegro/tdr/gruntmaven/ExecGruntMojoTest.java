package pl.allegro.tdr.gruntmaven;

import org.junit.Test;
import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class ExecGruntMojoTest {

	@Test
	public void testGetVersion() throws Exception {
		// This won't actually work unless grunt is installed locally for the grunt-maven-plugin project
		ExecGruntMojo gruntMojo = new ExecGruntMojo();
		initialiseExecutableMojo( gruntMojo );

		Field gruntExecutable = ExecGruntMojo.class.getDeclaredField( "gruntExecutable" );
		gruntExecutable.setAccessible( true );
		gruntExecutable.set( gruntMojo, "grunt" );

		String version = gruntMojo.getVersion();

		if( version == null ) {
			new ExecNpmMojoTest().testInstallGrunt();
		}

		version = gruntMojo.getVersion();
		System.out.println("ExecGruntMojoTest.testGetVersion(): '" + version + "'");
		assertNotNull( "was unable to determine grunt version", version );
	}

	private void initialiseExecutableMojo( AbstractExecutableMojo mojo ) throws Exception {
		Field osName = AbstractExecutableMojo.class.getDeclaredField( "osName" );
		osName.setAccessible( true );
		osName.set( mojo, System.getProperty("os.name") );

		Field gruntBuildDirectory = BaseMavenGruntMojo.class.getDeclaredField( "gruntBuildDirectory" );
		gruntBuildDirectory.setAccessible( true );
		gruntBuildDirectory.set( mojo, System.getProperty("user.dir") ); //+ File.separator + "src/main/resources" );
	}
}
