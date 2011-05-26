/*
 * OPMLGenerator.java
 *
 * Created on 15 April 2005, 00:58
 */

package org.bloggers4labour.opml;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Comparator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import com.sun.org.apache.xml.internal.serialize.Method;		// (AGR) 4 March 2006
import com.sun.org.apache.xml.internal.serialize.OutputFormat;		// (AGR) 4 March 2006
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;		// (AGR) 4 March 2006
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.site.SiteIF;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author andrewre
 */
public class OPMLGenerator implements OPMLGeneratorIF
{
	private DocumentBuilder		m_DB;

	private static Logger		s_OPML_Logger = Logger.getLogger( OPMLGenerator.class );

	/********************************************************************
	********************************************************************/
	public OPMLGenerator()
	{
		try
		{
			m_DB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		}
		catch (ParserConfigurationException e)
		{
		}
	}

	/********************************************************************
	********************************************************************/
	public synchronized String generate( SiteIF[] ioSitesArray) throws IOException
	{
		if ( ioSitesArray == null || ioSitesArray.length < 1)
		{
			return "";
		}
		
		///////////////////////////////////////////////////////

		Document	theDocument = m_DB.newDocument();

		if ( theDocument == null)  // FIXME. How???
		{
			return null;
		}

		Element	theOPML_Elem = theDocument.createElement("opml");
		theOPML_Elem.setAttribute("version","1.1");

		Element	theTitle_Elem = theDocument.createElement("title");
		theTitle_Elem.appendChild( theDocument.createTextNode("Bloggers4Labour feeds") );

		Element	theHead_Elem = theDocument.createElement("head");
		theHead_Elem.appendChild(theTitle_Elem);
		theOPML_Elem.appendChild(theHead_Elem);

		Element	theBody_Elem = theDocument.createElement("body");
		if ( theBody_Elem == null)  // FIXME. How???
		{
			return "";
		}

		///////////////////////////////////////////////////////

		try
		{
			Arrays.sort( ioSitesArray, new SiteComparator());	// (AGR) 18 April 2005
		}
		catch (ArrayIndexOutOfBoundsException ee)
		{
			s_OPML_Logger.error("ArrayBounds! when list = " + Arrays.deepToString(ioSitesArray), ee);    // (AGR) 17 April 2005. How? Why? (AGR) 29 Jan 2007. Removed extra logging
			return "";
		}

		///////////////////////////////////////////////////////

		for ( int i = 0; i < ioSitesArray.length; i++)
		{
			SiteIF		theSite = ioSitesArray[i];
			ChannelIF	ch = theSite.getChannel();

			if ( ch == null)
			{
				continue;
			}

			Element		outlineElem = theDocument.createElement("outline");

			if ( outlineElem != null)
			{
				outlineElem.setAttribute("text", ch.getTitle());
				outlineElem.setAttribute("title", ch.getTitle());
				outlineElem.setAttribute("xmlUrl", theSite.getFeedURL());
				outlineElem.setAttribute("htmlUrl", theSite.getSiteURL());
				outlineElem.setAttribute("type", ch.getFormatString());
				outlineElem.setAttribute("description", ch.getDescription());				

				try
				{
					theBody_Elem.appendChild(outlineElem);
				}
				catch (NullPointerException e)
				{
					s_OPML_Logger.error("NPE: body = " + theBody_Elem + ", elem = " + outlineElem, e);	// (AGR) 17 April 2005. How? Why?
				}
			}
		}

		theOPML_Elem.appendChild(theBody_Elem);
		theDocument.appendChild(theOPML_Elem);

		///////////////////////////////////////////////////////

		StringWriter	sw = new StringWriter();
//		s_OPML_Logger.info("... sw = " + sw);

		String		resultStr;
		OutputFormat	theFormat = new OutputFormat( Method.TEXT, "UTF-8", false);

//		s_OPML_Logger.info("... theFormat = " + theFormat);

		XMLSerializer	theSerializer = new XMLSerializer( sw, theFormat);
//		s_OPML_Logger.info("... serializer = " + theSerializer);
		theSerializer.serialize(theDocument);
//		s_OPML_Logger.info("... serialization DONE.");

		resultStr = sw.toString();

		sw.close();

		return resultStr;
	}

	/********************************************************************
		(AGR) 18 April 2005
	********************************************************************/
	static class SiteComparator implements Comparator<SiteIF>, /* (AGR) 29 Jan 2007. FindBugs recommended this */ Serializable
	{
		private static final long serialVersionUID = 1L;

		/********************************************************************
		********************************************************************/
		public int compare( SiteIF s1, SiteIF s2)
		{
			return ( s1.getRecno() < s2.getRecno() ? -1 : ( s1.getRecno() == s2.getRecno() ? 0 : 1));
		}
	}
}
