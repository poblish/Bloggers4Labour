/*
 * B4LHTLContext.java
 *
 * Created on 22 September 2006, 00:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.htl;

import com.biomates.*;
import com.hiatus.htl.*;
import java.util.Locale;
import java.util.ResourceBundle;

/*******************************************************************************

	HTL = Hiatus Template Language, a very light-weight system
		based upon Jakarta Velocity

	... B4L implementation of HTLAbstractContext, (AGR) 22 September 2006

*******************************************************************************/
public class B4LHTLContext extends HTLAbstractContext
{
	private static ResourceBundle	s_Bundle;

	/*******************************************************************************
	*******************************************************************************/
	static
	{
		s_Bundle = ResourceBundle.getBundle("org/bloggers4labour/Main");
	}

	/*******************************************************************************
	*******************************************************************************/
	public B4LHTLContext( Locale inLocale)
	{
		m_Locale = inLocale;
	}

	/*******************************************************************************
	*******************************************************************************/
	public ResourceBundle getBundle()
	{
		return s_Bundle;
	}

	/*******************************************************************************
		6 October 2001
	*******************************************************************************/
	public String loadString( String inKey)
	{
		return s_Bundle.getString(inKey);
//		return s_Bundle.getString( inKey, m_Locale);
	}

	/*******************************************************************************
		8 May 2002
	*******************************************************************************/
	public Object putString( Object inKey, String inStringKey)
	{
		return put( inKey, s_Bundle.getString(inStringKey));
//		return put( inKey, s_Bundle.getString( inStringKey, m_Locale));
	}

	/*******************************************************************************
	*******************************************************************************/
	public String handleExtraParsingFeatures( String inSourceStr, int inStartPos, int inEndPos)
	{
		if (inSourceStr.regionMatches( inStartPos, "ls:", 0, 3))	// does it start with "ls:" ??
		{
			return s_Bundle.getString( inSourceStr.substring( inStartPos + 3, inEndPos) );
//			return s_Bundle.getString( inSourceStr.substring( inStartPos + 3, inEndPos), m_Locale);
		}

		return null;
	}

	/*******************************************************************************
		30 April 2002
	*******************************************************************************/
	protected String getStringVersion()
	{
		StringBuffer	theBuf = super.getStringBufferVersion();

		theBuf.append(", RBS: ").append(s_Bundle);

		return theBuf.toString();
	}
}