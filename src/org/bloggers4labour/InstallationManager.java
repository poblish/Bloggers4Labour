/*
 * InstallationManager.java
 *
 * Created on 19 February 2006, 12:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour;

import com.hiatus.UText;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Logger;
import org.bloggers4labour.conf.Configuration;
import org.bloggers4labour.polling.Poller;
import org.bloggers4labour.polling.PollerConfig;
import org.w3c.dom.*;
import static org.bloggers4labour.Constants.*;

/**
 *
 * @author andrewre
 */
public class InstallationManager
{
	private Map<String,InstallationIF>	m_Map = new LinkedHashMap<String,InstallationIF>();

	private static Logger			s_Installations_Logger = Logger.getLogger("Main");

	public final static String		DEFAULT_INSTALL = "b4l";

	/*******************************************************************************
	*******************************************************************************/
	private InstallationManager()
	{
		XPathFactory		theFactory = XPathFactory.newInstance();
		XPath			theXPathObj = theFactory.newXPath();
		DocumentBuilderFactory	docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder		docBuilder;

		try
		{
			docBuilder = docFactory.newDocumentBuilder();

			Configuration		theConf = Configuration.getInstance();
			Document		theDocument = docBuilder.parse( theConf.findFile("installations.xml") );
			Element			theElement;

			XPathExpression			thePollersExpr = theXPathObj.compile("installations/pollers/poller");
			XPathExpression			thePollerClassExpr = theXPathObj.compile("class[1]/text()");
			XPathExpression			thePollerFreqExpr = theXPathObj.compile("frequency_ms[1]/text()");
			NodeList			thePollersNodes = (NodeList) thePollersExpr.evaluate( theDocument, XPathConstants.NODESET);
			Map<String,PollerConfig>	thePollerIdsMap = new HashMap<String,PollerConfig>();

			for ( int k = 0; k < thePollersNodes.getLength(); k++)
			{
				theElement = (Element) thePollersNodes.item(k);

				String		theId = theElement.getAttributes().getNamedItem("id").getTextContent();
				String		theClassStr = (String) thePollerClassExpr.evaluate( theElement, XPathConstants.STRING);

				long	thePollerFreqMS = 5 * ONE_MINUTE_MSECS;

				try
				{
					thePollerFreqMS = Long.parseLong( (String) thePollerFreqExpr.evaluate( theElement, XPathConstants.STRING) );
				}
				catch (Exception e)
				{
					;
				}

				/////////////////////////////////////////////////////////////////////////////

				try
				{
					Constructor	ctor = Class.forName(theClassStr).getConstructors()[0];

					thePollerIdsMap.put( theId, new PollerConfig( ctor, thePollerFreqMS));
				}
				catch (Exception e)
				{
					s_Installations_Logger.error("Failure when accessing \"" + theClassStr + "\". Will not be able to poll for this Installation.");
				}
			}

			/////////////////////////////////////////////////////////////////////////////////////

			XPathExpression		theInstallsExpr = theXPathObj.compile("installations/install");
			XPathExpression		theDSNExpr = theXPathObj.compile("dataSourceName[1]/text()");	// (AGR) 14 Feb 2007
			XPathExpression		theURLExpr = theXPathObj.compile("jdbc_url[1]/text()");
			XPathExpression		theHMgrExpr = theXPathObj.compile("headlinesMgr[1]");
			NodeList		theInstallsNodes = (NodeList) theInstallsExpr.evaluate( theDocument, XPathConstants.NODESET);
			InitialContext		initContext = new InitialContext();	// (AGR) 14 Feb 2007

			// s_Installations_Logger.info("initContext: " + initContext);

			Context			appContext = (Context) initContext.lookup("java:comp/env");

			// s_Installations_Logger.info("appContext: " + appContext);

			for ( int i = 0; i < theInstallsNodes.getLength(); i++)
			{
				theElement = (Element) theInstallsNodes.item(i);

				String		theName = theElement.getAttributes().getNamedItem("name").getTextContent();
				Node		theBundleNameNode = theElement.getAttributes().getNamedItem("bundle_name");
				DataSource	theSource;

				try	// (AGR) 14 Feb 2007. Pooling - at last!
				{
					String		theDSNStr = (String) theDSNExpr.evaluate( theElement, XPathConstants.STRING);

					s_Installations_Logger.info("[IMgr] Looking up: " + theDSNStr);

					theSource = (DataSource) appContext.lookup(theDSNStr);

					s_Installations_Logger.info("[IMgr] Obtained: " + theSource);

					// java.sql.Connection	c = theSource.getConnection();
					// s_Installations_Logger.info("c: " + c);
				}
				catch (Exception e)
				{
					s_Installations_Logger.info("Resorting to use JDBC URL: " + e);

					String		theURLNodeStr = (String) theURLExpr.evaluate( theElement, XPathConstants.STRING);

					theSource = new MysqlDataSource();

					if (UText.isValidString(theURLNodeStr))
					{
						((MysqlDataSource) theSource).setUrl(theURLNodeStr);
					}
				}

				/////////////////////////////////////////////////////////////////////////////  (AGR) 28 October 2006

				String		thePollerId = theElement.getAttributes().getNamedItem("poller").getTextContent();
				PollerConfig	thePollerConfigToUse = thePollerIdsMap.get(thePollerId);
				Poller		theNewPoller;

				try
				{
					theNewPoller = thePollerConfigToUse.newInstance();
				}
				catch (Exception e)
				{
					theNewPoller = null;
				}

				/////////////////////////////////////////////////////////////////////////////

				Element		theHeadMgrElem = (Element) theHMgrExpr.evaluate( theElement, XPathConstants.NODE);

				Installation	theInstall = new Installation( theName,
										( theBundleNameNode != null) ? theBundleNameNode.getTextContent() : theName,
										theSource,
										theElement.getAttributes().getNamedItem("mbean_name").getTextContent(),
										theNewPoller);

				theInstall.setHeadlinesMgr( new HeadlinesMgr( theHeadMgrElem, theInstall) );
				theInstall.complete();

				/////////////////////////////////////////////////////////////////////////////

				register(theInstall);
			}
		}
		catch (Exception e) // (XPathException e)
		{
			s_Installations_Logger.error("InstallationManager()", e);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public static InstallationManager getInstance()
	{
		return LazyHolder.s_Mgr;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Set<String> getInstallationNames()
	{
		return m_Map.keySet();
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized void register( Installation inInstall)
	{
		m_Map.put( inInstall.getName(), inInstall);
	}

	/*******************************************************************************
	*******************************************************************************/
	public static InstallationIF getDefaultInstallation()
	{
		return getInstance().getInstallation( DEFAULT_INSTALL );
	}

	/*******************************************************************************
	*******************************************************************************/
	public static InstallationIF getInstallation( String inName)
	{
		return getInstance().get(inName);
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized InstallationIF get( String inName)
	{
		return m_Map.get(inName);
	}

	/*******************************************************************************
	*******************************************************************************/
	public void restart()
	{
		for ( InstallationIF eachInstall : m_Map.values())
		{
			eachInstall.restart();
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public void stop()
	{
		for ( InstallationIF eachInstall : m_Map.values())
		{
			eachInstall.stop();
		}
	}

	/*******************************************************************************
		(AGR) 5 June 2005. See:
		    <http://www-106.ibm.com/developerworks/java/library/j-jtp03304/>
	*******************************************************************************/
	private static class LazyHolder
	{
		private static InstallationManager	s_Mgr = new InstallationManager();
	}
}
