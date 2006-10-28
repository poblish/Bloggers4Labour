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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Logger;
import org.bloggers4labour.conf.Configuration;
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
			XPathExpression		theInstallsExpr = theXPathObj.compile("installations/install");
			XPathExpression		theURLExpr = theXPathObj.compile("jdbc_url[1]/text()");
			XPathExpression		thePollerFreqExpr = theXPathObj.compile("poller_frequency_ms[1]/text()");
			XPathExpression		theHMgrExpr = theXPathObj.compile("headlinesMgr[1]");
			NodeList		theInstallsNodes = (NodeList) theInstallsExpr.evaluate( theDocument, XPathConstants.NODESET);

			for ( int i = 0; i < theInstallsNodes.getLength(); i++)
			{
				Element		theElement = (Element) theInstallsNodes.item(i);
				String		theURLNodeStr = (String) theURLExpr.evaluate( theElement, XPathConstants.STRING);
				String		theName = theElement.getAttributes().getNamedItem("name").getTextContent();
				Node		theBundleNameNode = theElement.getAttributes().getNamedItem("bundle_name");
				MysqlDataSource	theSource = new MysqlDataSource();

				if (UText.isValidString(theURLNodeStr))
				{
					theSource.setUrl(theURLNodeStr);
				}

				/////////////////////////////////////////////////////////////////////////////  (AGR) 26 October 2006. Poller freq [ms]

				long	thePollerFreqMS = 4 * ONE_MINUTE_MSECS;

				try
				{
					thePollerFreqMS = Long.parseLong( (String) thePollerFreqExpr.evaluate( theElement, XPathConstants.STRING) );
				}
				catch (Exception e)
				{
					;
				}

				/////////////////////////////////////////////////////////////////////////////

				Element		theHeadMgrElem = (Element) theHMgrExpr.evaluate( theElement, XPathConstants.NODE);

				Installation	theInstall = new Installation( theName,
										( theBundleNameNode != null) ? theBundleNameNode.getTextContent() : theName,
										theSource,
										theElement.getAttributes().getNamedItem("mbean_name").getTextContent(),
										thePollerFreqMS);

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
