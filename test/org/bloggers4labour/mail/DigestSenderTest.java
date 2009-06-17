/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.mail;

import com.hiatus.envt.impl.DefaultFileLocator;
import com.hiatus.htl.HTL;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import org.bloggers4labour.AbstractB4LTest;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.InstallationManager;
import org.bloggers4labour.feed.FeedListIF;
import org.junit.Assert;
import org.junit.Test;
import static org.bloggers4labour.Constants.*;

/**
 *
 * @author andrewregan
 */
public class DigestSenderTest extends AbstractB4LTest
{
	private byte[]	m_Lock = new byte[0];

	/*******************************************************************************
	*******************************************************************************/
	@Override protected void initTest()
	{
		// Need to do this prior to settng up the Launcher

		Properties theProps = System.getProperties();

		theProps.put("mail.smtp.host", "mail.bloggers4labour.org");
//		theProps.put("mail.debug", "true");

		////////////////////////////////////////////////////////////////

		DefaultFileLocator	theLocator = new DefaultFileLocator("test");

		theLocator.setRootDirectoryPath("/Users/andrewregan/www/htdocs/bloggers4labour/htl/");
		theLocator.setDirectoryPrefix("locales/");
		theLocator.setDefaultDirectoryName("default");

		HTL.registerIncludeFileLocator(theLocator);
	}

	/*******************************************************************************
	*******************************************************************************/
	@Test public void sendEmail() throws InterruptedException
	{
		synchronized (m_Lock)
		{
			new ObserverThread().start();

			// m_Logger.info("--> Waiting JUnit Test Thread at " + new java.util.Date() + " (" + Thread.currentThread() + ")");

			m_Lock.wait( 30 * ONE_MINUTE_MSECS);

			// m_Logger.info("--> JUnit Test Thread AWAKE at " + new java.util.Date() + " (" + Thread.currentThread() + ")");
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private class ObserverThread extends Thread
	{
		/*******************************************************************************
		*******************************************************************************/
		@Override public void run()
		{
		//	InstallationManager theIMgr = InstallationManager.getInstance();

		//	for (String eachInstallName : theIMgr.getInstallationNames())
			{
				InstallationIF theInstall = InstallationManager.getDefaultInstallation();
		//		InstallationIF theInstall = theIMgr.get(eachInstallName);
				Assert.assertNotNull(theInstall);

				FeedListIF	theFL = theInstall.getFeedList();
				Assert.assertNotNull(theFL);

				theInstall.getDigestSender().test(theFL, new Observer()
				{
					public void update(Observable o, Object arg)
					{
						completed();

						synchronized (m_Lock)
						{
							// m_Logger.info("--> Asking test to complete at " + new java.util.Date());

							m_Lock.notify();
						}
					}
				});
			}
		}

		/*******************************************************************************
		*******************************************************************************/
		public void completed()
		{
		}
	}
}