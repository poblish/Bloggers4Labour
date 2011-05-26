/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.opml;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.site.SiteIF;

/**
 *
 * @author andrewregan
 */
public class DefaultOPMLHandler implements OPMLHandlerIF
{
	private OPMLGeneratorIF		m_OPMLGenerator;
	private String			m_OPML_OutputStr;

	private InstallationIF		m_Install;

	private static Logger		s_Logger = Logger.getLogger( DefaultOPMLHandler.class );

	/*******************************************************************************
	*******************************************************************************/
	public DefaultOPMLHandler( final InstallationIF inInstall)
	{
		m_Install = inInstall;
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized void generate( final SiteIF[] ioSitesArray)
	{
		if ( m_OPMLGenerator == null)
		{
			m_OPMLGenerator = new OPMLGenerator();


			s_Logger.info( m_Install.getLogPrefix() + "created " + m_OPMLGenerator);
		}

		////////////////////////////////////////////////////////////////

		try
		{
			m_OPML_OutputStr = m_OPMLGenerator.generate(ioSitesArray);

			// s_Logger.info( m_Install.getLogPrefix() + "OPML_OutputStr = " + m_OPML_OutputStr);
		}
		catch (IOException e)
		{
			s_Logger.info( m_Install.getLogPrefix() + "OPML serialization failed", e);
		}

		// s_Logger.info( m_Install.getLogPrefix() + "generateOPML() DONE");
	}

	/*******************************************************************************
	*******************************************************************************/
	public InstallationIF getInstallation()
	{
		return m_Install;
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized String getOPMLString()
	{
		return m_OPML_OutputStr;
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized void clear()
	{
		m_OPML_OutputStr = null;
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized void disconnect()
	{
		if ( m_OPMLGenerator != null)
		{
			m_OPMLGenerator = null;
		}

		m_OPML_OutputStr = null;
	}
}