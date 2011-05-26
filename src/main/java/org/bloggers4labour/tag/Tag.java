/*
 * Tag.java
 *
 * Created on May 30, 2005, 11:54 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.tag;

import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bloggers4labour.FeedUtils;
import org.bloggers4labour.FormatOption;
import org.bloggers4labour.FormatUtils;
import org.bloggers4labour.TextCleaner;

/**
 *
 * @author andrewre
 */
public class Tag
{
	public int			m_StartPos;
	public int			m_EndPos;
	private String			m_Name;
	private boolean			m_IsImageLink;	// (AGR) 15 May 2005

	private static Pattern		s_ImageFinderPattern = Pattern.compile( TextCleaner.IMAGE_PATTERN_STR, Pattern.CASE_INSENSITIVE);  // (AGR) 15 May 2005

	/********************************************************************
	********************************************************************/
	public Tag( int inStartPos, int inEndPos, String inName)
	{
		m_StartPos = inStartPos;
		m_EndPos = inEndPos;
		m_Name = FeedUtils.stripHTML(inName).trim();	// (AGR) 13 April 2005. Added stripping, e.g. for image or styled links
	}

	/********************************************************************
		(AGR) 15 May 2005. New ctor with 'inOptions' parameter
	********************************************************************/
	public Tag( int inStartPos, int inEndPos, String inName, EnumSet<FormatOption> inOptions)
	{
		m_StartPos = inStartPos;
		m_EndPos = inEndPos;

		if (FormatUtils.allowingImages(inOptions))
		{
			Matcher	imageMatcher = s_ImageFinderPattern.matcher(inName);

			if (imageMatcher.find())	// Check this is actually an image!
			{
				m_Name = inName;
				m_IsImageLink = true;
				return;
			}
		}

		////////////////////////////////////////////////////////////

		m_Name = FeedUtils.stripHTML(inName).trim();	// (AGR) 13 April 2005. Added stripping, e.g. for image or styled links
	}

	/********************************************************************
		(AGR) 13 April 2005
	********************************************************************/
	public String getName()
	{
		return m_Name;
	}

	/********************************************************************
		(AGR) 30 April 2005
	********************************************************************/
	public boolean isImageLink()
	{
		return m_IsImageLink;
	}
}
