/*
 * HTMLMessageBuilder.java
 *
 * Created on July 3, 2005, 10:28 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.mail;

import com.hiatus.htl.HTL;
import com.hiatus.htl.HTLTemplate;
import com.hiatus.html.UHTML;
import com.hiatus.text.UText;
import java.text.DateFormat;
import java.util.Date;
import java.util.EnumSet;
import org.bloggers4labour.FeedUtils;
import org.bloggers4labour.FormatOption;
import org.bloggers4labour.InstallationIF;

/**
 *
 * @author andrewre
 */
public class HTMLMessageBuilder extends MessageBuilder
{
	private static HTLTemplate	s_HTMLItemTemplate;
	private static HTLTemplate	s_HTML1stItemTemplate;
	private static HTLTemplate	s_HTMLDescTemplate;
	private static HTLTemplate	s_HTMLNoDescTemplate;
	private static HTLTemplate	s_HTMLCatsTemplate;
	private static HTLTemplate	s_HTMLTitleTemplate;
	private static HTLTemplate	s_HTMLNoTitleTemplate;

	/*******************************************************************************
	*******************************************************************************/
	static
	{
		s_HTMLItemTemplate = HTL.createTemplate( "each_item.html", s_Locale);
		s_HTML1stItemTemplate = HTL.createTemplate( "each_item_first.html", s_Locale);
		s_HTMLDescTemplate = HTL.createTemplate( "item_desc.html", s_Locale);
		s_HTMLNoDescTemplate = HTL.createTemplate( "item_no_desc.html", s_Locale);
		s_HTMLCatsTemplate = HTL.createTemplate( "item_cats.html", s_Locale);
		s_HTMLTitleTemplate = HTL.createTemplate( "item_title.html", s_Locale);
		s_HTMLNoTitleTemplate = HTL.createTemplate( "item_no_title.html", s_Locale);
	}

	/*******************************************************************************
	*******************************************************************************/
	public HTMLMessageBuilder( final InstallationIF inInstall)
	{
		super(inInstall);
	}

	/*******************************************************************************
	*******************************************************************************/
	public HTMLMessageBuilder( final InstallationIF inInstall, DateFormat inDF)
	{
		super( inInstall, inDF);
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setCategory( String inStr)
	{
		m_MailContext.put( "categories", inStr);
		m_MailContext.join( "cats_buf", s_HTMLCatsTemplate);
	}

	/*******************************************************************************
	*******************************************************************************/
	public void buildMail( long inItemAgeMSecs, CharSequence inEmailSubject, int inIndex, Date theItemDate, int wantsSummary)
	{
		String		theDescrStr;
		boolean		wantDesc;

		if ( wantsSummary == 1)
		{
			String	cs = m_Item.getDescription();
//			theDescrStr = FeedUtils.newAdjustDescription( cs, Integer.MAX_VALUE, EnumSet.of( FormatOption.ALLOW_IMAGES, FormatOption.ALLOW_BREAKS)); // FeedUtils.adjustDescription( cs, false).trim();

			// (AGR) 3 Feb 2007. Added size limit to save bandwidth!

			theDescrStr = FeedUtils.newAdjustDescription( cs, 300, EnumSet.of( FormatOption.ALLOW_IMAGES, FormatOption.ALLOW_BREAKS)); // FeedUtils.adjustDescription( cs, false).trim();
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
			m_MailContext.join( "desc_buf", s_HTMLDescTemplate);
		}
		else
		{
			m_MailContext.join( "desc_buf", s_HTMLNoDescTemplate);
		}

		///////////////////////////////////////////////////////////////////////////  (AGR) 16 April 2005

		String	theAdjustedTitleStr = FeedUtils.adjustTitle(m_Item);

		if (UText.isValidString(theAdjustedTitleStr))
		{
			m_MailContext.put( "title", theAdjustedTitleStr);
			m_MailContext.join( "title_buf", s_HTMLTitleTemplate);
		}
		else
		{
			m_MailContext.join( "title_buf", s_HTMLNoTitleTemplate);
		}

		///////////////////////////////////////////////////////////////////////////

		String		theSiteStr = ( m_Channel != null) ? m_Channel.getSite().toString() : "";
		String		theDateDiffStr = " " + FeedUtils.getAgeDifferenceString(inItemAgeMSecs);

		m_MailContext.put( "item_index", inIndex + 1);
		m_MailContext.put( "from", theSiteStr);
		m_MailContext.put( "link_url", m_Item.getLink());
		m_MailContext.put( "post_date", UHTML.StringToHtml( m_DF.format(theItemDate) + theDateDiffStr));
		m_MailContext.put( "mail_title", inEmailSubject);
	}

	/*******************************************************************************
	*******************************************************************************/
	public CharSequence generateMessageBody()
	{
		return HTL.mergeTemplate( s_HTMLItemTemplate, m_MailContext);
	}

	/*******************************************************************************
		(AGR) 17 Jan 2007
	*******************************************************************************/
	public CharSequence generate1stMessageBody()
	{
		return HTL.mergeTemplate( s_HTML1stItemTemplate, m_MailContext);
	}

	/*******************************************************************************
	*******************************************************************************/
	public CharSequence generateMessageBodyText()
	{
		return "Your daily digest from Bloggers4Labour";
	}

	/*******************************************************************************
	*******************************************************************************/
	public CharSequence generateMessageBodyHTML()
	{
		return HTL.mergeTemplate( "html_full.html", m_MailContext);
	}
}