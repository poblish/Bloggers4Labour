/*
 * Configuration.java
 *
 * Created on July 9, 2005, 12:55 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.conf;

import java.io.File;
import org.apache.log4j.Logger;

/**
 *
 * @author andrewre
 */
public class Configuration
{
	private File		m_Dir;
	private boolean		m_DirSet = false;

	private static Logger	s_Logger = Logger.getLogger( Configuration.class );

	/*******************************************************************************
	*******************************************************************************/
	private Configuration()
	{
		m_Dir = new File( System.getProperty("user.dir") );
		s_Logger.info("Config: dir=\"" + m_Dir + "\"");
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setDirectory( String inDir)
	{
		m_Dir = new File(inDir);
		s_Logger.info("Config: dir=\"" + m_Dir + "\"");
		m_DirSet = true;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setDirectoryIfNotSet( String inDir)
	{
		if (!m_DirSet)
		{
			setDirectory(inDir);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public static Configuration getInstance()
	{
		return LazyHolder.s_Config;
	}

	/*******************************************************************************
	*******************************************************************************/
	public File findFile( String inName)
	{
		return new File( m_Dir, inName);
	}

	/*******************************************************************************
	*******************************************************************************/
	private static class LazyHolder
	{
		private static Configuration	s_Config = new Configuration();
	}
}
