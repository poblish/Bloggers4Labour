/*
 * OPMLGenerator.java
 *
 * Created on 15 April 2005, 00:58
 */

package org.bloggers4labour.opml;

import de.nava.informa.core.*;
import de.nava.informa.impl.basic.Channel;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
// import org.apache.xml.serialize.Method;
// import org.apache.xml.serialize.OutputFormat;
// import org.apache.xml.serialize.XMLSerializer;
import com.sun.org.apache.xml.internal.serialize.Method;		// (AGR) 4 March 2006
import com.sun.org.apache.xml.internal.serialize.OutputFormat;		// (AGR) 4 March 2006
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;		// (AGR) 4 March 2006
import org.bloggers4labour.feed.FeedList;
import org.bloggers4labour.Site;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author andrewre
 */
public class OPMLGenerator
{
	private DocumentBuilder		m_DB;
	private DocumentBuilderFactory	m_DBF;
	private XMLSerializer		m_XMLSerializer;

	private Document		m_Document;
	private Element			m_OPML_Elem;
	private Element			m_Head_Elem;
	private Element			m_Title_Elem;
	private Element			m_Body_Elem;

	private static Site[]		s_TempSiteArray = new Site[0];
	private static Logger		s_OPML_Logger = Logger.getLogger("Main");

	/********************************************************************
	********************************************************************/
	public OPMLGenerator()
	{
		try
		{
			m_DBF = DocumentBuilderFactory.newInstance();
			m_DB = m_DBF.newDocumentBuilder();
		}
		catch (ParserConfigurationException e)
		{
			;
		}
	}

	/********************************************************************
		(AGR) 19 April 2005
	********************************************************************/
	public Document createDocument()
	{
		return m_DB.newDocument();
	}

	/********************************************************************
	********************************************************************/
	public synchronized boolean generate( Site[] ioSitesArray) // List<Site> inList)
	{
		if ( ioSitesArray == null || ioSitesArray.length < 1)
		{
			return true;
		}
		
		///////////////////////////////////////////////////////

		m_Document = m_DB.newDocument();
		if ( m_Document == null)  // FIXME. How???
		{
			return false;
		}

		m_OPML_Elem = m_Document.createElement("opml");
		m_OPML_Elem.setAttribute("version","1.1");

		m_Title_Elem = m_Document.createElement("title");
		m_Title_Elem.appendChild( m_Document.createTextNode("Bloggers4Labour feeds") );

		m_Head_Elem = m_Document.createElement("head");
		m_Head_Elem.appendChild(m_Title_Elem);
		m_OPML_Elem.appendChild(m_Head_Elem);

		m_Body_Elem = m_Document.createElement("body");
		if ( m_Body_Elem == null)  // FIXME. How???
		{
			return false;
		}

		///////////////////////////////////////////////////////

//		Site[]	theArray;

		try
		{
//			theArray = (Site[]) inList.toArray(s_TempSiteArray);	// (AGR) 15 April 2005. Prevent co-mod exceptions
			Arrays.sort( ioSitesArray, new SiteComparator());	// (AGR) 18 April 2005
		}
		catch (ArrayIndexOutOfBoundsException ee)
		{
			s_OPML_Logger.error("ArrayBounds! when list = " + Arrays.deepToString(ioSitesArray) + ", s_TempSiteArray = " + s_TempSiteArray, ee);	// (AGR) 17 April 2005. How? Why?
			return false;
		}

		for ( int i = 0; i < ioSitesArray.length; i++)
		{
			Site		theSite = ioSitesArray[i];
			ChannelIF	ch = theSite.getChannel();

			if ( ch == null)
			{
				continue;
			}

			Element		outlineElem = m_Document.createElement("outline");

			if ( outlineElem != null)
			{
				outlineElem.setAttribute("text", ch.getTitle());
				outlineElem.setAttribute("title", ch.getTitle());
				outlineElem.setAttribute("xmlUrl", theSite.getFeedURL());
				outlineElem.setAttribute("htmlUrl", theSite.getSiteURL());
				outlineElem.setAttribute("type", ch.getFormat().toString());
				outlineElem.setAttribute("description", ch.getDescription());				

				try
				{
					m_Body_Elem.appendChild(outlineElem);
				}
				catch (NullPointerException e)
				{
					s_OPML_Logger.error("NPE: body = " + m_Body_Elem + ", elem = " + outlineElem, e);	// (AGR) 17 April 2005. How? Why?
				}
			}
		}

		m_OPML_Elem.appendChild(m_Body_Elem);
		m_Document.appendChild(m_OPML_Elem);

		return true;
	}

	/********************************************************************
	********************************************************************/
	public Document getDocument()
	{
		return m_Document;
	}

	/********************************************************************
	********************************************************************/
	public synchronized String serialize() throws IOException
	{
//		s_OPML_Logger.info("... m_Document = " + m_Document);

		if ( m_Document == null)
		{
			return "";
		}

		///////////////////////////////////////////////////////

		StringWriter	sw = new StringWriter();
//		s_OPML_Logger.info("... sw = " + sw);

		String		resultStr;
		OutputFormat	theFormat = new OutputFormat( Method.TEXT, "UTF-8", false);

//		s_OPML_Logger.info("... theFormat = " + theFormat);

		m_XMLSerializer = new XMLSerializer( sw, theFormat);
//		s_OPML_Logger.info("... serializer = " + m_XMLSerializer);
		m_XMLSerializer.serialize(m_Document);
//		s_OPML_Logger.info("... serialization DONE.");

		resultStr = sw.toString();

		sw.close();
		sw = null;

		m_XMLSerializer = null;

		return resultStr;
	}

	/********************************************************************
	********************************************************************/
	public void release()
	{
		m_OPML_Elem = null;
		m_Head_Elem = null;
		m_Title_Elem = null;
		m_Body_Elem = null;
		m_Document = null;

		m_XMLSerializer = null;
	}

	/********************************************************************
		(AGR) 18 April 2005
	********************************************************************/
	class SiteComparator implements Comparator<Site>
	{
		/********************************************************************
		********************************************************************/
		public int compare( Site s1, Site s2)
		{
			return ( s1.getRecno() < s2.getRecno() ? -1 : ( s1.getRecno() == s2.getRecno() ? 0 : 1));
		}
	}
}
