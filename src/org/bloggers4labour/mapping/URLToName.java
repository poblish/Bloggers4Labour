/*
 * URLToName.java
 *
 * Created on June 18, 2005, 7:41 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.mapping;

import com.sun.org.apache.xpath.internal.XPathAPI;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.*;
import org.apache.log4j.Logger;
import org.bloggers4labour.conf.Configuration;
import org.bloggers4labour.xml.XMLUtils;
import org.w3c.dom.*;
import org.w3c.dom.traversal.NodeIterator;

/**
 *
 * @author andrewre
 */
public class URLToName
{
//	private HashMap<String,String>	m_Mappings = new HashMap<String,String>(50);
	private Map<String,String>	m_Mappings = new TreeMap<String,String>( new HostComparator() );

	private static Logger		s_Logger = Logger.getLogger("Main");

	/*******************************************************************************
		(AGR) 26 July 2005. For testing!
	*******************************************************************************
	public static void main( String[] args)
	{
		org.bloggers4labour.Launcher	l = new org.bloggers4labour.Launcher(s_Logger);
		l.start();
		new URLToName();
	}/

	/*******************************************************************************
	*******************************************************************************/
	private URLToName()
	{
		Configuration		theConf = Configuration.getInstance();
		DocumentBuilderFactory	docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder		docBuilder;

		try
		{
			docBuilder = docFactory.newDocumentBuilder();

			Document	theHDoc = docBuilder.parse( theConf.findFile("mappings.xml") );
			Element		docElem = theHDoc.getDocumentElement();
			NodeList	headsNodes = docElem.getElementsByTagName("mapping");

			if ( headsNodes != null)
			{
				for ( int i = 0; i < headsNodes.getLength(); i++)
				{
					Element		e = (Element) headsNodes.item(i);
					String		theURLPattern = XMLUtils.getNodeAttrValue( e, "url");
					String		theDisplayName = e.getFirstChild().getNodeValue();

					m_Mappings.put( theURLPattern, theDisplayName);
				}
			}
		}
		catch (Exception e)
		{
			s_Logger.error( "URLToName()", e);
		}

		// s_Logger.info("Mappings: " + m_Mappings);
	}
	
	/*******************************************************************************
	*******************************************************************************/
	public String getMapping( String inName)
	{
		return m_Mappings.get(inName);
	}
	
	/*******************************************************************************
	*******************************************************************************/
	public static URLToName getInstance()
	{
		return LazyHolder.s_Table;
	}
	
	/*******************************************************************************
		(AGR) 5 June 2005. See:
		    <http://www-106.ibm.com/developerworks/java/library/j-jtp03304/>
	*******************************************************************************/
	private static class LazyHolder
	{
		private static URLToName	s_Table = new URLToName();
	}

	/*******************************************************************************
	*******************************************************************************/
	class HostComparator implements Comparator<String>
	{
		/*******************************************************************************
		*******************************************************************************/
		public HostComparator()
		{
		}

		/*******************************************************************************
		*******************************************************************************/
		public int compare( String sa, String sb)
		{
			String	ssa = sa.startsWith("www.") ? sa.substring(4) : sa;
			String	ssb = sb.startsWith("www.") ? sb.substring(4) : sb;

			return ssa.compareToIgnoreCase(ssb);
		}
	}
}
