/*
 * InstallationManager.java
 *
 * Created on 19 February 2006, 12:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour;

import com.hiatus.envt.IncludeFileLocator;
import com.hiatus.envt.impl.DefaultFileLocator;
import com.hiatus.htl.HTL;
import com.hiatus.text.UText;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Logger;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import org.bloggers4labour.conf.Configuration;
import org.bloggers4labour.feed.auth.AuthenticationManager;
import org.bloggers4labour.index.IndexMgr;
import org.bloggers4labour.installation.InstallationNotFoundException;
import org.bloggers4labour.installation.tasks.InstallationTaskIF;
import org.bloggers4labour.polling.Poller;
import org.bloggers4labour.polling.PollerConfig;
import org.bloggers4labour.polling.PollerIF;
import org.bloggers4labour.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import static org.bloggers4labour.Constants.*;

/**
 *
 * @author andrewre
 */
public class InstallationManager implements InstallationManagerIF
{
	private Map<String,InstallationIF>	m_Map = new LinkedHashMap<String,InstallationIF>();
	private Collection<IncludeFileLocator>	m_FileLocators = new HashSet<IncludeFileLocator>();
	private Context				m_AppContext;	// (AGR) 28 April 2007

	private InstallationStatus		m_Status = InstallationStatus.UNCONFIGURED;

	private static Logger			s_Installations_Logger = Logger.getLogger( InstallationManager.class );

	public final static String		DEFAULT_INSTALL = "b4l";

	/*******************************************************************************
	*******************************************************************************/
	@SuppressWarnings("unchecked")
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

			///////////////////////////////////////////////////////////////////////////////////////  (AGR) 16 August 2008

			XPathExpression			theFileLocatorsExpr = theXPathObj.compile("installations/fileLocators/fileLocator");
			NodeList			theFileLocatorNodes = (NodeList) theFileLocatorsExpr.evaluate( theDocument, XPathConstants.NODESET);

			for ( int k = 0; k < theFileLocatorNodes.getLength(); k++)
			{
				theElement = (Element) theFileLocatorNodes.item(k);

				NamedNodeMap	theAttrs = theElement.getAttributes();

				///////////////////////////////////////////////////////////////////////////////

				DefaultFileLocator	theLocator = new DefaultFileLocator( theAttrs.getNamedItem("id").getTextContent() );

				theLocator.setRootDirectoryPath( theAttrs.getNamedItem("rootDirectoryPath").getTextContent() );
				theLocator.setDirectoryPrefix( theAttrs.getNamedItem("directoryPrefix").getTextContent() );
				theLocator.setDefaultDirectoryName( theAttrs.getNamedItem("defaultDirectoryName").getTextContent() );

				m_FileLocators.add(theLocator);
			}

			///////////////////////////////////////////////////////////////////////////////////////  (AGR) 16 August 2008

			XPathExpression		theHTLPropsExpr = theXPathObj.compile("installations/HTL[1]");
			Node			theHTLPropsNode = (Node) theHTLPropsExpr.evaluate( theDocument, XPathConstants.NODE);

			if ( theHTLPropsNode != null)
			{
				NamedNodeMap	theAttrs = theHTLPropsNode.getAttributes();
				Node		theDesiredLocatorAttr = theAttrs.getNamedItem("fileLocator");

				if ( theDesiredLocatorAttr != null)
				{
					String	theName = theDesiredLocatorAttr.getTextContent();

					for ( IncludeFileLocator eachLocator : m_FileLocators)
					{
						if (eachLocator.getId().equals(theName))
						{
							HTL.registerIncludeFileLocator(eachLocator);
							break;
						}
					}
				}
			}

			///////////////////////////////////////////////////////////////////////////////////////

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
				catch (XPathException e)
				{
					// NOOP
				}
				catch (NumberFormatException e)
				{
					// NOOP
				}

				/////////////////////////////////////////////////////////////////////////////

				try
				{
					Constructor	ctor = Class.forName(theClassStr).getConstructors()[0];

					thePollerIdsMap.put( theId, new PollerConfig( ctor, theId, thePollerFreqMS));
				}
				catch (ClassNotFoundException e)
				{
					s_Installations_Logger.error("Failure when accessing \"" + theClassStr + "\". Will not be able to poll for this Installation.");
				}
			}

			/////////////////////////////////////////////////////////////////////////////////////

			XPathExpression		theInstallsExpr = theXPathObj.compile("installations/install");
			XPathExpression		theDSNExpr = theXPathObj.compile("dataSourceName[1]/text()");	// (AGR) 14 Feb 2007
			XPathExpression		theURLExpr = theXPathObj.compile("jdbc_url[1]/text()");
			XPathExpression		theHMgrExpr = theXPathObj.compile("headlinesMgr[1]");
			XPathExpression		theIdxMgrExpr = theXPathObj.compile("indexManager[1]");
			XPathExpression		theTasksExpr = theXPathObj.compile("task");
//			XPathExpression		theFBGroupIDExpr = theXPathObj.compile("facebook[1]/group_id[1]");
			NodeList		theInstallsNodes = (NodeList) theInstallsExpr.evaluate( theDocument, XPathConstants.NODESET);
			InitialContext		initContext = new InitialContext();	// (AGR) 14 Feb 2007
//			Context			appContext;

			// s_Installations_Logger.info("initContext: " + initContext);

			try
			{
//				appContext = (Context) initContext.lookup("java:comp/env");
				m_AppContext = (Context) initContext.lookup("java:comp/env");	// (AGR) 28 April 2007
			}
			catch (javax.naming.NoInitialContextException e)
			{
				// (AGR) 13 March 2007. Only happens when runnng locally, not at B4L

//				appContext = null;	// (AGR) 13 March 2007. Take the NPE later.
				m_AppContext = null;	// (AGR) 28 April 2007
			}

			// s_Installations_Logger.info("appContext: " + appContext);

			for ( int i = 0; i < theInstallsNodes.getLength(); i++)
			{
				theElement = (Element) theInstallsNodes.item(i);

				///////////////////////////////////////////////////////////////////////////  (AGR) 23 Oct 2009

				String				theImplClassName = XMLUtils.getNodeAttrValue( theElement, "impl");
				Class<? extends InstallationIF>	theImplClass;

				if (UText.isNullOrBlank(theImplClassName))
				{
					theImplClass = Installation.class;
				}
				else
				{
					try
					{
						theImplClass = (Class<? extends InstallationIF>) Class.forName(theImplClassName);
					}
					catch (ClassNotFoundException ex)
					{
						s_Installations_Logger.error("Couldn't create InstallationIF of type '" + theImplClassName + "'");
						continue;
					}
				}

				s_Installations_Logger.info("Impl class = " + theImplClass);

				///////////////////////////////////////////////////////////////////////////

				String		theName = theElement.getAttributes().getNamedItem("name").getTextContent();
				Node		theBundleNameNode = theElement.getAttributes().getNamedItem("resourceBundle");
				Node		theMaxItemAgeMSecsNode = theElement.getAttributes().getNamedItem("maxItemAgeMillis");
				DataSource	theSource;

				try	// (AGR) 14 Feb 2007. Pooling - at last!
				{
					String		theDSNStr = (String) theDSNExpr.evaluate( theElement, XPathConstants.STRING);

					s_Installations_Logger.info("[IMgr] Looking up: " + theDSNStr);

//					theSource = (DataSource) appContext.lookup(theDSNStr);
					theSource = (DataSource) m_AppContext.lookup(theDSNStr);	// (AGR) 28 April 2007

					s_Installations_Logger.info("[IMgr] Obtained: " + theSource);

					// java.sql.Connection	c = theSource.getConnection();
					// s_Installations_Logger.info("c: " + c);
				}
				catch (Exception e)
				{
					String		theURLNodeStr = (String) theURLExpr.evaluate( theElement, XPathConstants.STRING);

					s_Installations_Logger.info("[IMgr] Resorting to use JDBC URL: " + theURLNodeStr);

					theSource = new MysqlDataSource();

					if (UText.isValidString(theURLNodeStr))
					{
						((MysqlDataSource) theSource).setUrl(theURLNodeStr);
					}
				}

				/////////////////////////////////////////////////////////////////////////////  (AGR) 28 October 2006

				String			thePollerIdStr = theElement.getAttributes().getNamedItem("poller").getTextContent();
				String[]		thePollerIds = thePollerIdStr.split(",");
				Collection<PollerIF>	thePollers = new ArrayList<PollerIF>();

				for ( String eachPollerId : thePollerIds)
				{
					String	thePollerId = eachPollerId.trim();

					s_Installations_Logger.debug("thePollerId = " + thePollerId);

					PollerConfig	thePollerConfigToUse = thePollerIdsMap.get(thePollerId);

					s_Installations_Logger.debug("thePollerConfigToUse = " + thePollerConfigToUse);

					try
					{
						Poller	theNewPoller = thePollerConfigToUse.newInstance();

						s_Installations_Logger.debug("Adding theNewPoller = " + theNewPoller);

						thePollers.add(theNewPoller);
					}
					catch (Exception e)
					{
						s_Installations_Logger.error("Poller creation failed", e);
					}
				}

				/////////////////////////////////////////////////////////////////////////////
 
				InstallationIF	theInstall;

				try
				{
					@SuppressWarnings(value = "unchecked")
					Constructor<InstallationIF>	theCtor = (Constructor<InstallationIF>) theImplClass.getConstructor( String.class, String.class, DataSource.class, String.class, long.class, Collection.class);

					theInstall = theCtor.newInstance( theName,
										theBundleNameNode.getTextContent(),
										theSource,
										theElement.getAttributes().getNamedItem("mbean_name").getTextContent(),
										( theMaxItemAgeMSecsNode != null) ? Long.parseLong( theMaxItemAgeMSecsNode.getTextContent() ) : Long.MAX_VALUE,
										thePollers);

					s_Installations_Logger.debug("Created... " + theInstall);
				}
				catch (IllegalAccessException ex)
				{
					s_Installations_Logger.error("", ex);
					continue;
				}
				catch (InstantiationException ex)
				{
					s_Installations_Logger.error("", ex);
					continue;
				}
				catch (InvocationTargetException ex)
				{
					s_Installations_Logger.error("", ex);
					continue;
				}
				catch (NoSuchMethodException ex)
				{
					s_Installations_Logger.error("", ex);
					continue;
				}

				/////////////////////////////////////////////////////////////////////////////  (AGR) 27 October 2008. InstallationTasks...

				NodeList				theTaskNodes = (NodeList) theTasksExpr.evaluate( theElement, XPathConstants.NODESET);
				Collection<InstallationTaskIF>		theTasksColl = new LinkedList<InstallationTaskIF>();

				for ( int eachTaskIdx = 0; eachTaskIdx < theTaskNodes.getLength(); eachTaskIdx++)
				{
					Node	eachTaskNode = theTaskNodes.item(eachTaskIdx);
					String	theClassName = eachTaskNode.getAttributes().getNamedItem("className").getTextContent();
					String	theDelayStr = eachTaskNode.getAttributes().getNamedItem("delayMS").getTextContent();
					String	theFreqStr = eachTaskNode.getAttributes().getNamedItem("frequencyMS").getTextContent();

					try
					{
						@SuppressWarnings("unchecked")
						Constructor<InstallationTaskIF>	theCtor = (Constructor<InstallationTaskIF>) Class.forName(theClassName).getConstructor( InstallationIF.class, Number.class, Number.class);

						theTasksColl.add( theCtor.newInstance( theInstall, Long.valueOf(theDelayStr), Long.valueOf(theFreqStr)) );
					}
					catch (InvocationTargetException e)
					{
						s_Installations_Logger.error("Creating InstallationTasks", e);
					}
					catch (InstantiationException e)
					{
						s_Installations_Logger.error("Creating InstallationTasks", e);
					}
					catch (IllegalAccessException e)
					{
						s_Installations_Logger.error("Creating InstallationTasks", e);
					}
					catch (ClassNotFoundException e)
					{
						s_Installations_Logger.error("Creating InstallationTasks", e);
					}
					catch (NoSuchMethodException e)
					{
						s_Installations_Logger.error("Creating InstallationTasks", e);
					}
					catch (IllegalArgumentException e)
					{
						s_Installations_Logger.error("Creating InstallationTasks", e);
					}
				}

				theInstall.setTasks(theTasksColl);

				/////////////////////////////////////////////////////////////////////////////

				Element		theHeadMgrElem = (Element) theHMgrExpr.evaluate( theElement, XPathConstants.NODE);

				theInstall.setHeadlinesMgr( new HeadlinesMgr( theHeadMgrElem, theInstall) );

				/////////////////////////////////////////////////////////////////////////////

				Element		theIndexMgrElem = (Element) theIdxMgrExpr.evaluate( theElement, XPathConstants.NODE);

				if ( theIndexMgrElem != null)
				{
					theInstall.setIndexMgr(  new IndexMgr(theInstall) );
				}

				/////////////////////////////////////////////////////////////////////////////

				theInstall.complete();

				AuthenticationManager.getInstance();	// Yuk! Move me!!!

				/////////////////////////////////////////////////////////////////////////////

				register(theInstall);
			}

			m_Status = InstallationStatus.STARTED;
		}
		catch (XPathExpressionException e)
		{
			s_Installations_Logger.error("InstallationManager()", e);
		}
		catch (NamingException e)		// for InitialContext stuff
		{
			s_Installations_Logger.error("InstallationManager()", e);
		}
		catch (ParserConfigurationException e)	// for DocumentBuilderFactory
		{
			s_Installations_Logger.error("InstallationManager()", e);
		}
		catch (SAXException e)			// for DocumentBuilder.parse
		{
			s_Installations_Logger.error("InstallationManager()", e);
		}
		catch (IOException e)			// for DocumentBuilder.parse
		{
			s_Installations_Logger.error("InstallationManager()", e);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public static InstallationManagerIF getInstance()
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
	public synchronized void register( InstallationIF inInstall)
	{
		m_Map.put( inInstall.getName(), inInstall);
	}

	/*******************************************************************************
	*******************************************************************************/
	public static InstallationIF getDefaultInstallation()
	{
		return getInstallation( DEFAULT_INSTALL );
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
		InstallationIF	theInstall = m_Map.get(inName);

		if ( theInstall != null)
		{
			return theInstall;
		}

		throw new InstallationNotFoundException(inName);
	}

	/*******************************************************************************
	*******************************************************************************/
	public void restart()
	{
		for ( InstallationIF eachInstall : m_Map.values())
		{
			eachInstall.restart();
		}

		m_Status = InstallationStatus.STARTED;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void startIfStopped()
	{
		if ( m_Status == InstallationStatus.STOPPED)
		{
			restart();
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

		m_Status = InstallationStatus.STOPPED;
	}

	/*******************************************************************************
	*******************************************************************************/
	public InstallationStatus getStatus()
	{
		return m_Status;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Iterable<IncludeFileLocator> getFileLocators()
	{
		return new Iterable<IncludeFileLocator>()
		{
			public Iterator<IncludeFileLocator> iterator()
			{
				return m_FileLocators.iterator();
			}
		};
	}

	/*******************************************************************************
		(AGR) 28 April 2007
	*******************************************************************************/
	public DataSource lookupDataSource( String inName) throws NamingException
	{
		return ( m_AppContext != null) ? (DataSource) m_AppContext.lookup(inName) : null;
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
