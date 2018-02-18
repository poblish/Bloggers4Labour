/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour;

import org.junit.Test;
import org.junit.Assert;

/**
 *
 * @author andrewregan
 */
public class HelloWorldTest extends AbstractB4LTest
{
	/*******************************************************************************
	*******************************************************************************/
	@Test public void testHello()
	{
		InstallationIF	theInstall = InstallationManager.getDefaultInstallation();
		Assert.assertNotNull(theInstall);

		System.out.println("Hello World!");
	}
}
