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

import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import javax.xml.parsers.*;
import org.apache.log4j.Logger;
import org.bloggers4labour.conf.Configuration;
import org.bloggers4labour.xml.XMLUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 *
 * @author andrewre
 */
public class URLToName
{
	private Map<String,String>	m_Mappings = new Object2ObjectRBTreeMap<String,String>( new HostComparator() );

	private static Logger		s_Logger = Logger.getLogger("Main");

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
		catch (ParserConfigurationException e)
		{
			s_Logger.error( "URLToName()", e);
		}
		catch (SAXException e)
		{
			s_Logger.error( "URLToName()", e);
		}
		catch (IOException e)
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
	static class HostComparator implements Comparator<String>, /* (AGR) 29 Jan 2007. FindBugs recommended this */ Serializable
	{
		private static final long serialVersionUID = 1L;

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
