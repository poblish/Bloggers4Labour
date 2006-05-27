/*
 * TextMessageBuilder.java
 *
 * Created on July 3, 2005, 10:35 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.mail;

import com.hiatus.UText;
import com.hiatus.htl.*;
import de.nava.informa.core.*;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;
import org.bloggers4labour.FeedUtils;
import org.bloggers4labour.InstallationIF;

/**
 *
 * @author andrewre
 */
public class TextMessageBuilder extends MessageBuilder
{
	private static HTLTemplate	s_TextItemTemplate;
	private static HTLTemplate	s_TextDescTemplate;
	private static HTLTemplate	s_TextNoDescTemplate;
	private static HTLTemplate	s_TextCatsTemplate;
	private static HTLTemplate	s_TextTitleTemplate;
	private static HTLTemplate	s_TextNoTitleTemplate;

	/*******************************************************************************
	*******************************************************************************/
	static
	{
		s_TextItemTemplate = HTL.createTemplate( "each_item.txt", s_Locale);
		s_TextDescTemplate = HTL.createTemplate( "item_desc.txt", s_Locale);
		s_TextNoDescTemplate = HTL.createTemplate( "item_no_desc.txt", s_Locale);
		s_TextCatsTemplate = HTL.createTemplate( "item_cats.txt", s_Locale);
		s_TextTitleTemplate = HTL.createTemplate( "item_title.txt", s_Locale);
		s_TextNoTitleTemplate = HTL.createTemplate( "item_no_title.txt", s_Locale);
	}

	/*******************************************************************************
	*******************************************************************************/
	public TextMessageBuilder( final InstallationIF inInstall)
	{
		super(inInstall);

		// _loadTemplates();
	}

	/*******************************************************************************
	*******************************************************************************/
	public TextMessageBuilder( final InstallationIF inInstall, DateFormat inDF)
	{
		super( inInstall, inDF);

		// _loadTemplates();
	}

	/*******************************************************************************
	*******************************************************************************
	private void _loadTemplates()
	{
		m_TextItemTemplate = HTL.createTemplate( "each_item.txt", m_Locale);
		m_TextDescTemplate = HTL.createTemplate( "item_desc.txt", m_Locale);
		m_TextNoDescTemplate = HTL.createTemplate( "item_no_desc.txt", m_Locale);
		m_TextCatsTemplate = HTL.createTemplate( "item_cats.txt", m_Locale);
		m_TextTitleTemplate = HTL.createTemplate( "item_title.txt", m_Locale);
		m_TextNoTitleTemplate = HTL.createTemplate( "item_no_title.txt", m_Locale);
	}/

	/*******************************************************************************
	*******************************************************************************/
	public void setCategory( String inStr)
	{
		m_MailContext.put( "categories", inStr);
		m_MailContext.join( "cats_buf", s_TextCatsTemplate);
	}

	/*******************************************************************************
	*******************************************************************************
	public void setCount( int inCount)
	{
		super.setCount(inCount);

		m_EligibleItemsCount = inCount;
	}/

	/*******************************************************************************
	*******************************************************************************
	public void handleFirstMessage()
	{
	}/

	/*******************************************************************************
	*******************************************************************************
	public void handleNextMessage()
	{
	}/

	/*******************************************************************************
	*******************************************************************************/
	public void buildMail( long inItemAgeMSecs, CharSequence inEmailSubject, int inIndex, Date theItemDate, int wantsSummary)
	{
		String		theDescrStr;
		boolean		wantDesc;

		if ( wantsSummary == 1)
		{
			theDescrStr = FeedUtils.newAdjustTextDescription( m_Item.getDescription() ).trim();
			wantDesc = UText.isValidString(theDescrStr);
		}
		else
		{
			theDescrStr = null;
			wantDesc = false;
		}

		///////////////////////////////////////////////////////////////////////////

		if (wantDesc)
		{
			m_MailContext.put( "description", theDescrStr);
			m_MailContext.join( "desc_buf", s_TextDescTemplate);
		}
		else
		{
			m_MailContext.join( "desc_buf", s_TextNoDescTemplate);
		}

		///////////////////////////////////////////////////////////////////////////  (AGR) 16 April 2005

		String	theAdjustedTitleStr = FeedUtils.adjustTitle(m_Item);

		if (UText.isValidString(theAdjustedTitleStr))
		{
			m_MailContext.put( "title", theAdjustedTitleStr);
			m_MailContext.join( "title_buf", s_TextTitleTemplate);
		}
		else
		{
			m_MailContext.join( "title_buf", s_TextNoTitleTemplate);
		}

		///////////////////////////////////////////////////////////////////////////

		m_MailContext.put( "item_index", inIndex + 1);

		if ( m_Channel != null)
		{
			URL	theSiteObj = m_Channel.getSite();
			m_MailContext.put( "from", ( theSiteObj != null) ? theSiteObj.toString() : "");
		}
		else
		{
			m_MailContext.put( "from", "");
		}

		String	theDateDiffStr = " " + FeedUtils.getAgeDifferenceString(inItemAgeMSecs);

		m_MailContext.put( "link_url", m_Item.getLink());
		m_MailContext.put( "post_date", m_DF.format(theItemDate) + theDateDiffStr);
		m_MailContext.put( "mail_title", inEmailSubject);
	}

	/*******************************************************************************
	*******************************************************************************/
	public CharSequence generateMessageBody()
	{
		return HTL.mergeTemplate( s_TextItemTemplate, m_MailContext);
	}

	/*******************************************************************************
	*******************************************************************************/
	public CharSequence generateMessageBodyText()
	{
		return HTL.mergeTemplate( "text_full.txt", m_MailContext);
	}

	/*******************************************************************************
	*******************************************************************************/
	public CharSequence generateMessageBodyHTML()
	{
		return null;
	}
}