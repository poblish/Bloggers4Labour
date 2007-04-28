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
import javax.servlet.http.*;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationManager;
import org.bloggers4labour.MainServlet;

/**
 *
 * @author andrewre
 */
public class APIHandlerServlet extends HttpServlet
{
	protected DataSource		m_DataSource;
	protected static Logger		s_Servlet_Logger = Logger.getLogger("Main");

	/*******************************************************************************
	*******************************************************************************/
	public APIHandlerServlet()
	{
	}

	/*******************************************************************************
	*******************************************************************************/
	public void init( ServletConfig inConfig)
	{
		try
		{
			super.init(inConfig);

			try
			{
				m_DataSource = InstallationManager.getInstance().lookupDataSource("jdbc/b4lTestDataSource");
				s_Servlet_Logger.info("APIHandlerServlet: loaded " + m_DataSource);
			}
			catch (NamingException e)
			{
				s_Servlet_Logger.error("NamingException: " + e);
			}
		}
		catch (Exception ex)
		{
			s_Servlet_Logger.error( "init error", ex);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public void doXXXXX( HttpServletRequest inRequest, HttpServletResponse inResponse)
	{
		CharSequence	theOutputBuffer = null;
		boolean		keepAlive = true;

		try
		{
/*			theOutputBuffer = MainServlet.handleRecommendations( inRequest, inResponse).toString();

			s_Servlet_Logger.info("req: " + inRequest.getPathInfo);
			
			java.util.Enumeration	en = inRequest.getHeaderNames();
			while (en.hasMoreElements())
			{
				String	eachName = (String) en.nextElement();
				s_Servlet_Logger.info("Header \"" + eachName + "\"");
				
				java.util.Enumeration	x = (java.util.Enumeration) inRequest.getHeaders(eachName);

				while (x.hasMoreElements())
				{
					s_Servlet_Logger.info(" => " + x.nextElement());
				}
			}

			s_Servlet_Logger.info("getPathInfo: " + inRequest.getPathInfo());
			s_Servlet_Logger.info("getQueryString: " + inRequest.getQueryString());
			s_Servlet_Logger.info("getRequestURI: " + inRequest.getRequestURI());
			s_Servlet_Logger.info("getRequestURL: " + inRequest.getRequestURL());
*/		}
		catch (Exception e)
		{
			s_Servlet_Logger.error("WS.doGet", e);
		}
		finally
		{
			MainServlet.myOutputBuffer( inResponse, theOutputBuffer, keepAlive);
			MainServlet.Finish(inResponse);
		}
	}
}