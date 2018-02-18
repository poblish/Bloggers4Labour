/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hiatus.envt.impl;

import com.hiatus.htl.IncludeFileLocator;
import com.hiatus.text.UText;
import com.hiatus.file.File;
import java.util.Locale;

/**
 *
 * @author andrewregan
 */
public class DefaultFileLocator implements IncludeFileLocator
{
	private String			m_Id;
	private String			m_RootDirPath;
	private String			m_DirPrefix;
	private String			m_DefaultDirName;

	protected final static String	DEFAULT_LANGUAGE = "en";

	/*******************************************************************************
	*******************************************************************************/
	public DefaultFileLocator( final String inId)
	{
		m_Id = inId;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getId()
	{
		return m_Id;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setRootDirectoryPath( final String inX)
	{
		m_RootDirPath = inX;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setDirectoryPrefix( final String inPrefix)
	{
		m_DirPrefix = inPrefix;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setDefaultDirectoryName( final String inX)
	{
		m_DefaultDirName = inX;
	}

	/*******************************************************************************
	*******************************************************************************/
	public File getIncludeFileObject( Locale inLocale, String inFilename)
	{
		return getLocaleFile( inLocale, "includes", inFilename, false);
	}

	/*******************************************************************************
	*******************************************************************************/
	public File getLocaleFile( Locale inLocale, String inDirectoryName, String inFilename, boolean inExactMatch)
	{
		if ( inLocale != null)		// 18 May 2002
		{
			return getLocaleFile( inLocale.getLanguage(), inLocale.getCountry(), inDirectoryName, inFilename, inExactMatch);
		}
		else
		{
			return getLocaleFile( "en", "", inDirectoryName, inFilename, inExactMatch);	// 18 May 2002
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public File getLocaleFile( String inLangString, String inCountryString, String inDirectoryName, String inFilename, boolean inExactMatch)
	{
		// 11 July 2002
//		String		thePrefix = ( testSiteIsLive() || inLangString.equalsIgnoreCase(TEST_LANGUAGE)) ?
//						getProperty("test.locales_dir_name") : getProperty("bm.locales_dir_name");

		String	thePrefix = ( m_DirPrefix != null) ? m_DirPrefix : "";

		///////////////////////////////////////////////////

		StringBuffer	thePathname;
		boolean		gotCountryVariant = UText.isValidString(inCountryString);	// "GB" or nothing?

		thePathname = new StringBuffer(thePrefix).append(inLangString);			// "...locales/en"

		if (gotCountryVariant)
		{
			thePathname.append(File.separator).append(inLangString)		// "...locales/en/en"
				   .append("-").append(inCountryString);			// "...locales/en/en-GB"
		}

		///////////////////////////////////////////////////  22 May 2002

		String	theSuffix;

		if (UText.isValidString(inDirectoryName))
		{
			theSuffix = inDirectoryName + File.separator + inFilename;
		}
		else	theSuffix = inFilename;

		///////////////////////////////////////////////////

		thePathname.append(File.separator).append(theSuffix);				// "...locales/en/en-GB/includes/foo"

// Logger.getLogger("Main").info("////  thePathname: " + thePathname);

		File	theFile = new File( m_RootDirPath, thePathname.toString() );

// Logger.getLogger("Main").info("////  1st try: " + theFile);

		if (theFile.exists())			// If "...locales/en-GB/includes/foo" exists....
		{
			return theFile;
		}
		else
		{
			if (gotCountryVariant)		// If we're just "de", this step becomes pointless...
			{
				thePathname.setLength(0);
				thePathname.append( thePrefix + inLangString + File.separator + theSuffix);

// Logger.getLogger("Main").info("////  thePathname: " + thePathname);

				theFile = new File( m_RootDirPath, thePathname.toString() );

// Logger.getLogger("Main").info("////      2nd try: " + theFile);

				if (theFile.exists())	// if "...locales/de/includes/foo" exists....
				{
					return theFile;
				}
			}

			//////////////////////////////////////////////////////////  Try default language

			if (inExactMatch)		// 23 Feb 2001. Don't show English flag if no Thai or Swedish one can be found!
			{
				return null;
			}
			else if (!inLangString.equals(DEFAULT_LANGUAGE))
			{
				thePathname.setLength(0);
				thePathname.append( thePrefix + DEFAULT_LANGUAGE + File.separator + theSuffix);

// log_info("////  thePathname: " + thePathname);

				theFile = new File( m_RootDirPath, thePathname.toString() );

// Logger.getLogger("Main").info("////          3rd try: " + theFile);

				if (theFile.exists())	// if "...locales/DEFAULT_LANGUAGE/includes/foo" exists....
				{
					return theFile;
				}
			}

			//////////////////////////////////////////////////////////  NO luck with that either - try the default directory

			thePathname.setLength(0);
			thePathname.append( thePrefix + m_DefaultDirName + File.separator + theSuffix);

// log_info("////  thePathname: " + thePathname);

			theFile = new File( m_RootDirPath, thePathname.toString() );

// Logger.getLogger("Main").info("////              Try to use default dir: " + theFile);

			if (theFile.exists())		// if "...locales/default/includes/foo" exists....
			{
				return theFile;
			}

// log_info("////                  Nothing works!!");

			return null;			// Give up!
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}

		final DefaultFileLocator other = (DefaultFileLocator) obj;

		if ( m_Id == null || !m_Id.equals(other.m_Id))
		{
			return false;
		}
		if ( m_RootDirPath == null || !m_RootDirPath.equals(other.m_RootDirPath))
		{
			return false;
		}
		if ( m_DirPrefix == null || !m_DirPrefix.equals(other.m_DirPrefix))
		{
			return false;
		}
		if ( m_DefaultDirName == null || !m_DefaultDirName.equals(other.m_DefaultDirName))
		{
			return false;
		}

		return true;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public int hashCode()
	{
		int hash = 7;
		hash = 67 * hash + (m_Id != null ? m_Id.hashCode() : 0);
		hash = 67 * hash + (m_RootDirPath != null ? m_RootDirPath.hashCode() : 0);
		hash = 67 * hash + (m_DirPrefix != null ? m_DirPrefix.hashCode() : 0);
		hash = 67 * hash + (m_DefaultDirName != null ? m_DefaultDirName.hashCode() : 0);
		return hash;
	}
}
