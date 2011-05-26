/*
 * Link.java
 *
 * Created on May 30, 2005, 12:03 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.tag;

import java.net.URL;
import java.util.EnumSet;
import org.bloggers4labour.*;

/**
 *
 * @author andrewre
 */
public class Link extends Tag
{
	public String	m_URL;
	private URL	m_Source;
	private boolean	m_NeedsPrecedingSpace;	// (AGR) 16 Dec 2005
	private boolean	m_NeedsSucceedingSpace;	// (AGR) 16 Dec 2005

	/********************************************************************
	********************************************************************/
	public Link( int inStartPos, int inEndPos, String inURL, String inName, EnumSet<FormatOption> inOptions)
	{
		super( inStartPos, inEndPos, inName, inOptions);

		m_URL = inURL;
	}

	/********************************************************************
		(AGR) 13 April 2005
	********************************************************************/
	public String getURL()
	{
		return m_URL;
	}

	/********************************************************************
	********************************************************************/
	public String toString()
	{
		return "\"" + m_URL + "\"";
	}

	/********************************************************************
		(AGR) 30 May 2005
	********************************************************************/
	public void setSource( final URL inSourceURL)
	{
		m_Source = inSourceURL;
	}

	/********************************************************************
		(AGR) 30 May 2005
	********************************************************************/
	public URL getSource()
	{
		return m_Source;
	}

	/********************************************************************
		(AGR) 16 Dec 2005
	********************************************************************/
	public void setNeedsPrecedingSpace( boolean inValue)
	{
		m_NeedsPrecedingSpace = inValue;
	}

	/********************************************************************
		(AGR) 16 Dec 2005
	********************************************************************/
	public boolean needsPrecedingSpace()
	{
		return m_NeedsPrecedingSpace;
	}

	/********************************************************************
		(AGR) 16 Dec 2005
	********************************************************************/
	public void setNeedsSucceedingSpace( boolean inValue)
	{
		m_NeedsSucceedingSpace = inValue;
	}

	/********************************************************************
		(AGR) 16 Dec 2005
	********************************************************************/
	public boolean needsSucceedingSpace()
	{
		return m_NeedsSucceedingSpace;
	}
}
