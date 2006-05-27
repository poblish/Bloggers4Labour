/*
 * MessageBuilder.java
 *
 * Created on July 3, 2005, 10:22 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.mail;

import com.hiatus.*;
import com.hiatus.htl.*;
import de.nava.informa.core.*;
import java.text.DateFormat;
import java.util.*;
import org.apache.log4j.Logger;
import org.bloggers4labour.*;
import org.bloggers4labour.feed.*;

/**
 *
 * @author andrewre
 */
abstract class MessageBuilder
{
	private InstallationIF		m_Installation;

	protected Locale		m_Locale = Locale.UK;
	protected HTLContext		m_MailContext;
	protected DateFormat		m_DF;
	protected ItemIF		m_Item;
	protected ChannelIF		m_Channel;
	protected Site			m_SiteObj;
	protected boolean		m_GotOne = false;

	protected final static Locale	s_Locale = Locale.UK;

	protected static Logger		s_DS_Logger = Logger.getLogger("Main");

	/*******************************************************************************
	*******************************************************************************/
	public MessageBuilder( final InstallationIF inInstall)
	{
		m_Installation = inInstall;

		m_MailContext = HTL.createInstance(m_Locale);

		m_DF = ULocale2.getClientDateTimeFormat( m_Locale, DateFormat.MEDIUM);

		// s_DS_Logger.info("created context: " + m_MailContext);
	}

	/*******************************************************************************
	*******************************************************************************/
	public MessageBuilder( final InstallationIF inInstall, DateFormat inDF)
	{
		m_Installation = inInstall;

		m_MailContext = HTL.createInstance(m_Locale);

		m_DF = inDF;

		s_DS_Logger.info( m_Installation.getLogPrefix() + "created context: " + m_MailContext);
	}

	/*******************************************************************************
	*******************************************************************************/
	public HTLContext getContext()
	{
		return m_MailContext;
	}

	/*******************************************************************************
	*******************************************************************************/
	public abstract void setCategory( String inStr);

	/*******************************************************************************
	*******************************************************************************/
	public void startNewItem( final ItemIF inItem)
	{
		m_Item = inItem;
		m_Channel = inItem.getChannel();
		m_SiteObj = m_Installation.getFeedList().lookupPostsChannel(m_Channel);

		if (m_GotOne)
		{
			// handleNextMessage();
		}
		else
		{
			// handleFirstMessage();

			m_GotOne = true;
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public void handleCategories()
	{
		clearCategory();		// (AGR) 2 July 2005

		if ( m_SiteObj != null)
		{
			String	theCatsStr = m_SiteObj.getCategoriesString(m_Item);

			if (UText.isValidString(theCatsStr))
			{
				setCategory(theCatsStr);
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public Site getSite()
	{
		return m_SiteObj;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void clearCategory()
	{
		m_MailContext.remove("cats_buf");
	}

	/*******************************************************************************
	*******************************************************************************/
	public void clear()
	{
		// m_MailContext.remove("cats_buf");

		m_MailContext.clear();

		m_Item = null;
		m_Channel = null;
		m_SiteObj = null;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setCount( int inCount)
	{
		m_MailContext.put( "item_count", inCount);
	}

	/*******************************************************************************
	*******************************************************************************/
	public abstract void buildMail( long inItemAgeMSecs, CharSequence inEmailSubject, int inIndex, Date theItemDate, int wantsSummary);

	/*******************************************************************************
	*******************************************************************************/
	public abstract CharSequence generateMessageBody();

	/*******************************************************************************
	*******************************************************************************/
	public abstract CharSequence generateMessageBodyText();
	public abstract CharSequence generateMessageBodyHTML();

	/*******************************************************************************
	*******************************************************************************
	public abstract void handleFirstMessage();
	public abstract void handleNextMessage();
	/

	/*******************************************************************************
	*******************************************************************************/
	public void setMessageBody( final CharSequence inBuf)
	{
		m_MailContext.put( "msgs", inBuf);
	}
}