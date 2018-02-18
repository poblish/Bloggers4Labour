/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour;

import org.apache.log4j.Logger;
import org.junit.*;

/**
 *
 * @author andrewregan
 */
public abstract class AbstractB4LTest
{
	protected Logger	m_Logger = Logger.getLogger( getClass() );
	protected Launcher	m_Launcher;

	/*******************************************************************************
	*******************************************************************************/
	@Before public void setUp()
	{
		initTest();

		m_Launcher = new Launcher(m_Logger);
		m_Launcher.start();
	}

	/*******************************************************************************
	*******************************************************************************/
	protected void initTest()
	{
		// NOOP
	}

	/*******************************************************************************
	*******************************************************************************/
	@After public void tearDown()
	{
		if ( m_Launcher != null)
		{
			m_Launcher.stop();
		}
	}
}