/*
 * APIHandlerServlet.java
 *
 * Created on 27 April 2007, 22:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.api;

import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationManager;

/**
 *
 * @author andrewre
 */
public class APIHandlerServlet extends HttpServlet
{
	protected transient DataSource		m_DataSource;

	protected final Logger			m_Logger = Logger.getLogger( getClass() );

	private static final long		serialVersionUID = 1L;

	/*******************************************************************************
	*******************************************************************************/
	@Override public void init( ServletConfig inConfig)
	{
		try
		{
			super.init(inConfig);

			try
			{
//				m_DataSource = InstallationManager.getInstance().lookupDataSource("jdbc/b4lTestDataSource");
				m_DataSource = InstallationManager.getInstance().lookupDataSource("jdbc/b4lDataSource");
				m_Logger.info("Loaded " + m_DataSource);
			}
			catch (NamingException e)
			{
				m_Logger.error("NamingException", e);
			}
		}
		catch (Exception ex)
		{
			m_Logger.error( "init error", ex);
		}
	}
}