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

import com.hiatus.htl.HTL;
import com.hiatus.htl.HTLContext;
import com.hiatus.htl.HTLTemplate;
import com.hiatus.locales.ULocale2;
import com.hiatus.text.UText;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.bridge.channel.item.ItemIF;
import org.bloggers4labour.site.SiteIF;

/**
 *
 * @author andrewre
 */
public abstract class MessageBuilder
{
	private InstallationIF		m_Installation;

	protected Locale		m_Locale = Locale.UK;
	protected HTLContext		m_MailContext;
	protected DateFormat		m_DF;
	protected ItemIF		m_Item;
	protected ChannelIF		m_Channel;
	protected SiteIF		m_SiteObj;
	protected boolean		m_GotOne = false;

	protected final static Locale	s_Locale = Locale.UK;

	protected final static Logger	s_DS_Logger = Logger.getLogger( MessageBuilder.class );

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
		m_Channel = inItem.getOurChannel();
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
	public SiteIF getSite()
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
	public abstract CharSequence generate1stMessageBody();	// (AGR) (AGR) 17 Jan 2007

	/*******************************************************************************
	*******************************************************************************/
	public abstract CharSequence generateMessageBodyText();
	public abstract CharSequence generateMessageBodyHTML();

	/*******************************************************************************
	*******************************************************************************/
	public void setMessageBody( final CharSequence inBuf)
	{
		m_MailContext.put( "msgs", inBuf);
	}

	/*******************************************************************************
		(AGR) 16 January 2007
	*******************************************************************************/
	public void put( final String inName, final Object inObj)
	{
		m_MailContext.put( inName, inObj);
	}

	/*******************************************************************************
		(AGR) 16 January 2007
	*******************************************************************************/
	public StringBuffer mergeTemplate( final HTLTemplate inTmpl)
	{
		return HTL.mergeTemplate( inTmpl, m_MailContext);
	}
}