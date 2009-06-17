/*
 * DisplayItem.java
 *
 * Created on June 18, 2005, 1:11 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.jsp;

import com.hiatus.text.UText;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bloggers4labour.FeedUtils;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.ItemType;
import org.bloggers4labour.TextCleaner;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.bridge.channel.item.ItemIF;
import org.bloggers4labour.site.SiteIF;

/**
 *
 * @author andrewre
 */
public class DisplayItem extends AbstractDisplayable
{
	private ChannelIF		m_Channel;	// (AGR) 29 Jan 2007. Removed pointless 'transient'
	private SiteIF			m_Site;		// (AGR) 29 Jan 2007. Removed pointless 'transient'

	private String			m_Description;
	private String			m_DateString;
	private String			m_SiteURL;
	private String			m_DescriptionStyle;
	private String			m_EncodedTitle;
	private String			m_DispTitle;
	private URL			m_Link;
	private String			m_EncodedLink;

	private ItemType		m_ItemType;		// (AGR) 1 Dec 2005
	private String			m_CommentTitle;		// " " "
	private String			m_CommentAuthor;	// " " "

	private static Pattern		s_PooterGeekBanceCommentPattern = Pattern.compile("Comment on (.*) by:* (.*)");	// (AGR) 7 Jan 2006. Made the colon optional (why? the format changed! from colon-space to just space!!)
	private static Pattern		s_TalkPoliticsCommentPattern = Pattern.compile("In response to:(.*)");
	private static Pattern		s_Trees4LabourCommentPattern = Pattern.compile("(.*)by:* (.+)");		// (AGR) 7 Jan 2006. See comment above - better safe than sorry.

	/*******************************************************************************
	*******************************************************************************/
	public DisplayItem( final InstallationIF inInstall, final ItemIF inItem, final long inTimeMSecs)
	{
		try
		{
			m_Description = FeedUtils.newAdjustDescription( inItem.getDescription() );
		}
		catch (RuntimeException e)
		{
			m_Description = "???";
		}

		String	theTitleStr = inItem.getTitle();

		m_DateString = FeedUtils.getAgeDifferenceString( inTimeMSecs - FeedUtils.getItemDate(inItem).getTime());
		m_DispTitle = FeedUtils.getDisplayTitle(inItem);
		m_Link = inItem.getLink();

		try
		{
			m_EncodedTitle = URLEncoder.encode( theTitleStr, "UTF-8");

			if ( m_Link != null)		// (AGR) 13 Jan 2006
			{
				m_EncodedLink = URLEncoder.encode( m_Link.toString(), "UTF-8");
			}
			else	m_EncodedLink = "";	// (AGR) 13 Jan 2006
		}
		catch (UnsupportedEncodingException e)
		{
		}

		////////////////////////////////////////////////////////////////

		m_Channel = inItem.getOurChannel();
		m_Site = inInstall.getFeedList().lookupChannel(m_Channel);

		if ( m_Site != null)
		{
			m_SiteURL = m_Site.getSiteURL();
			m_DescriptionStyle = m_Site.getDescriptionStyle(inItem);

			////////////////////////////////////////////////////////

			ChannelIF	theCommentsChannel = m_Site.getCommentsChannel();

			if ( theCommentsChannel != null && theCommentsChannel == m_Channel)
			{
				m_ItemType = ItemType.COMMENT;
				m_CommentAuthor = FeedUtils.getCommentAuthor(inItem);	// look for <author>/<name> ...

				////////////////////////////////////////////////  Parse the title for an adjusted title, and possibly an author

				if (UText.isValidString(theTitleStr))
				{
					theTitleStr = TextCleaner.getLinkStripper( FeedUtils.stripHTML(theTitleStr) ).replaceAll("");

					Matcher	m = s_PooterGeekBanceCommentPattern.matcher(theTitleStr);

					if ( m.find() && m.groupCount() == 2)
					{
						m_CommentTitle = m.group(1);

						if (UText.isNullOrBlank(m_CommentAuthor))	// don't let this value override the XML one...
						{
							m_CommentAuthor = m.group(2);
						}
					}
					else	// No? Try another pattern...
					{
						m = s_TalkPoliticsCommentPattern.matcher(theTitleStr);

						if ( m.find() && m.groupCount() == 1)
						{
							m_CommentTitle = m.group(1);
						}
						else	// Still no? Try another pattern...
						{
							m = s_Trees4LabourCommentPattern.matcher(theTitleStr);

							if ( m.find() && m.groupCount() == 2)
							{
								m_CommentTitle = m.group(1);

								if (UText.isNullOrBlank(m_CommentAuthor))	// don't let this value override the XML one...
								{
									m_CommentAuthor = m.group(2);
								}
							}
						}
					}

					////////////////////////////////////////

					if (UText.isValidString(m_CommentTitle))	// Did we find a title? If so, trim it...
					{
						TextCleaner	tc = new TextCleaner();

						m_CommentTitle = tc.process( m_CommentTitle, null, 170, true).toString();
					}

					if ( m_CommentTitle == null)
					{
						m_CommentTitle = "";
					}
				}
			}
			else
			{
				m_ItemType = ItemType.POST;
			}
		}
		else
		{
			m_SiteURL = "";
			m_DescriptionStyle = DEFAULT_DESCRIPTION_STYLE;
			m_ItemType = ItemType.UNKNOWN;
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public SiteIF getSite()
	{
		return m_Site;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getDescription()
	{
		return m_Description;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getDateString()
	{
		return m_DateString;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getSiteURL()
	{
		return m_SiteURL;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getDescriptionStyle()
	{
		return m_DescriptionStyle;
	}

	/*******************************************************************************
		(AGR) 30 September 2006
	*******************************************************************************/
	public String getDescriptionStyle( int inNumRecommendations)
	{
		if ( inNumRecommendations > 0 && ( m_Site != null))
		{
			return  m_Site.getDescriptionStyle( null, inNumRecommendations);
		}

		return /* The default... */ m_DescriptionStyle;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getEncodedTitle()
	{
		return m_EncodedTitle;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getDispTitle()
	{
		return m_DispTitle;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getEncodedLink()
	{
		return m_EncodedLink;
	}

	/*******************************************************************************
	*******************************************************************************/
	public URL getLink()
	{
		return m_Link;
	}

	/*******************************************************************************
		(AGR) 1 Dec 2005
	*******************************************************************************/
	public ItemType getItemType()
	{
		return m_ItemType;
	}

	/*******************************************************************************
		(AGR) 1 Dec 2005
	*******************************************************************************/
	public String getCommentTitle()
	{
		return m_CommentTitle;
	}

	/*******************************************************************************
		(AGR) 1 Dec 2005
	*******************************************************************************/
	public String getCommentAuthor()
	{
		return m_CommentAuthor;
	}
}
