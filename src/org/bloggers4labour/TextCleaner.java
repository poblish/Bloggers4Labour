/*
 * TextCleaner.java
 *
 * Created on 12 April 2005, 10:51
 */

package org.bloggers4labour;

import com.hiatus.UHTML;
import com.hiatus.UText;
import com.purpletech.util.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.*;
import org.bloggers4labour.tag.*;

/**
 *
 * @author andrewre
 */
public class TextCleaner
{
	private final static String	ABBR_CLOSE_LCASE = "</abbr>";
	private final static String	A_CLOSE_LCASE = "</a>";
	private final static String	U_CLOSE_LCASE = "</u>";
	private final static String	SUP_CLOSE_UCASE = "</SUP>";
	private final static String	SUP_CLOSE_LCASE = "</sup>";

	private final static String	STRIKE_OPEN_LCASE = "<strike>";
	private final static String	STRIKE_CLOSE_UCASE = "</STRIKE>";
	private final static String	STRIKE_CLOSE_LCASE = "</strike>";

	private final static String	I_OPEN_LCASE = "<i>";
	private final static String	I_CLOSE_UCASE = "</I>";
	private final static String	I_CLOSE_LCASE = "</i>";

	private final static String	LINK_PATTERN_STR = "<a [^>]*href *= *";
	public final static String	IMAGE_PATTERN_STR = "<img [^>]*src *= *";	// (AGR) 15 May 2005

	private static Pattern		s_AmpPattern = Pattern.compile("&");
	private static Pattern		s_LtPattern = Pattern.compile("<");
	private static Pattern		s_GtPattern = Pattern.compile(">");

	private static Pattern		s_LinkFinderPattern  = Pattern.compile("(" + LINK_PATTERN_STR + ")", Pattern.CASE_INSENSITIVE);
	private static Pattern		s_TagFinderPattern   = Pattern.compile("(" + LINK_PATTERN_STR + ")|(<abbr [^>]*title *= *)|(<u[^>]*>)|(<sup[^>]*>)|(" + IMAGE_PATTERN_STR + ")|(<br>)|(" + STRIKE_OPEN_LCASE + ")|(" + I_OPEN_LCASE + ")", Pattern.CASE_INSENSITIVE);

	private static Pattern		s_PoundPattern  = Pattern.compile("&#163;");	// (AGR) 16 April 2005. This is pretty dodgy. Assumes certain charset
	private static Pattern		s_eAcutePattern = Pattern.compile("&#233;");	// (AGR) 16 April 2005. This is pretty dodgy. Assumes certain charset

	private static Pattern		s_LinkStripperPattern = Pattern.compile("<a[^>]*>|</a>", Pattern.CASE_INSENSITIVE);

	/********************************************************************
	********************************************************************/
	public static void main(String[] a)
	{
//		String		theQuery = org.bloggers4labour.sql.QueryBuilder.getDigestEmailQuery( 2, 15);

		TextCleaner s = new TextCleaner();
		// s.fookTest();
		// s.oldDo();

//		String	ss = "takes, please get in touch.<a href=\"http://www.recessmonkey.com\"><img src=\"http://bill.verity-networks.com/ext/recess/media/votemonkey.gif\" alt=\"Recess Monkey\" title=\"Recess Monkey\" /></a></p>";
//		String	ss = "This evening's meeting heard reports on: The A2 - Blackheath to <st1:Street w:st=\"on\"><st1:address w:st=\"on\">Shooters Hill Rd</st1:address></st1:Street> Coach f...";
	//	String	ss = "arse<a href=\"http://www.tribute.r8.org\" /> <b> Woot</b>!!</a>!!";
//		String	ss = "<P><!--StartFragment -->Sophia over at&nbsp;<A href=\"http://www.demosgreenhouse.co.uk/archives/000970.html\">Demos</A> has been reflecting on whether there might be lessons to be learned from some bits of the private sector, in particular <A href=\"http://www.pret.com/\">Pret-a-Manger</A>.&nbsp; Im intrigued by some of the ideas (without being clear whether theyre practical in a local government setting), for example:</P>";
//		String	ss = "Chuckie Kennedy party of <strike>not very</strike> prestigious jobs";
	//	String	ss = "<P class=MsoNormal style=\"MARGIN: 0cm 0cm 0pt\"><TT><SPAN style=\"FONT-SIZE: 10pt; FONT-FAMILY: 'Lucida Sans Typewriter'\"><FONT face=\"Arial, Helvetica, sans-serif\">A&nbsp;really interesting email from a member of the Hastings Creative and Media Community&nbsp;came to my attention today, about what is claimed to be ‘the 1<SUP>st</SUP> genuinely independent public access web tv station in Hasings’.<?xml:namespace prefix = o ns = \"urn:schemas-microsoft-com:office:office\" /><o:p></o:p></FONT></SPAN></TT></P>\n<P class=MsoNormal style=\"MARGIN: 0cm 0cm 0pt\"><TT><SPAN style=\"FONT-SIZE: 10pt; FONT-FAMILY: 'Lucida Sans Typewriter'\"><FONT face=\"Arial, Helvetica, sans-serif\">&nbsp;<o:p></o:p></FONT></SPAN></TT></P>\n<P class=MsoNormal style=\"MARGIN: 0cm 0cm 0pt\"><TT><SPAN style=\"FONT-SIZE: 10pt; FONT-FAMILY: 'Lucida Sans Typewriter'\"><FONT face=\"Arial, Helvetica, sans-serif\">Its presently posted </FONT><A href=\"http://www.angusg.f2s.com/\"><FONT face=\"Arial, Helvetica, sans-serif\">here</FONT></A><FONT face=\"Arial, Helvetica, sans-serif\"> but from tomorrow will be running from:</FONT></SPAN></TT></P>\n<P class=MsoNormal style=\"MARGIN: 0cm 0cm 0pt\"><TT><SPAN style=\"FONT-SIZE: 10pt; FONT-FAMILY: 'Lucida Sans Typewriter'\"><FONT face=\"Arial, Helvetica, sans-serif\">&nbsp;<o:p></o:p></FONT></SPAN></TT></P>\n<P class=MsoNormal style=\"MARGIN: 0cm 0cm 0pt\"><TT><SPAN style=\"FONT-SIZE: 10pt; FONT-FAMILY: 'Lucida Sans Typewriter'\"><A href=\"http://www.tvhastings.org/\"><FONT face=\"Arial, Helvetica, sans-serif\">www.tvhastings.org</FONT></A><o:p></o:p></SPAN></TT></P>\n<P class=MsoNormal style=\"MARGIN: 0cm 0cm 0pt\"><TT><SPAN style=\"FONT-SIZE: 10pt; FONT-FAMILY: 'Lucida Sans Typewriter'\"><FONT face=\"Arial, Helvetica, sans-serif\">&nbsp;<o:p></o:p></FONT></SPAN></TT></P><TT><SPAN style=\"FONT-SIZE: 10pt; FONT-FAMILY: 'Lucida Sans Typewriter'; mso-ansi-language: EN-GB; mso-fareast-language: EN-US; mso-bidi-language: AR-SA\"><FONT face=\"Arial, Helvetica, sans-serif\">Its early days but all credit to the creators - I’m sure it can’t fail to be better than the standard of the local dead tree press.</FONT></SPAN></TT>";
//		String	ss = "spotted at<a href=\"http://foo.com\"> normblog</a>.";
	//	String	ss = "spotted at <a href=\"http://foo.com\">normblog </a>Hullo";
//		String	ss = "<p>This site lists Blogs in the UK. Woo.<BR><br  /><a href=\"http://www.britblog.com\"  target=\'_blank\'><p style=\"text-align:center;\"><img src=\"http://www.westminstervillage.co.uk/images/icon_britblog_80x15.gif\" border=\"0\" title=\"\" alt=\"\" class=\"pivot-image\" /></p></a></p>";
	//	String	ss = "rewind<a href=\"#footnote-1-1581\" id=\"footnote-link-1-1581\" title=\"See the footnote.\"><strike>fldflfslsjfs</strike></a> end.";
//		String	ss = "<div class=\"feedflare\"><a href=\"http://feeds.feedburner.com/~f/wongablog?a=ylCzK0wE\"><img src=\"http://feeds.feedburner.com/~f/wongablog?i=ylCzK0wE\" border=\"0\"></img></a> <a href=\"http://feeds.feedburner.com/~f/wongablog?a=msj6myka\"><img src=\"http://feeds.feedburner.com/~f/wongablog?i=msj6myka\" border=\"0\"></img></a> <a href=\"http://feeds.feedburner.com/~f/wongablog?a=iOpV25QU\"><img src=\"http://feeds.feedburner.com/~f/wongablog?i=iOpV25QU\" border=\"0\"></img></a> <a href=\"http://feeds.feedburner.com/~f/wongablog?a=rV8omHpX\"><img src=\"http://feeds.feedburner.com/~f/wongablog?i=rV8omHpX\" border=\"0\"></img></a></div>";
//		String	ss = "<a href=\"http://feeds.feedburner.com/~f/wongablog?a=ylCzK0wE\"><img src=\"http://feeds.feedburner.com/~f/wongablog?i=ylCzK0wE\" border=\"0\"></img></a>";
	//	String	ss = "<a href=\"http://feeds.feedburner.com/~f/wongablog?a=ylCzK0wE\"><img src=\"http://feeds.feedburner.com/~f/wongablog?i=ylCzK0wE\" border=\"0\" /></a>";
//		String	ss = "<p><a id=\"more-788\"></a><center><a href=\"http://www.flickr.com/photos/josalmon/86375059/\"><img src=\"http://static.flickr.com/41/86375059_71b88aaa15.jpg\" width=\"375\" height=\"500\" alt=\"Pitt Rivers Museum, Oxford\" title=\"Pitt Rivers Museum, Oxford\" /></a></center></p>";
	//	String	ss = "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div align=\"left\"><a href=\"http://photos1.blogger.com/blogger/6247/500/1600/ramsrightlogowhite.jpg\"><img alt=\"\" border=\"0\" height=\"197\" src=\"http://photos1.blogger.com/blogger/6247/500/320/ramsrightlogowhite.jpg\" style=\"DISPLAY: block; MARGIN: 0px auto 10px; WIDTH: 181px; CURSOR: hand; HEIGHT: 137px; TEXT-ALIGN: center\" width=\"246\"/></a><br/><strong>                               Tooting and Mitcham v Ramsgate Sat 14th January</strong></div><div align=\"left\"><strong/><br/>Third placed Ramsgate visited second placed Tooting this afternoon, and the strength and pace of both teams produced an entertaining fixture that showed why they are both near the top.<br/>The first chance of the match fell to Ramsgate in the 2nd minute, when Shaun Welford's header went just over the bar. The next 6 minutes saw 6 corners - the first 2 falling to Tooting and the remainder to the Rams, the last one being fumbled by 'keeper Scarcella to allow Stuart Vahid to flick the ball into the net in the 8th minute.<br/>A stunning 'Banks-style' save by Plumley from Mikael Munday at point blank range in the 64th minute retained the Rams' lead in a hard fought 2nd half, whilst in the 80th minute Dave Hastings went down in the Rams' area under a challenge from Warren Schulz, only to receive a yellow card rather than the hoped for penalty. Two minutes into stoppage time Hastings went down in the area between Edd Vahid and Liam Morris, and this time was rewarded with a spot kick that Danny Twin converted for a late equaliser. </div><div align=\"left\"> </div><div align=\"left\">Next Home Match is Sat 21st Jan 06 - 3PM KO against Molesey</div></div>";
//		String	ss = "&#60;i&#62;For as long as we are subjects, not citizens, of our country&#60;/i&#62;Have a <i><b>woo!</b></i>, yeah!";
//		String	ss = "<a href=\"XXX\"><img src=\"YYY.gif\" /></a>"; // "I co-chair the neighbourhood management forum for Heathside and <?xml:namespace prefix=st1 ns= \"urn:schemas-microsoft-com:office:smarttags\" />Lethbridge with Tracey Skingly"; // "<p><a href=\"http://www.amazon.co.uk/exec/obidos/ASIN/B000024V1U/madmusinofme-21\"><img src=\"http://www.madmusingsof.me.uk/archives/suburbs.jpeg\" border=\"1\" title=\"Sound of the Suburbs\"alt=\"Sound of the Suburbs\" hspace=\"3\" vspace=\"3\"></a>&nbsp;A compilation of songs taken from the Golden Age of Pop"; // "<img alt=\"New Labour MPs\" src=\"http://www.andrewregan.com/images/mp/welcome.jpg\"/> Or, \"Yet another newspaper (and blogger) jumps on the bandwagon\"...<br/>\n<a href=\"http://www.mirror.co.uk/news/tm_objectid=15512533%26method=full%26siteid=94762%26headline=the-great-su-doku-challenge-name_page.html\">Have a go yourself</a> then send in your completed entry - plus a note of how long it took you - to see if you've beaten that legendary brainbox, <span style=\"font-weight:bold;\">Carol Vorderman</span> (an <a href=\"http://en.wikipedia.org/wiki/Alan_Turing\">Alan Turing</a> for a simpler age).<br/>\n<br/>I won't reveal the solution (unless you donate to the site!), but I will say that it took me <span style=\"font-weight:bold;\">15 minutes and 50 seconds.</span>\n\n<br/>\n<br/>Actually I must admit that I've been doing a hell of a lot of these recently. <a href=\"http://www.griffiths-jones.co.uk/sudoku/\">This site</a> offers a new one every day, plus there's an archive of easy, medium, hard, and very hard ones going back a month or two. You can also <a href=\"http://www.griffiths-jones.co.uk/sudoku/downloads.html\">download</a> a grid (as a PDF), which is just the right size for printing.<br/>\n<br/>If you still don't know what SuDoku is, look it up at the <a href=\"http://en.wikipedia.org/wiki/Sudoku\">Wikipedia</a>."; // "<div xmlns=\"http://www.w3.org/1999/xhtml\">Here's Celia, with other new Labour MPs at Westminster today:<br/><br/><img alt=\"New Labour MPs\" src=\"http://www.andrewregan.com/images/mp/welcome.jpg\"/>Andrew</div>"; // "<a name=\"c111529093173103484\" />Hilary Wade"; // "A reminder: Vote early and vote often.<sup class=\"foo\" >*</sup><SUP   >*</sup>Where often is defined as more than zero times and not more than one time.";	// "I've only just caught up with this <u class=\"foo\" title=\"bar\" >splendid</u> piece by Michael Lynch in the Chronicle of Higher Education."; // "<a href=\"apple.com\">Apple</a> same way as ...<b><abbr class=\"col\" title=\"Tosser Ex-Manager\">WTEM</ABBR>-Hello <a href=\"ms.com\">MS</a>  bye"; // "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><style type=\"text/css\">.flickr-photo { border: solid 2px #000000; }.flickr-yourcomment { }.flickr-frame { text-align: left; padding: 3px; }.flickr-caption { font-size: 0.8em; margin-top: 0px; }</style><div class=\"flickr-frame\">	<a href=\"http://www.flickr.com/photos/yukihisa/12894905/\" title=\"photo sharing\"><img src=\"http://photos10.flickr.com/12894905_de034941bc.jpg\" class=\"flickr-photo\" alt=\"\" /></a><br />	<span class=\"flickr-caption\"><a href=\"http://www.flickr.com/photos/yukihisa/12894905/\">_DSC5760</a>, originally uploaded by <a href=\"http://www.flickr.com/people/yukihisa/\">yukihisa</a>.</span></div>				<p class=\"flickr-yourcomment\">	One of the things that always gives me hope is the behaviour of children - curious, energetic, demanding and generous. What we do to them as they grow is another matter - but the potential is always there.</p></p></div>";
	//	String	ss = "<object width=\"425\" height=\"350\"><param name=\"movie\" value=\"http://www.youtube.com/v/79T96_WFKRM\"></param><embed src=\"http://www.youtube.com/v/79T96_WFKRM\" type=\"application/x-shockwave-flash\" width=\"425\" height=\"350\"></embed></object><br /><br /><br /><br />Leonard Cohen's song from \"Dear Heather\" album, PV by his daughter, Lorca, featuring Anjani Thomas.";
//		String	ss = "<p>Lets see if this works&#8230;</p>\n<p><object width=\"425\" height=\"350\"><br />\n<param name=\"movie\" value=\"http://www.youtube.com/v/z5e4L3LtYw4\"></param><embed src=\"http://www.youtube.com/v/z5e4L3LtYw4\" type=\"application/x-shockwave-flash\" width=\"425\" height=\"350\"></embed></object></p>\n<p>Hmmm&#8230; Seems to work OK, but what a lot of hoops to jump through!  I had to deactivate the WYSIWYG editing plugin AND create the post in Internet Explorer - for some reason it would not work properly in Firefox. I think I shall push my luck and try to embed the video for the single too.</p>\n<p><object width=\"425\" height=\"350\"><br />\n<param name=\"movie\" value=\"http://www.youtube.com/v/7p7XiPymhYw\"></param><embed src=\"http://www.youtube.com/v/7p7XiPymhYw\" type=\"application/x-shockwave-flash\" width=\"425\" height=\"350\"></embed></object></p>";
		String	ss = "&lt;a href=\"http://eustonmanifesto.org/joomla/content/view/72/46/\">Nice paper&lt;/a> at euston, largely agree with it. Only reccomended for those with a serious case of time on hands!";

	//	List	l = s.collectTags(ss);
	//	System.out.println("l.1 = " + l);
		// System.out.println("--------------------------------------------");

		// System.out.println("l.2 = " + l);
		// System.out.println("--------------------------------------------");

		String	s2 = FeedUtils.newAdjustDescription( ss, 999 );
//		String	s2 = FeedUtils.newAdjustDescription( ss, 250, EnumSet.of( FormatOption.ALLOW_IMAGES, FormatOption.ALLOW_BREAKS));
//		String	s2 = FeedUtils.adjustDescription( ss, false).trim();
		System.out.println("BEFORE: " + ss + " / " + ss.length());
		System.out.println("--------------------------------------------");
		System.out.println(" AFTER: " + s2 + " / " + s2.length());
	}

	/********************************************************************
	********************************************************************/
	public TextCleaner()
	{
		;
	}

	/********************************************************************
	********************************************************************/
	public void fookTest()
	{
		String	s = " CUNT FUCK!!! wanker wankers  fucking fuck sfuck fuckt   sfucking fuckingt   \n\r\tfucking FuCk wank wanking swanking scunt cunts!!! \n\n";
		String	x = FeedUtils.stripExpletives(s);
		System.out.println("BEFORE: " + s);
		System.out.println("======================");
		System.out.println(" AFTER: " + x);
		System.out.println("======================");
		System.out.println("===> \"" + FeedUtils.getDisplayTitle("Horrific London Fuckstick") + "\"");
	}

	/********************************************************************
	********************************************************************
	public void oldDo()
	{
		String	x = "&uuml; <<< >>> £";
		System.out.println("From \"" + x + "\" to \"" + Purple_Utils.htmlescape( x, false) + "\"");
		
//		String 	s = "It now appears that the leaflet referring to the <a href=\"http://erictheunred.blogspot.com/2005/04/protocols-of-nus.html\">Protocols of Zion at the NUS conference</a> was distributed by <a href=\"http://www.gups.org.uk/Home/HOME.htm\">The General Union of Palestinian Students</a> (GUPS). This has provoked a discussion at the <a href=\"http://www.educationet.org/messageboard/posts/47449.html\">Education.net bulletin board</a>, which is fairly illuminating.  The NUS seems to be having the same problems as much of the rest of the left, as Middle Eastern politics are dragged into their discussions, along with all the anti-semitic material which is sadly common currency in that area. At a recent meeting a GUPS spokesperson is described as saying the Holocaust was less important, than the \"holocaust\" being perpetrated by the Jews on the Palestinians.\n\n\n<a href=\"http://www.gups.org.uk/EVENTS/SHAKH-YASSEEN.htm\">GUPS have a nice line</a> in support of the recently departed suicide bomber dispatching <a href=\"http://hurryupharry.bloghouse.net/archives/2004/01/21/sheik_yassin_feminist.php\">Sheikh Yassin</a>: We in GUPS ask almighty Allah to reward Sheikh Yassin with Jannah and promise him and all the martyrs of Palestine that we shall be always steadfast until the day our Palestinian flag flies above Jerusalem, the capital of Palestine.";
		String	s = "More of a nightmare, really. Find their <a href=\"http://www.conservatives.com/pdf/manifesto-uk-2005.pdf\">manifesto</a> here.  At the risk of falling foul of <a href=\"http://en.wikipedia.org/wiki/Godwin\">Godwin's law</a> myself, I was intrigued by the BBC's choice of photo to illustrate the launch.      'We will spend the same as Labour would on the NHS' Nope, Andrew Lansley reckons that the Tory private healthcare subsidy will take ?1.2 billion from the NHS to help the richest 10% in society.   'As well as keeping taxes low, we must reduce the burdens on business through deregulation. A Conservative Government will negotiate to restore our opt-out from the European Social Chapter and liberate small businesses from job destroying employment legislation.'   If you are in work, this little phrase should alarm you. The Social Chapter is designed to allow the EU to set European-wide policy on health and safety, equal pay, working conditions and consultation between management and workforce. The Tories will also force government departments to cap and reduce the costs of regulation, which must also have benefits that exceed the costs. How they will quantify those costs isn't clear, but I'll place a bet that your rights as an employee will suffer.  We will ensure proper discipline in schools by giving heads and governors full control over admissions and expulsions. So if your child is expelled unreasonably, tough. Watch out for schools disposing of those under-performing pupils and passing them onto the new 'Turnaround Schools'  '[We will] review all speed cameras to ensure they are there to save lives, not make money'  <a href=\"http://icbirmingham.icnetwork.co.uk/post/news/tm_objectid=14573204&method=full&amp;siteid=50002-name_page.html\">John Hemming is thinking what they're thinking</a>.  We will introduce a points-based system for work permits similar to the one used in Australia. This will give priority to people with the skills Britain needs. We'll start with <a href=\"http://politics.guardian.co.uk/conservatives/story/0,,1400369,00.html\">dodgy foreign political imports</a>, eh?  'We will take back powers from Brussels to ensure national control of asylum policy, withdraw from the 1951 Geneva Convention... Our objective is a system where we take a fixed number of refugees from the UNHCR rather than simply accepting those who are smuggled to our shores. Asylum seekers? applications will be processed outside Britain. We will set an overall annual limit on the numbers coming to Britain, including a fixed quota for the number of asylum seekers we accept. Parliament will set, and review, that number every year.' This fantasy island for asylum processing returns, as does the 'fixed quota.' This has already <a href=\"http://news.bbc.co.uk/1/hi/uk_politics/vote_2005/frontpage/4428517.stm\">been attacked </a>by Charles Wardle, a former Tory immigration minister, who said that Labour had the most practical policies and that these proposals show that Michael Howard is 'utterly unsuited' to be PM (we know that already, don't we?). If even the leader of the barkingly-mad UKIP party says that your policies on immigration are 'so unworkable it was almost laughable,' then I think you have a real problem. Don't forget that we take 2.8% of all the refugees in the world - making up around 0.4% of our population, hardly overwhelming us. <a href=\"http://politicalhackuk.blogspot.com/2005/01/howard-pulling-up-drawbridge.html\">Still, why let facts get in the way of easy political points scoring</a>?";		
//		String	s = "<a href=\"http://erictheunred.blogspot.com/2005/04/protocols-of-nus.html\">hip</a>";
//		String	s = "Arsey is 5 >= 4, 2 < 3. <<a    href   =   \"x\">>hip & cool</a>Hello <a>>, you && me";
//		String	s = "<a href='http://photos1.blogger.com/img/231/930/1024/ann%20w.jpg'><img border='0' style='border:2px solid #660000; margin:2px' src='http://photos1.blogger.com/img/231/930/400/ann%20w.jpg'></a><br /><br />Via <a href=\"http://blogs.guardian.co.uk/election2005/archives/2005/04/12/you_can_do_better_than_this.html\">The Guardian election blog</a>";
//		String	s = "<a href=\"http://www.marxists.org/archive/mariateg/bio/index.htm\">José Carlos Mariátegui</a>, Peruvian socialist; born 1894, died 16 April 1930.</div>";
		List	theLinks = _collectLinks(s);

		System.out.println("BEFORE: " + s);
		System.out.println("======================");

		StringBuffer	b;
		b = process( s, theLinks, 2100, true);
		System.out.println("AFTER: " + b);
		System.out.println("======================");

		for ( int i = 0; i < 400; i++)
		{
			b = process( s, theLinks, i, true);
			System.out.println("AFTER [" + i + "]: len = " + b.length() + ", " + b);
		}

	}/

	/********************************************************************
		(AGR) 12 July 2005
	********************************************************************/
	public static Matcher getLinkMatcher( String inStr)
	{
		return s_LinkFinderPattern.matcher(inStr);
	}

	/********************************************************************
		(AGR) 12 July 2005
	********************************************************************/
	public static Matcher getLinkStripper( String inStr)
	{
		return s_LinkStripperPattern.matcher(inStr);
	}

	/********************************************************************
	********************************************************************/
	public List<Link> collectLinks( String inStr)
	{
		return collectLinks( inStr, null, FormatUtils.defaultOptions());
	}

	/********************************************************************
	********************************************************************/
	public List<Link> collectLinks( String inStr, URL inSource)
	{
		return collectLinks( inStr, inSource, FormatUtils.defaultOptions());
	}

	/********************************************************************
		(AGR) 15 May 2005. Added 'inOptions'
	********************************************************************/
	public List<Link> collectLinks( String inStr, URL inSource, EnumSet<FormatOption> inOptions)
	{
		if (UText.isNullOrBlank(inStr))
		{
			return null;
		}

		////////////////////////////////////////////////////////////////

		List<Tag>	lt = _collectTags( getLinkMatcher(inStr), inStr, inOptions);

		if ( lt != null && lt.size() > 0)	// (AGR) 30 May 2005. Avoid casting by client, and attach URL rather than make Tag understand URLs
		{
			List<Link>	ll = new ArrayList<Link>( lt.size() );
			Link		theLink;

			for ( Tag t : lt)
			{
				theLink = (Link) t;
				theLink.setSource(inSource);
				ll.add(theLink);
			}

			return ll;
		}

		return null;
	}

	/********************************************************************
	********************************************************************/
	public List<Tag> collectTags( String inStr)
	{
		return collectTags( inStr, FormatUtils.defaultOptions());
	}

	/********************************************************************
		(AGR) 15 May 2005. Added 'inOptions'
	********************************************************************/
	public List<Tag> collectTags( String inStr, EnumSet<FormatOption> inOptions)
	{
		if (UText.isNullOrBlank(inStr))
		{
			return null;
		}

		return _collectTags( s_TagFinderPattern.matcher(inStr), inStr, inOptions);
	}

	/********************************************************************
		(AGR) 15 May 2005. Added 'inOptions'
	********************************************************************/
	private List<Tag> _collectTags( Matcher inMatcher, String inStr, EnumSet<FormatOption> inOptions)
	{
		List<Tag>	theList = new ArrayList<Tag>();
		int		nextCloseLinkPos = -1;

		while (inMatcher.find())
		{
			int	groupCount = inMatcher.groupCount();

			if ( groupCount < 1)	// (AGR) 15 May 2005. No groups -> nothing matched -> don't bother!
			{
				continue;
			}

			////////////////////////////////////////////////////  (AGR) 17 May 2005. Bodge...

			if (( nextCloseLinkPos != -1) && inMatcher.start() < nextCloseLinkPos)
			{
				// System.out.println("Don't backtrack! new index = " + inMatcher.start() + ", last = " + nextCloseLinkPos + ", Input was \"" + inStr + "\"");

				continue;	// If we match an <A> and extract the <IMG> from within it, ensure we don't go back and match the <IMG> in its own right
			}

			////////////////////////////////////////////////////

			boolean	foundLink = ( inMatcher.group(1) != null);
			boolean	foundAbbr = (( groupCount >= 2) && inMatcher.group(2) != null);
			boolean	foundUnderline = (( groupCount >= 3) && inMatcher.group(3) != null);	// (AGR) 10 May 2005
			boolean	foundSuperscript = (( groupCount >= 4) && inMatcher.group(4) != null);	// (AGR) 11 May 2005
			boolean	foundImage = (( groupCount >= 5) && inMatcher.group(5) != null);	// (AGR) 15 May 2005
			boolean	foundBreak = (( groupCount >= 6) && inMatcher.group(6) != null);	// (AGR) 15 May 2005
			boolean	foundStrike = (( groupCount >= 7) && inMatcher.group(7) != null);	// (AGR) 4 August 2005
			boolean	foundItalics = (( groupCount >= 7) && inMatcher.group(8) != null);	// (AGR) 16 Jan 2006

			int	nextPos;
			String	theURLString;

			if ( foundUnderline || foundSuperscript || foundBreak || foundStrike || foundItalics)
			{
				theURLString = null;
				nextPos = inMatcher.start();
			}
			else
			{	
				int	startPos = inMatcher.end() + 1;
				int	nextSpacePos = inStr.indexOf("'", startPos);
				int	nextQuotesPos = inStr.indexOf("\"", startPos);

				if ( nextSpacePos < 0 && nextQuotesPos < 0)
				{
					// something funny going on...
					continue;
				}
				else if ( nextSpacePos < 0)
				{
					nextPos = nextQuotesPos;
				}
				else if ( nextQuotesPos < 0)
				{
					nextPos = nextSpacePos;
				}
				else
				{
					nextPos = ( nextQuotesPos > nextSpacePos) ? nextSpacePos : nextQuotesPos;
				}

				// System.out.println("nextPos = " + nextPos);

				////////////////////////////////////////////////////

				theURLString = inStr.substring( startPos, nextPos).trim();
				// System.out.println("URL: \"" + theURLString + "\"");
			}

			////////////////////////////////////////////////////

			if (foundLink)
			{
				nextCloseLinkPos = _getNextClosePos( inStr, nextPos + 1, "</A>", A_CLOSE_LCASE);
				if ( nextCloseLinkPos == -1)	// (AGR) 7 July 2005. Found <a href="" /> !!!
				{
					nextCloseLinkPos = inStr.indexOf( "/>", nextPos + 1);

					theList.add( new Link( inMatcher.start(), nextCloseLinkPos + 1, theURLString, "", inOptions) );

					continue;
				}
			}
			else if (foundAbbr)
			{
				nextCloseLinkPos = _getNextClosePos( inStr, nextPos + 1, "</ABBR>", ABBR_CLOSE_LCASE);
			}
			else if (foundUnderline)
			{
				nextCloseLinkPos = _getNextClosePos( inStr, nextPos + 1, "</U>", U_CLOSE_LCASE);
			}
			else if (foundSuperscript)
			{
				nextCloseLinkPos = _getNextClosePos( inStr, nextPos + 1, SUP_CLOSE_UCASE, SUP_CLOSE_LCASE);
			}
			else if (foundStrike)	// (AGR) 4 August 2005
			{
				nextCloseLinkPos = _getNextClosePos( inStr, nextPos + 1, STRIKE_CLOSE_UCASE, STRIKE_CLOSE_LCASE);
			}
			else if (foundImage)
			{
				nextCloseLinkPos = _getNextClosePos( inStr, nextPos + 1, "/>", "/>");
			}
			else if (foundBreak)
			{
				nextCloseLinkPos = _getNextClosePos( inStr, nextPos + 1, ">", ">");
			}
			else if (foundItalics)	// (AGR) 16 Jan 2006
			{
				nextCloseLinkPos = _getNextClosePos( inStr, nextPos + 1, I_CLOSE_UCASE, I_CLOSE_LCASE);
			}
			else	continue;

			////////////////////////////////////////////////////

			if ( nextCloseLinkPos < 0)
			{
				// something funny going on...
				continue;
			}
			else if (foundImage)		// (AGR) 15 May 2005
			{
				theList.add( new Image( inMatcher.start(), nextCloseLinkPos + 1, theURLString) );
			}
			else if (foundBreak)		// (AGR) 15 May 2005
			{
				theList.add( new Break( inMatcher.start(), nextCloseLinkPos) );
			}
			else
			{
				int	theNextGTPos = inStr.indexOf( ">", nextPos + 1) + 1;

				if ( theNextGTPos > nextCloseLinkPos)	// (AGR) 23 July 2005
				{
					// (AGR) 23 July 2005. Found the following example: "<a href="http://politics.guardian.co.uk/columnist/story/0,9321,1534728,00.html"John O' Farrell's last comment column in the Grauniad</a>"
					// Not that the initial link is not properly terminated. Try to skip this
					// broken link and attempt to continue, rather than (a) get an out-of-bounds
					// error, or try to be too smart.

					continue;
				}

				String	theLinkString = inStr.substring( theNextGTPos, nextCloseLinkPos).trim();

				// System.out.println("Tag: \"" + theLinkString + "\"");

				if (foundLink)
				{
					if (theURLString.equalsIgnoreCase("http://"))	// (AGR) 30 May 2005
					{
						continue;
					}

					///////////////////////////////////////////////////////////////////////
					//
					// (AGR) 9 Jan 2006. This is a bodge to avoid things (other than images)
					// within links having their HTML encoded. Unfortunately we're too
					// thick to be able to handle nested Tags, so all we can offer is to
					// strip away the HTML from the link's inner tag and just show the
					// name/value (as text). Earlier attempts appear commented-out.
					//

					List<Tag>	theNestedTags;

					theNestedTags = _collectTags( s_TagFinderPattern.matcher(theLinkString), theLinkString, inOptions);
					if ( theNestedTags != null && theNestedTags.size() == 1)
					{
						Tag	theInnerTag = theNestedTags.get(0);

						if ( theInnerTag instanceof Image)
						{
							;
						}
						else
						{
							theLinkString = theInnerTag.getName();

							// CharSequence	cs = process( theLinkString, theNestedTags, Integer.MAX_VALUE, true);

							// theLinkString = cs.toString();

							// theList.addAll(theNestedTags);
						}
					}

					///////////////////////////////////////////////////////////////////////

					Link	ll = new Link( inMatcher.start(), nextCloseLinkPos + 3, theURLString, theLinkString, inOptions);

					///////////////////////////////////////////////////////////////////////
					//
					// (AGR) 16 Dec 2005. Seeing a lot of this kind of thing:
					//
					// (a) Hello<a href="X"> Y</a> Z, which ==> Hello<a href="X">Y</a> Z
					// (b) Hello <a href="X">Y </a>Z, which ==> Hello <a href="X">Y</a>Z
					//
					// Should try to be smart(-arsed), look for whitespace inside the link
					// and 'move' it outside.

					if ( Character.isWhitespace( inStr.charAt(theNextGTPos) ))		// (a) above
					{
						ll.setNeedsPrecedingSpace(true);
					}

					if ( Character.isWhitespace( inStr.charAt( nextCloseLinkPos - 1) ))	// (b) above
					{
						ll.setNeedsSucceedingSpace(true);
					}

					///////////////////////////////////////////////////////////////////////

					// System.out.println("theURLString = " + theURLString);

					theList.add(ll);
				}
				else if (foundAbbr)		// <abbr>
				{
					theList.add( new Abbreviation( inMatcher.start(), nextCloseLinkPos + 6, theURLString, theLinkString) );
				}
				else if (foundUnderline)	// (AGR) 10 May 2005 <u>
				{
					theList.add( new Underline( inMatcher.start(), nextCloseLinkPos + 3, theLinkString) );
				}
				else if (foundSuperscript)	// (AGR) 11 May 2005 <sup>
				{
					theList.add( new Superscript( inMatcher.start(), nextCloseLinkPos + 5, theLinkString) );
				}
				else if (foundStrike)		// (AGR) 4 August 2005 <strike>
				{
					theList.add( new Strike( inMatcher.start(), nextCloseLinkPos + 8, theLinkString) );
				}
				else if (foundItalics)		// (AGR) 16 Jan 2006 <i>
				{
					theList.add( new Italics( inMatcher.start(), nextCloseLinkPos + 3, theLinkString) );
				}
			}
		}

		return theList;
	}

	/********************************************************************
	********************************************************************/
	public int _getNextClosePos( String inStr, int inNextPos, String inUppercaseStr, String inLowercaseStr)
	{
		int	nextCloseLink1Pos = inStr.indexOf( inLowercaseStr, inNextPos);
		int	nextCloseLink2Pos = inStr.indexOf( inUppercaseStr, inNextPos);

		if ( nextCloseLink1Pos < 0)
		{
			return nextCloseLink2Pos;
		}
		else if ( nextCloseLink2Pos < 0)
		{
			return nextCloseLink1Pos;
		}

		return ( nextCloseLink1Pos > nextCloseLink2Pos) ? nextCloseLink2Pos : nextCloseLink1Pos;
	}

	/********************************************************************
	********************************************************************/
	public CharSequence process( String s, List inLinks, int inMaxVisibleChars, boolean inMakeHTML)
	{
		StringBuilder	sb = new StringBuilder( s.length() );
		String		ss;
		int		prevPos = 0;
		int		spaceLeft = inMaxVisibleChars;
		int		charsWeCanTrim;

		if ( inLinks != null)
		{
			for ( Iterator itr = inLinks.iterator(); ( spaceLeft > 0) && itr.hasNext(); )
			{
				Tag	theTag = (Tag) itr.next();

				ss = ( theTag.m_StartPos >= prevPos) ? s.substring( prevPos, theTag.m_StartPos) : "";
				// System.out.println("x  : \"" + ss + "\", spaceLeft = " + spaceLeft);

				int	blockLen = ss.length();

				if ( spaceLeft >= blockLen)
				{
					sb.append( _handleEntities( ss, inMakeHTML) );

					spaceLeft -= blockLen;
				}
				else if ( blockLen >= 3)
				{
					charsWeCanTrim = ( spaceLeft < 3) ? 0 : ( spaceLeft - 3);

					sb.append( _handleEntities( ss.substring( 0, charsWeCanTrim), inMakeHTML) );

					return sb.append("...");
				}
				else	return sb;

				// System.out.println("sb.1 = \"" + sb + "\", spaceLeft = " + spaceLeft);

				////////////////////////////////////////////////////////

				int	nameLen = theTag.getName().length();

			//	System.out.println("spaceLeft = \"" + spaceLeft + "\", nameLen = " + nameLen);

				if ( spaceLeft >= nameLen)
				{
					if (inMakeHTML)
					{
						if (theTag.isImageLink())	// (AGR) 15 May 2005
						{
							_appendTag( sb, theTag, theTag.getName());
							// spaceLeft -= nameLen;
						}
						else
						{
							_appendTag( sb, theTag, _handleEntities( theTag.getName(), inMakeHTML));
							spaceLeft -= nameLen;
						}
					}
					else	// (AGR) 16 April 2005
					{
						sb.append( _convertLinkNameToText(theTag.getName()) );
						spaceLeft -= nameLen;
					}
				}
				else if ( nameLen >= 3)
				{
					charsWeCanTrim = ( spaceLeft < 3) ? 0 : ( spaceLeft - 3);

					if (inMakeHTML)
					{
						if (theTag.isImageLink())	// (AGR) 17 May 2005
						{
							_appendTag( sb, theTag, theTag.getName());	// Is this right??
						}
						else
						{
							_appendTag( sb, theTag, _handleEntities( theTag.getName().substring( 0, charsWeCanTrim), inMakeHTML) + "...");
						}
					}
					else	// (AGR) 16 April 2005
					{
						sb.append( _convertLinkNameToText( theTag.getName().substring( 0, charsWeCanTrim) ) );
					}

					return sb;
				}
				else	return sb;

				////////////////////////////////////////////////////////

				// System.out.println("sb.2 = \"" + sb + "\", spaceLeft = " + spaceLeft);

				prevPos = theTag.m_EndPos + 1;
			}
		}

		/////////////////////////////////////////////////////////

		if ( prevPos < s.length())
		{
			ss = s.substring(prevPos);

			int	blockLen = ss.length();

			if ( spaceLeft >= blockLen)
			{
				sb.append( _handleEntities( ss, inMakeHTML) );

				spaceLeft -= blockLen;
			}
			else if ( blockLen >= 3)
			{
				charsWeCanTrim = ( spaceLeft < 3) ? 0 : ( spaceLeft - 3);
				sb.append( _handleEntities( ss.substring( 0, charsWeCanTrim), inMakeHTML) );

				return sb.append("...");
			}
		}

		return sb;
	}

	/********************************************************************
	********************************************************************/
	private void _appendTag( StringBuilder ioBuf, Tag inTag, String inContentsStr)
	{
		if ( inTag instanceof Link)
		{
			Link	theLink = (Link) inTag;

			if (theLink.needsPrecedingSpace())	// (AGR) 16 Dec 2005
			{
				ioBuf.append(" ");
			}

			ioBuf.append("<a href=\"" + theLink.m_URL + "\" class=\"col\" target=\"_blank\">" + inContentsStr + A_CLOSE_LCASE);

			if (theLink.needsSucceedingSpace())	// (AGR) 16 Dec 2005
			{
				ioBuf.append(" ");
			}
		}
		else if ( inTag instanceof Abbreviation)
		{
			ioBuf.append("<abbr title=\"" + ((Abbreviation) inTag).m_Title + "\">" + inContentsStr + ABBR_CLOSE_LCASE);
		}
		else if ( inTag instanceof Underline)		// (AGR) 10 May 2005
		{
			ioBuf.append("<u>" + inContentsStr + U_CLOSE_LCASE);
		}
		else if ( inTag instanceof Superscript)		// (AGR) 11 May 2005
		{
			ioBuf.append("<sup>" + inContentsStr + SUP_CLOSE_LCASE);
		}
		else if ( inTag instanceof Image)		// (AGR) 15 May 2005
		{
			ioBuf.append("<img src=\"" + ((Image) inTag).m_HRef + "\" alt=\"\" />");
		}
		else if ( inTag instanceof Break)		// (AGR) 15 May 2005
		{
			ioBuf.append("<br />");
		}
		else if ( inTag instanceof Strike)		// (AGR) 4 August 2005
		{
			ioBuf.append( STRIKE_OPEN_LCASE + inContentsStr + STRIKE_CLOSE_LCASE);
		}
		else if ( inTag instanceof Italics)		// (AGR) 16 Jan 2006
		{
			ioBuf.append( I_OPEN_LCASE + inContentsStr + I_CLOSE_LCASE);
		}
	}

	/********************************************************************
	********************************************************************/
	private String _convertLinkNameToText( String inStr)
	{
		if (UText.isValidString(inStr))
		{
			return inStr;
		}

		return "<link>";
	}

	/********************************************************************
	********************************************************************/
	private String _handleEntities( String inStr, boolean inHTML)
	{
		if (!inHTML)
		{
			return s_eAcutePattern.matcher(
					s_PoundPattern.matcher(inStr).replaceAll("£") ).replaceAll("é");
		}

		return Purple_Utils.htmlescape( inStr, false);
	}

	/********************************************************************
		(AGR) 13 April 2005
	********************************************************************/
	public Comparator<Tag> newLinkNameSorter()
	{
		return new LinkNameSorter();
	}

	/********************************************************************
		(AGR) 13 April 2005
	********************************************************************/
	public Comparator<Tag> newLinkURLSorter()
	{
		return new LinkURLSorter();
	}

	/********************************************************************
		(AGR) 13 April 2005
	********************************************************************/
	public class LinkNameSorter implements Comparator<Tag>
	{
		/********************************************************************
		********************************************************************/
		public int compare( Tag thisOne, Tag theOther)
		{
			int	nameScore = thisOne.getName().compareToIgnoreCase( theOther.getName() );

			if ( nameScore != 0)
			{
				return nameScore;
			}

			return ((Link) thisOne).m_URL.compareToIgnoreCase( ((Link) theOther).m_URL );
		}
	}

	/********************************************************************
		(AGR) 13 April 2005
	********************************************************************/
	public class LinkURLSorter implements Comparator<Tag>
	{
		/********************************************************************
		********************************************************************/
		public int compare( Tag thisOne, Tag theOther)
		{
			int	urlScore = ((Link) thisOne).m_URL.compareToIgnoreCase( ((Link) theOther).m_URL );

			if ( urlScore != 0)
			{
				return urlScore;
			}

			return thisOne.getName().compareToIgnoreCase( theOther.getName() );
		}
	}
}
