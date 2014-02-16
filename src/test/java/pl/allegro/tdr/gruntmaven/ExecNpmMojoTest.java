package pl.allegro.tdr.gruntmaven;

import org.junit.Test;
import pl.allegro.tdr.gruntmaven.resources.ExecutionException;

import java.lang.reflect.Field;

public class ExecNpmMojoTest {

	@Test
	public void testInstallGrunt() throws Exception {
		System.out.println("grunt is not installed locally - installing now");
		ExecNpmMojo npmMojo = new ExecNpmMojo();
		initialiseExecutableMojo( npmMojo );

		Field npmExecutable = ExecNpmMojo.class.getDeclaredField( "npmExecutable" );
		npmExecutable.setAccessible( true );
		npmExecutable.set( npmMojo, "npm" );

		// This hangs forever (and the alternative approach below actually tests the exposed functionality)
		try {
			npmMojo.executeCommand("install", "grunt", 10 * 1000, 10 * 1000);
		} catch( ExecutionException e ) {
			if( e.getExitValue() == 7 ) {
				System.out.println("ignore exit code 7");
			} else {
				System.out.println("unexpected exit code: " + e.getExitValue());
				e.printStackTrace();
			}
		}
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
