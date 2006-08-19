/*
 * FeedUtils.java
 *
 * Created on 13 March 2005, 23:15
 */

package org.bloggers4labour;

import com.hiatus.UDates;
import com.hiatus.UHTML;
import com.hiatus.ULocale2;
import com.hiatus.UText;
import de.nava.informa.core.*;
import de.nava.informa.impl.basic.Item;
import de.nava.informa.utils.ParserUtils;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.bloggers4labour.feed.FeedList;
import org.bloggers4labour.tag.Tag;
import static org.bloggers4labour.Constants.*;

/**
 *
 * @author andrewre
 */
public final class FeedUtils
{
	private static Pattern		s_ClearNewlinesPattern;		// (AGR) 1 April 2005
	private static Pattern		s_aAcutePattern;		// (AGR) 3 April 2005
	private static Pattern		s_uumlPattern;			// (AGR) 8 April 2005
	private static Pattern		s_SpacePattern;
	private static Pattern		s_ClearPattern;
	private static Pattern		s_QuotePattern;			// (AGR) 16 April 2005
	private static Pattern		s_FookPattern;			// (AGR) 19 April 2005
	private static Pattern		s_ImagePattern;			// (AGR) 13 May 2005. Seperated out.
	private static Pattern		s_BreakPattern;			// (AGR) 15 May 2005. Seperated out.

	private static Pattern		s_MidHeaderPattern;		// (AGR) 10 May 2005
	private static Pattern		s_OuterHeaderPattern;		// (AGR) 10 May 2005
	private static Pattern		s_ObjectPattern = Pattern.compile( "<object *>|<object [^>]*>", Pattern.CASE_INSENSITIVE);	// (AGR) 11 June 2006
	private static Pattern		s_ObjectEndPattern = Pattern.compile( "</object>", Pattern.CASE_INSENSITIVE);			// (AGR) 11 June 2006

	private static Logger		s_Utils_Logger = Logger.getLogger("Main");

//	private static final String	ITALIC_STRIPPER = "<i>|<i +[^>]*>|</i>";	// (AGR) 16 Jan 2006. Keep these now. (AGR) 13 May 2005. Made less greedy.
	private static final String	BOLD_STRIPPER = "<b>|<b +[^>]*>|</b>";		// (AGR) 15 May 2005. Made less greedy.
	private static final String	MICROSOFT_STRIPPER = "<\\?xml:namespace +prefix *=.* +ns *= *\"urn:schemas-microsoft-com:.*\" +/>|<o:p[^>]*>|<st1:[^>]*>|</st1:[^>]*>";    // (AGR) 16 May 2005. Improved and factored-out.
	private static final String	FRAG_STRIPPER = "<!--StartFragment -->";					// (AGR) 3 August 2005
	private static final String	TT_STRIPPER = "<tt>|<tt +[^>]*>|</tt>";						// (AGR) 9 November 2005
	private static final String	INTERNAL_LINK_STRIPPER = "<a[^>]*name=\"[^>]*\"[^>]+/>|<a id=\"[^>=]*\"></a>";	// (AGR) 14 Jan 2006
	private static final String	STRONG_STRIPPER = "<strong>|</strong>|<strong */>";				// (AGR) 16 Jan 2006

	private static Pattern		s_CommentAuthorVisPattern = Pattern.compile(" \\[Visitor\\]");			// (AGR) 30 Nov 2005
	private static Pattern		s_CommentAuthorMemPattern = Pattern.compile(" \\[Member\\]");			// (AGR) 30 Nov 2005

	private static NumberFormat	s_MemoryNumFormat;		// (AGR) 22 May 2005
	private static DateFormat	s_BigIssueDotNetDateFormat;	// (AGR) 12 July 2006

	/*******************************************************************************
	*******************************************************************************/
	static
	{
		s_ClearNewlinesPattern = Pattern.compile("[\r\t\n]+");		// (AGR) 1 April 2005
		s_aAcutePattern = Pattern.compile("&amp;aacute;");		// (AGR) 3 April 2005
		s_uumlPattern = Pattern.compile("&amp;uuml;");			// (AGR) 8 April 2005

		s_ImagePattern = Pattern.compile( "<img[^>]*>", Pattern.CASE_INSENSITIVE);					// (AGR) 13 May 2005. Seperated out.
//		s_BreakPattern = Pattern.compile( "<BR/>|<BR +/>|<br/ >|<br>|<br +clear=\"*\".*/>", Pattern.CASE_INSENSITIVE);	// (AGR) 15 May 2005. Seperated out.
		s_BreakPattern = Pattern.compile( "<BR */ *>|<br>|<br +clear=\"*\".*/>", Pattern.CASE_INSENSITIVE);		// (AGR) 6 Jan 2006. Many of these cases are illegal, but better safe than sorry.

		s_SpacePattern = Pattern.compile( "</table>|</p>|</o:p>|</div>|</iframe>|</ul>|</ol>|</li>|</font *>|&nbsp;|</td>|</h[1-7][^>]*>|</tbody>", Pattern.CASE_INSENSITIVE);
//		s_ClearPattern = Pattern.compile( "<p[^>]*>|<small>|</small>|" + BOLD_STRIPPER + "|" + ITALIC_STRIPPER + "|<em[^>]*>|</em>|" + STRONG_STRIPPER + "|</span>|" + MICROSOFT_STRIPPER + "|<!\\[CDATA\\[|<div[^>]*>|<span[^>]*>|<font[^>]*>|<iframe[^>]*>|<center>|</center>|<ul[^>]*>|<ol[^>]*>|<li[^>]*>|]]>|<table[^>]*>|<tbody[^>]*>|<tr[^>]*>|</tr>|<s>|</s>|<del>|</del>|<td[^>]*>|<h[1-7][^>]*>|<style[^>]*>.*</style[^>]*>|<wbr />|</img>|" + FRAG_STRIPPER + "|" + TT_STRIPPER + "|" + INTERNAL_LINK_STRIPPER, Pattern.CASE_INSENSITIVE);
//		s_ClearPattern = Pattern.compile( "<p[^>]*>|<small>|</small>|" + BOLD_STRIPPER + "|<em[^>]*>|</em>|" + STRONG_STRIPPER + "|</span>|" + MICROSOFT_STRIPPER + "|<!\\[CDATA\\[|<div[^>]*>|<span[^>]*>|<font[^>]*>|<iframe[^>]*>|<center>|</center>|<ul[^>]*>|<ol[^>]*>|<li[^>]*>|]]>|<table[^>]*>|<tbody[^>]*>|<tr[^>]*>|</tr>|<s>|</s>|<del>|</del>|<td[^>]*>|<h[1-7][^>]*>|<style[^>]*>.*</style[^>]*>|<wbr />|</img>|" + FRAG_STRIPPER + "|" + TT_STRIPPER + "|" + INTERNAL_LINK_STRIPPER + "|" + "<object .*</object>", Pattern.CASE_INSENSITIVE);
		s_ClearPattern = Pattern.compile( "<p[^>]*>|<small>|</small>|" + BOLD_STRIPPER + "|<em[^>]*>|</em>|" + STRONG_STRIPPER + "|</span>|" + MICROSOFT_STRIPPER + "|<!\\[CDATA\\[|<div[^>]*>|<span[^>]*>|<font[^>]*>|<iframe[^>]*>|<center>|</center>|<ul[^>]*>|<ol[^>]*>|<li[^>]*>|]]>|<table[^>]*>|<tbody[^>]*>|<tr[^>]*>|</tr>|<s>|</s>|<del>|</del>|<td[^>]*>|<h[1-7][^>]*>|<style[^>]*>.*</style[^>]*>|<wbr />|</img>|" + FRAG_STRIPPER + "|" + TT_STRIPPER + "|" + INTERNAL_LINK_STRIPPER, Pattern.CASE_INSENSITIVE);
		s_QuotePattern = Pattern.compile( "<blockquote[^>]*>[\r\n\t ]*\"|<blockquote[^>]*>[\r\n\t ]*|\"[\r\n\t ]*</blockquote>|[\r\n\t ]*</blockquote>", Pattern.CASE_INSENSITIVE);	// (AGR) 19 April 2005. Also strip out initial double-quote

		s_MidHeaderPattern = Pattern.compile( "</th><th[^>]*>", Pattern.CASE_INSENSITIVE);
		s_OuterHeaderPattern = Pattern.compile( "<th[^>]*>|</th>", Pattern.CASE_INSENSITIVE);

		s_FookPattern = Pattern.compile("fucking|\\bfuck|\\bwank|\\bcunt", Pattern.CASE_INSENSITIVE);		// (AGR) 19 April 2005

		////////////////////////////////////////////////////////////////////////////////

		s_MemoryNumFormat = NumberFormat.getNumberInstance( Locale.UK );
		s_MemoryNumFormat.setMaximumFractionDigits(2);

		s_BigIssueDotNetDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);	// (AGR) 12 July 2006. Shudder...
		s_BigIssueDotNetDateFormat.setTimeZone( ULocale2.getBestTimeZone( Locale.UK ));		// Assume published in London time
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String adjustDescription( final String inDescr)
	{
		return adjustDescription( inDescr, true);
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String adjustDescription( final String inDescr, final boolean inAdjustLength)
	{
		if (UText.isValidString(inDescr))
		{
			final String	s = stripHTML(inDescr);

			if ( inAdjustLength && s.length() > 160)
			{
				return s.substring(0,160) + "...";
			}

			return s;
		}

		return inDescr;
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String newAdjustDescription( final String inDescr)
	{
		return newAdjustDescription( inDescr, 170);
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String newAdjustDescription( final String inDescr, final int inMaxVisibleChars)
	{
		return newAdjustDescription( inDescr, inMaxVisibleChars, FormatUtils.defaultOptions());
	}

	/*******************************************************************************
		(AGR) 13 May 2005 - new 'inAllowImages' parameter
	*******************************************************************************/
	public static String newAdjustDescription( final String inDescr, final int inMaxVisibleChars, final EnumSet<FormatOption> inOptions)
	{
		if (UText.isValidString(inDescr))
		{
			final String		s = stripHTML( inDescr, inOptions);
			final TextCleaner	tc = new TextCleaner();
			final List<Tag>		linksArray = tc.collectTags( s, inOptions);

			return tc.process( s, linksArray, inMaxVisibleChars, true).toString().trim();
		}

		return inDescr;
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String newAdjustTextDescription( final String inDescr)
	{
		return newAdjustTextDescription( inDescr, Integer.MAX_VALUE);
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String newAdjustTextDescription( final String inDescr, final int inMaxVisibleChars)
	{
		if (UText.isValidString(inDescr))
		{
			final String		s = stripHTML(inDescr);
			TextCleaner		tc = new TextCleaner();
			final List<Tag>		linksArray = tc.collectTags(s);

			return tc.process( s, linksArray, inMaxVisibleChars, false).toString();
		}

		return inDescr;
	}

	/*******************************************************************************		
	*******************************************************************************/
	public static String stripHTML( final String inStr)
	{
		return stripHTML( inStr, FormatUtils.defaultOptions());
	}

	/*******************************************************************************
		(AGR) 13 May 2005 - new 'inAllowImages' parameter
	*******************************************************************************/
	public static String stripHTML( final String inStr, final EnumSet<FormatOption> inOptions)
	{
		final String	cleanedStr = stripExpletives(inStr);

		if (!cleanedStr.contains("<"))	// (AGR) 27 Feb 2006. A performance bodge - we're only stripping HTML, which must contain a <
		{
			// System.out.println("~~~ Nothing in... " + cleanedStr); // .substring(0, cleanedStr.length() > 100 ? 100 : cleanedStr.length()));

			return cleanedStr;
		}

		////////////////////////////////////////////////////////////////

		final String	imageAdjustedStr = FormatUtils.allowingImages(inOptions) ? cleanedStr : s_ImagePattern.matcher(cleanedStr).replaceAll("");			// (AGR) 13 May 2005
		String		breakAdjustedStr = s_BreakPattern.matcher(imageAdjustedStr).replaceAll( FormatUtils.allowingBreaks(inOptions) ? "<br>" : " ");			// (AGR) 13 May 2005

		////////////////////////////////////////////////////////////////  (AGR) 11 June 2006. Strip everything inside Object tags

		while (true)
		{
			Matcher		objMatcher = s_ObjectPattern.matcher( breakAdjustedStr );

			if (objMatcher.find())
			{
				Matcher	objEndMatcher = s_ObjectEndPattern.matcher(breakAdjustedStr);

				if (objEndMatcher.find( objMatcher.end() ))
				{
					StringBuilder	sb = new StringBuilder( breakAdjustedStr.length() );

					sb.append( breakAdjustedStr.substring( 0, objMatcher.start() ) )
					  .append( breakAdjustedStr.substring( objEndMatcher.end() ) );

					breakAdjustedStr = sb.toString();
				}
				else	break;
			}
			else	break;
		}

		////////////////////////////////////////////////////////////////

		return s_OuterHeaderPattern.matcher(
			s_MidHeaderPattern.matcher(
				s_QuotePattern.matcher(
					s_SpacePattern.matcher(
						s_ClearPattern.matcher( breakAdjustedStr ).replaceAll("") ).replaceAll(" ") ).replaceAll("\"") ).replaceAll(" / ") ).replaceAll("|");
	}

	/*******************************************************************************	
		(AGR) 19 April 2005!
	*******************************************************************************/
	public static String stripExpletives( final String inStr)
	{
		Matcher	m = s_FookPattern.matcher(inStr);

		if (!m.find(0))
		{
			return inStr;
		}

		//////////////////////////////////////////////////

//		s_Utils_Logger.info("::: Found expletive!!! \"" + inStr.substring( m.start(), m.end()) + "\"");

		final StringBuffer	theBuf = new StringBuffer(inStr);
		int			startPos = 0;

		do
		{
			startPos = m.start() + 1;
			theBuf.setCharAt( startPos, '*');
		}
		while (m.find(startPos));

		return theBuf.toString();
	}

	/*******************************************************************************
		(AGR) 3 April 2005
		Not pretty. Some feeds will have already encoded e-acute into
		&aacute;, so that when UHTML gets it, it encodes the & to make &amp;
		For now, use some 'special cases' to reverse the process. Surely
		we can make a 'special' version of StringToHtml ???
	*******************************************************************************/
	public static String getDisplayDescription( final String inStr)
	{
		String	s = UHTML.StringToHtml(inStr);

		return s_uumlPattern.matcher( s_aAcutePattern.matcher(s).replaceAll("&aacute;") ).replaceAll("&uuml;");
	}

	/*******************************************************************************
		(AGR) 8 April 2005
		See getDisplayDescription.

		(AGR) 14 April 2005
		Changed once again

		(AGR) 12 July 2005
		Now remove all trace of links after stripping other HTML
	*******************************************************************************/
	public static String getDisplayTitle( final String inStr)
	{
		if (UText.isValidString(inStr))
		{
			String		s = TextCleaner.getLinkStripper( stripHTML(inStr) ).replaceAll("");
			TextCleaner	tc = new TextCleaner();

			return tc.process( s, null, 170, true).toString();
		}

		return inStr;
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String getDisplayTitle( final ItemIF inItem)
	{
		return getDisplayTitle( inItem.getTitle() );
	}

	/*******************************************************************************
		(AGR) 29 Nov 2005
	*******************************************************************************/
	public static String getCommentAuthor( final ItemIF inItem)
	{
		String	theAuthor = inItem.getElementValue("author/name");

		if (UText.isNullOrBlank(theAuthor))
		{
			return "";
		}

		String	s = s_CommentAuthorVisPattern.matcher(
				s_CommentAuthorMemPattern.matcher(theAuthor).replaceAll("")).replaceAll("");

		return s.trim();
	}

	/*******************************************************************************
		(AGR) 1 April 2005
	*******************************************************************************/
	public static String adjustTitle( final String inTitle)
	{
		if (UText.isValidString(inTitle))
		{
			final String	s = s_ClearNewlinesPattern.matcher( inTitle.trim() ).replaceAll(" ");

			return s;
		}

		return "";
	}

	/*******************************************************************************
		(AGR) 3 April 2005
	*******************************************************************************/
	public static String adjustTitle( final ItemIF inItem)
	{
		return ( inItem != null) ? adjustTitle( inItem.getTitle() ) : "";
	}

	/*******************************************************************************
		Atom 0.3 feeds like NormBlog weren't setting a date tag in the
		expected way, but I found a workaround...
	*******************************************************************************/
	public static Date getItemDate( final ItemIF inItem)
	{
		if ( inItem != null)
		{
			if ( inItem.getDate() != null)
			{
				return inItem.getDate();
			}

			try
			{
				return ParserUtils.getDate( inItem.getElementValue("created") );
			}
			catch (Exception e)
			{
				try	// (AGR) 4 March 2006. "http://geoffblog.co.uk/?atom=1" is Atom v.???, and only has <published> and <updated> !!!
				{
					String	theUpdatedStr = inItem.getElementValue("updated");

					if (theUpdatedStr.endsWith("Z"))	// As if this wasn't enough, a trailing 'Z' would kill any date parsing
					{
						return ParserUtils.getDate( theUpdatedStr.substring( 0, theUpdatedStr.length() - 1));
					}

					return ParserUtils.getDate(theUpdatedStr);
				}
				catch (Exception e2)
				{
					// (AGR) 4 March 2006. Haven't we tried hard enough???
					// (AGR) 12 July 2006. For TheBigIssue.Net's benefit, handle this bogus date format...

					try
					{
						return s_BigIssueDotNetDateFormat.parse( inItem.getElementValue("pubDate") );
					}
					catch (Exception e3)
					{
						;
					}
				}
			}
		}

		return null;
	}

	/*******************************************************************************
		(AGR) 3 April 2005 - 1 minute
	*******************************************************************************/
	public static String getAgeDifferenceString( final long inAgeDiffMSecs)
	{
		if ( inAgeDiffMSecs == 0)
		{
			return "(Now)";
		}
		else if ( inAgeDiffMSecs > 0)
		{
			if ( inAgeDiffMSecs < ONE_MINUTE_MSECS)		// (AGR) 9 April 2005
			{
				return "(< 1 minute ago)";
			}

			return "(" + UDates.getFormattedTimeDiff( inAgeDiffMSecs, false, false) + " ago)";
		}

		// Must be in the future now...

		else if ( -inAgeDiffMSecs < ONE_MINUTE_MSECS)		// (AGR) 6 June 2005
		{
			return "(< 1 minute in the future)";
		}

		return "(" + UDates.getFormattedTimeDiff( -inAgeDiffMSecs, false, false) + " in the future)";
	}

	/*******************************************************************************
		(AGR) 1 April 2005
	*******************************************************************************/
	public static Calendar getNextDigestTime()
	{
		final Calendar	c = Calendar.getInstance( Locale.UK );
		final int	theMins = c.get( Calendar.MINUTE );

		if ( theMins > 45)
		{
			c.roll( Calendar.HOUR_OF_DAY, true);
			c.set( Calendar.MINUTE, 0);
		}
		else if ( theMins > 30)
		{
			c.set( Calendar.MINUTE, 45);
		}
		else if ( theMins > 15)
		{
			c.set( Calendar.MINUTE, 30);
		}
		else // if ( theMins > 0)
		{
			c.set( Calendar.MINUTE, 15);
		}

		c.set( Calendar.SECOND, 0);
		c.set( Calendar.MILLISECOND, 0);

		return c;
	}

	/*******************************************************************************
		(AGR) 21 April 2005
	*******************************************************************************/
	public static String channelToString( final ChannelIF inChannel)
	{
		return ( inChannel != null) ? inChannel.getTitle() : "";
	}

	/*******************************************************************************
		(AGR) 14 Jan 2006
	*******************************************************************************/
	public static boolean isAcceptableFutureDate( long inItemAgeMSecs)
	{
		long	theMsecsInTheFuture = -inItemAgeMSecs;
		return ( theMsecsInTheFuture < ONE_WEEK_MSECS);
	}

	/*******************************************************************************
		(AGR) 4 April - 22 May 2005
	*******************************************************************************/
	public static Date adjustFutureItemDate( final ItemIF ioItem, final Date inItemDate, long inItemAgeMSecs)
	{
		// (AGR) 4 April 2005. Item date is before now. Hmm. Probably because feed is
		// configured to always send in GMT. So even though it's 9pm BST (8pm GMT) here,
		// item claims it was posted at 9m GMT, which is 1hr in the future.
		//
		// Is there a more intelligent solution? Perhaps if the Item API used
		// Calendar instead of Date...

		Calendar	c = Calendar.getInstance();
		long		theMsecsInTheFuture = -inItemAgeMSecs;
		long		theItemDateMsecs = inItemDate.getTime();

		if (!isAcceptableFutureDate(inItemAgeMSecs))
		{
			// Leave the date alone, and let the AgeResult record flag us as unacceptable
		}
		else if (( theMsecsInTheFuture > ( 3L * ONE_HOUR_MSECS)) &&	// (AGR) 19 Dec 2005. New logic
		         ( theMsecsInTheFuture <= ( 8L * ONE_HOUR_MSECS)))	// (AGR) 7 Jan 2006. Had to add this when I noticed a post 25.5 hours in the future!
		{
			// Assume US time (well, PST) if the time is as far into the future as this

//			c.roll( Calendar.HOUR_OF_DAY, -8);
			theItemDateMsecs -= ( 8L * ONE_HOUR_MSECS);
		}
		else
		{
			int	numHoursToRollBack = (int)( theMsecsInTheFuture / ONE_HOUR_MSECS);

			if (( theMsecsInTheFuture % ONE_HOUR_MSECS) != 0)	// not an exact number of hours - cannot leave date in the future
			{
				numHoursToRollBack++;
			}

			theItemDateMsecs -= ( numHoursToRollBack * ONE_HOUR_MSECS);
		}

		// Old logic (pre-19 Dec 2005)
		// c.setTime(inItemDate);
		// c.roll( Calendar.HOUR_OF_DAY, -1);	// subtract an hour

		////////////////////////////////////////////////////////////////

		c.setTimeInMillis(theItemDateMsecs);

		final Date	newDate = c.getTime();

//		System.out.println("## diff = " + UDates.getFormattedTimeDiff(theMsecsInTheFuture) + ", convert from " + inItemDate + " to " + newDate + " !!");

		ioItem.setDate(newDate);

		return newDate;
	}

	/*******************************************************************************
		(AGR) 22 May 2005
	*******************************************************************************/
	public static void logMemory()
	{
		Runtime	r = Runtime.getRuntime();
		System.out.println( new Date() + " ... " + s_MemoryNumFormat.format( (double) r.freeMemory() / 1048576) + " MB free / Total: " + s_MemoryNumFormat.format( (double) r.totalMemory() / 1048576) + " MB / Max: " + s_MemoryNumFormat.format( (double) r.maxMemory() / 1048576) + " MB.");
	}

	/*******************************************************************************
		(AGR) 22 May 2005
	*******************************************************************************/
	public static void logMemory( final String inLabel)
	{
		Runtime	r = Runtime.getRuntime();
		System.out.println( new Date() + " ... " + s_MemoryNumFormat.format( (double) r.freeMemory() / 1048576) + " MB free / Total: " + s_MemoryNumFormat.format( (double) r.totalMemory() / 1048576) + " MB / Max: " + s_MemoryNumFormat.format( (double) r.maxMemory() / 1048576) + " MB. \"" + inLabel + "\"");
	}

	/*******************************************************************************
		(AGR) 25 April - 23 May 2005
	*******************************************************************************/
	public static ItemIF cloneItem( final ChannelIF inChannel, final ItemIF inItem, final boolean inWantCategories)
	{
		Item	theCopy = new Item( inChannel, inItem.getTitle(), inItem.getDescription(), inItem.getLink());	// (AGR) 25 April 2005

		theCopy.setDate( getItemDate(inItem) );
//		theCopy.setComments( inItem.getComments() );
		theCopy.setSubject( inItem.getSubject() );

		if (inWantCategories)
		{
			final ArrayList		theOrig = (ArrayList) inItem.getCategories();

			if ( theOrig != null)
			{
				final Collection	theCollCopy = (Collection) theOrig.clone();
				theCopy.setCategories(theCollCopy);
			}
		}

		return theCopy;
	}
	/*******************************************************************************
		(AGR) 10 June 2006
	*******************************************************************************/
	public static String listToString( final List<?> inList)
	{
		StringBuilder	theSitesBuf = new StringBuilder(500);
		int		numDoneSoFar = 0;
		int		numEntries = inList.size();

		for ( Object eachName : inList)
		{
			if ( numDoneSoFar >= numEntries - 1)	// the last one...
			{
				if ( numEntries >= 3)
				{
					theSitesBuf.append(", and ");
				}
				else if ( numEntries == 2)
				{
					theSitesBuf.append(" and ");
				}
			}
			else if ( numDoneSoFar++ > 0)
			{
				theSitesBuf.append(", ");
			}

			theSitesBuf.append(eachName);
		}

		return theSitesBuf.toString();
	}
}
