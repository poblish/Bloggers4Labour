/*
 * HeadlinesMgr.java
 *
 * Created on May 21, 2005, 6:49 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour;

import com.hiatus.UText;
import com.sun.org.apache.xpath.internal.XPathAPI;
import de.nava.informa.core.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Logger;
import org.bloggers4labour.conf.Configuration;
import org.bloggers4labour.feed.FeedList;
import org.bloggers4labour.jmx.Stats;
import org.bloggers4labour.headlines.*;
import org.bloggers4labour.xml.XMLUtils;
import org.w3c.dom.*;
import org.w3c.dom.traversal.NodeIterator;
import static org.bloggers4labour.Constants.*;

/**
 *
 * @author andrewre
 */
public class HeadlinesMgr
{
	private Headlines		m_24Hour_Headlines;
	private Headlines		m_MainRSSFeed_Headlines;
	private Headlines		m_EmailPosts_Headlines;
	private Headlines		m_IndexablePosts_Headlines;
	private Headlines		m_RecentPosts_Headlines;
	private Headlines		m_Comments_Headlines;			// (AGR) 29 Nov 2005

	private List<Headlines>		m_HeadlinesList = new CopyOnWriteArrayList<Headlines>();  // (AGR) 29 May 2005. Was ArrayList

	private static Logger		s_HeadlinesMgr_Logger = Logger.getLogger("Main");

	/*******************************************************************************
	*******************************************************************************
	public HeadlinesMgr( final Installation inInstallation)
	{
		this( null, inInstallation);
	}/

	/*******************************************************************************
	*******************************************************************************/
	public HeadlinesMgr( final Element docElem, final Installation inInstallation)
	{
/*		Configuration		theConf = Configuration.getInstance();
		DocumentBuilderFactory	docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder		docBuilder;
*/
		try
		{
/*			docBuilder = docFactory.newDocumentBuilder();
			Document	theHDoc = docBuilder.parse( theConf.findFile("headlines.xml") );
			Element		docElem = theHDoc.getDocumentElement();
*/
			String		rss_feed_ID = XMLUtils.getIDValue( docElem, "rss_feed");
			String		email_ID = XMLUtils.getIDValue( docElem, "email");
			String		index_ID = XMLUtils.getIDValue( docElem, "index");
			String		recent_ID = XMLUtils.getIDValue( docElem, "recent");
			String		comments_ID = XMLUtils.getIDValue( docElem, "comments");    // (AGR) 29 Nov 2005

			NodeList	headsNodes = docElem.getElementsByTagName("headlines");

			if ( headsNodes != null)
			{
				XPathFactory		theFactory = XPathFactory.newInstance();
				XPath			theXPathObj = theFactory.newXPath();
				XPathExpression		theCatFiltersExpr = theXPathObj.compile("filter/creatorStatus");


				for ( int i = 0; i < headsNodes.getLength(); i++)
				{
					Element		e = (Element) headsNodes.item(i);
					String		ourIDStr = XMLUtils.getNodeIDValue(e);
					NodeList	nl = e.getChildNodes();
					String		theName = null;
					String		theDescription = null;
					String		theAllowedItems = null;
					long		theMinValue = -1;
					long		theMaxValue = -1;
					boolean		gotError = false;
					boolean		wantComments = false;
					boolean		wantPosts = false;

					for ( int j = 0; j < nl.getLength(); j++)
					{
						Node	n = nl.item(j);

						if (n.getNodeName().equals("name"))
						{
							theName = n.getTextContent();
						}
						else if (n.getNodeName().equals("description"))		// (AGR) 25 March 2006
						{
							theDescription = n.getTextContent();
						}
						else if (n.getNodeName().equals("items"))		// (AGR) 29 Nov 2005
						{
							theAllowedItems = n.getTextContent();
							if (UText.isNullOrBlank(theAllowedItems))
							{
								gotError = true;
								break;
							}

							////////////////////////  Look for a usable value...

							if (theAllowedItems.equalsIgnoreCase("POSTS"))
							{
								wantPosts = true;
							}
							else if (theAllowedItems.equalsIgnoreCase("COMMENTS"))
							{
								wantComments = true;
							}
							else
							{
								gotError = true;
								break;
							}
						}
						else if (n.getNodeName().equals("minAgeMillis"))
						{
							theMinValue = Long.parseLong( n.getTextContent() );
						}
						else if (n.getNodeName().equals("maxAgeMillis"))
						{
							theMaxValue = Long.parseLong( n.getTextContent() );
							if ( theMaxValue <= 0L)
							{
								gotError = true;
								break;
							}
						}
					}

					////////////////////////////////////////////////////////////////

					if ( gotError || theMinValue >= theMaxValue)
					{
						continue;
					}
					
					////////////////////////////////////////////////////////////////

					Headlines	h = new Headlines( inInstallation, theName, theDescription, theMinValue, theMaxValue);

					h.setAllowPosts(wantPosts);
					h.setAllowComments(wantComments);

					////////////////////////////////////////////////////////////////  (AGR) 24 March 2006

					NodeList	tempNodeList = (NodeList) theCatFiltersExpr.evaluate( e, XPathConstants.NODESET);
					Element		tempElem;

					if ( tempNodeList != null && tempNodeList.getLength() >= 1)
					{
						List<Number>	ll = new ArrayList<Number>();

						for ( int jj = 0; jj < tempNodeList.getLength(); jj++)
						{
							ll.add( new Integer( tempNodeList.item(jj).getTextContent() ) );
						}

						h.setFilterCreatorStatuses(ll);

						ll = null;
					}

					////////////////////////////////////////////////////////////////

					NodeIterator	ni = XPathAPI.selectNodeIterator( e, "handlers/class");
					Node		handlerNode;
					String		theClassName;

					while (( handlerNode = ni.nextNode()) != null)
					{
						try
						{
							theClassName = handlerNode.getFirstChild().getNodeValue();

							Class	clazz = Class.forName(theClassName);

							h.addHandler((Handler) clazz.newInstance());
						}
						catch (Exception e2)
						{
							s_HeadlinesMgr_Logger.error( "handlers...", e2);
						}
					}

					////////////////////////////////////////////////////////////////

					if ( ourIDStr != null)
					{
						if ( ourIDStr.equals(rss_feed_ID) && m_MainRSSFeed_Headlines == null)
						{
							m_MainRSSFeed_Headlines = h;
						}

						if ( ourIDStr.equals(email_ID) && m_EmailPosts_Headlines == null)
						{
							m_EmailPosts_Headlines = h;
						}

						if ( ourIDStr.equals(index_ID) && m_IndexablePosts_Headlines == null)
						{
							m_IndexablePosts_Headlines = h;
						}

						if ( ourIDStr.equals(recent_ID) && m_RecentPosts_Headlines == null)
						{
							m_RecentPosts_Headlines = h;
						}

						if ( ourIDStr.equals(comments_ID) && m_Comments_Headlines == null)	// (AGR) 29 Nov 2005
						{
							m_Comments_Headlines = h;
						}
					}

					////////////////////////////////////////////////////////////////

					if ( theMinValue <= 0 && theMaxValue == ONE_DAY_MSECS && m_24Hour_Headlines == null)
					{
						m_24Hour_Headlines = h;
					}

					////////////////////////////////////////////////////////////////

					m_HeadlinesList.add(h);
				}
			}

		}
		catch (Exception e)
		{
			s_HeadlinesMgr_Logger.error( "setUpDefaults()", e);
		}

		////////////////////////////////////////////////////////////////

		inInstallation.getFeedList().addObserver( new PublishSnapshotEvent( inInstallation.getManagement().getStats() ) );
	}

	/*******************************************************************************
	*******************************************************************************/
	public int getHeadlinesCount()
	{
		return m_HeadlinesList.size();
	}

	/*******************************************************************************
	*******************************************************************************/
	public Headlines getMainRSSFeedInstance()
	{
		return m_MainRSSFeed_Headlines;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Headlines getRecentPostsInstance()
	{
		return m_RecentPosts_Headlines;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Headlines getCommentsInstance()
	{
		return m_Comments_Headlines;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Headlines getEmailPostsInstance()
	{
		return m_EmailPosts_Headlines;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Headlines getIndexablePostsInstance()
	{
		return m_IndexablePosts_Headlines;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Headlines get24HourInstance()
	{
		return m_24Hour_Headlines;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void removeFor( ChannelIF inChannel)
	{
		for ( Headlines h : m_HeadlinesList)
		{
			h.removeFor(inChannel);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public List<Headlines> getHeadlinesList()
	{
		return m_HeadlinesList;
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized void shutdown()
	{
		for ( Headlines h : m_HeadlinesList)
		{
			h.shutdown();
		}

		m_HeadlinesList.clear();
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized Headlines findHeadlines( String inName)
	{
		for ( Headlines h : m_HeadlinesList)
		{
			if (h.getName().equals(inName))
			{
				return h;
			}
		}

		return null;
	}

	/*******************************************************************************
		(AGR) 19 April 2005. Publish Headlines snapshot in FeedList
		(AGR) 22 June 2005. Changed to be FeedList Observer
	*******************************************************************************/
	private class PublishSnapshotEvent implements Observer
	{
//		private Installation	m_Installation;
		private Stats		m_Stats;

		/*******************************************************************************
		*******************************************************************************/
		public PublishSnapshotEvent( Stats inStats)
		{
			m_Stats = inStats;
		}

		/*******************************************************************************
		*******************************************************************************/
		public void update( Observable o, Object arg)
		{
			Headlines	h = getMainRSSFeedInstance();

			if ( h != null)		// (AGR) 28 May 2005. Shouldn't happen, but prevent NPE
			{
				h.publishSnapshot(m_Stats);
			}
			else	s_HeadlinesMgr_Logger.info("Cannot write snapshot: no RSS Feed Headlines");
		}
	}
}
