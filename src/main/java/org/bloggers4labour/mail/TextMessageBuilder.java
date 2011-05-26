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

import com.hiatus.htl.HTL;
import com.hiatus.htl.HTLTemplate;
import com.hiatus.text.UText;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import org.bloggers4labour.FeedUtils;
import org.bloggers4labour.InstallationIF;

/**
 *
 * @author andrewre
 */
public class TextMessageBuilder extends MessageBuilder
{
	private static HTLTemplate	s_TextItemTemplate;
	private static HTLTemplate	s_Text1stItemTemplate;
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
		s_Text1stItemTemplate = HTL.createTemplate( "each_item_first.txt", s_Locale);
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
	}

	/*******************************************************************************
	*******************************************************************************/
	public TextMessageBuilder( final InstallationIF inInstall, DateFormat inDF)
	{
		super( inInstall, inDF);
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setCategory( String inStr)
	{
		m_MailContext.put( "categories", inStr);
		m_MailContext.join( "cats_buf", s_TextCatsTemplate);
	}

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
		(AGR) 17 Jan 2007
	*******************************************************************************/
	public CharSequence generate1stMessageBody()
	{
		return HTL.mergeTemplate( s_Text1stItemTemplate, m_MailContext);
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