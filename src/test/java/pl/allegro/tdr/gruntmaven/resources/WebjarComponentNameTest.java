/**
 * 
 */
package pl.allegro.tdr.gruntmaven.resources;

import org.junit.Assert;
import org.junit.Test;

import pl.allegro.tdr.gruntmaven.BowerRepositoryMojo;

/**
 * @author Remi Cattiau
 *
 */
public class WebjarComponentNameTest extends BowerRepositoryMojo {
	@Test
	public void testName() {
		Assert.assertNull(getComponentNameFromPath(""));
		Assert.assertNull(getComponentNameFromPath("component"));
		Assert.assertNull(getComponentNameFromPath("component/"));
		Assert.assertNull(getComponentNameFromPath("component/1.0.0"));
		Assert.assertNull(getComponentNameFromPath("component/1.0.0/"));
		Assert.assertEquals("component/1.0.0", getComponentNameFromPath("component/1.0.0/nsr"));
		Assert.assertEquals("component/1.0.0", getComponentNameFromPath("component/1.0.0/nsr/Test.js"));
		Assert.assertEquals("component/1.0.0", getComponentNameFromPath("component/1.0.0/nsr/subfolder/Test/"));
	}
}
