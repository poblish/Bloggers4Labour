/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.opml.input;

import com.hiatus.sql.USQL_Utils;
import com.hiatus.text.UText;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.api.SubmissionResult;
import org.bloggers4labour.api.SubmitBlogHandlerServlet;
import org.bloggers4labour.installation.tasks.InstallationTaskIF;
import org.bloggers4labour.req.MapParameters;
import org.bloggers4labour.sql.DataSourceConnection;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author andrewregan
 */
public class OPMLMonitoringTask implements InstallationTaskIF
{
	private InstallationIF	m_Install;
	private long		m_DelayMS;
	private long		m_FrequencyMS;

	private static Logger	s_Logger = Logger.getLogger( OPMLMonitoringTask.class );

	/*******************************************************************************
	*******************************************************************************/
	public OPMLMonitoringTask( final InstallationIF inInstall, final Number inDelayMS, final Number inFreqMS)
	{
		m_Install = inInstall;
		m_DelayMS = inDelayMS.longValue();
		m_FrequencyMS = inFreqMS.longValue();
	}

	/*******************************************************************************
	*******************************************************************************/
	public void run()
	{
		DataSource		theDS = m_Install.getDataSource();
		DataSourceConnection	theConnectionObject = null;

		try
		{
			theConnectionObject = new DataSourceConnection(theDS);
			if (theConnectionObject.Connect())
			{
				PreparedStatement	theScreeningStatement = null;

				try
				{
					theScreeningStatement = theConnectionObject.prepareStatement("SELECT 1 FROM site WHERE url=? AND feed_url=?");

					_handleEntries( theDS, theScreeningStatement);
				}
				catch (SQLException e)
				{
					s_Logger.error( "", e);
				}
				finally
				{
					USQL_Utils.closeStatementCatch(theScreeningStatement);
				}
			}
			else
			{
				// s_Servlet_Logger.warn("Cannot connect!");
			}
		}
		catch (RuntimeException e)
		{
			s_Logger.error( "", e);
		}
		finally
		{
			if ( theConnectionObject != null)
			{
				theConnectionObject.CloseDown();
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private void _handleEntries( final DataSource inDS, final PreparedStatement inStatement) throws SQLException
	{
		DocumentBuilder		theBuilder;

		try
		{
			theBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			URLConnection		theConn = new URL("http://www.brightonbloggers.com/brighton.opml").openConnection();
			Document		theDocument = theBuilder.parse( theConn.getInputStream() );

			XPath			theXPathObj = XPathFactory.newInstance().newXPath();
			XPathExpression		theExpr = theXPathObj.compile("opml/body/outline/outline");
			NodeList		theNodes = (NodeList) theExpr.evaluate( theDocument, XPathConstants.NODESET);
			Map<String,String>	theParams = new HashMap<String,String>();

			theParams.put( "location", Integer.toString( 22 ));
			theParams.put( "status", Integer.toString( 9 ));
			theParams.put( "site_category", Integer.toString( 1 ));

			for ( int k = 0; k < theNodes.getLength(); k++)
			{
				NamedNodeMap	theAttrs = theNodes.item(k).getAttributes();
				String		theBlogName = theAttrs.getNamedItem("title").getTextContent();

				if (UText.isNullOrBlank(theBlogName))
				{
					continue;
				}

				/////////////////////////////////////////////////////////////////////////////////////

				String		theBlogURL = theAttrs.getNamedItem("htmlUrl").getTextContent();

				if (theBlogURL.endsWith("/index.html"))
				{
					theBlogURL = theBlogURL.substring( 0, theBlogURL.length() - 11);
				}

				if (theBlogURL.endsWith("/index.jsp"))
				{
					theBlogURL = theBlogURL.substring( 0, theBlogURL.length() - 10);
				}

				if (theBlogURL.endsWith("/"))
				{
					theBlogURL = theBlogURL.substring( 0, theBlogURL.length() - 1);
				}

				/////////////////////////////////////////////////////////////////////////////////////

				OutlineEntry	theEntry = new OutlineEntry( theBlogName, theBlogURL, theAttrs.getNamedItem("xmlUrl").getTextContent());

				inStatement.setString( 1, theEntry.getBlogURL());
				inStatement.setString( 2, theEntry.getFeedURL());

				ResultSet	theRS = inStatement.executeQuery();

				if (!theRS.next())
				{
					theParams.put( "blog_name", theEntry.getBlogName());
					theParams.put( "blog_url", theEntry.getBlogURL());
					theParams.put( "feed_url", theEntry.getFeedURL());

					SubmissionResult	theResult = SubmitBlogHandlerServlet.handleSubmission( inDS, s_Logger, new MapParameters(theParams));

					s_Logger.info( "Submission results for: " + theEntry + " : " + theResult.getCode());
				}
			}
		}
		catch (XPathExpressionException ex)
		{
			s_Logger.error( "", ex);
		}
		catch (ParserConfigurationException ex)
		{
			s_Logger.error( "", ex);
		}
		catch (FileNotFoundException ex)
		{
			s_Logger.error( "", ex);
		}
		catch (SAXException ex)
		{
			s_Logger.error( "", ex);
		}
		catch (IOException ex)
		{
			s_Logger.error( "", ex);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public InstallationIF getInstallation()
	{
		return m_Install;
	}

	/*******************************************************************************
	*******************************************************************************/
	public long getDelayMS()
	{
		return m_DelayMS;
	}

	/*******************************************************************************
	*******************************************************************************/
	public long getFrequencyMS()
	{
		return m_FrequencyMS;
	}
}