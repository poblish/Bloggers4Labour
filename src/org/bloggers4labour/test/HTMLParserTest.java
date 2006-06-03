/*
 * HTMLParserTest.java
 *
 * Created on 31 May 2006, 21:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.*;
import org.bloggers4labour.feed.*;
import org.htmlparser.*;
import org.htmlparser.filters.*;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 *
 * @author andrewre
 */
public class HTMLParserTest
{
	private static Pattern		theRelPattern = Pattern.compile(" +rel=\"alternate\"");
	private static Pattern		theTypePattern = Pattern.compile(" +type=\"([^\"]*)\"");
	private static Pattern		theHrefPattern = Pattern.compile(" +href=\"([^\"]*)\"");
	private static Pattern		theRSS092Pattern = Pattern.compile(" +title=\"RSS .92\"");

	/********************************************************************
	********************************************************************/
	public static void main( String[] args)
	{
		new HTMLParserTest();
	}

	/********************************************************************
	********************************************************************/
	public static List<Feed> discoverFeeds( final String inSiteURL)
	{
		TagNameFilter	tf = new TagNameFilter("link");

		try
		{
			Parser		p = new Parser(inSiteURL);
			NodeList	nl = p.extractAllNodesThatMatch(tf);
			Node[]		nArray = nl.toNodeArray();
			List<Feed>	theFeedsList = new ArrayList<Feed>();

			for ( Node n : nArray)
			{
				String	theLinkStr = n.getText();
		//		System.out.println(theLinkStr);

				Matcher	relMatcher = theRelPattern.matcher(theLinkStr);
				if (relMatcher.find())
				{
					Matcher	typeMatcher = theTypePattern.matcher(theLinkStr);
					if (typeMatcher.find())
					{
						String	theTypeStr = typeMatcher.group(1);

						if (theTypeStr.startsWith("application/"))
						{
							Matcher	hrefMatcher = theHrefPattern.matcher(theLinkStr);
							if (hrefMatcher.find())
							{
								theTypeStr = theTypeStr.substring(12);

								String	theHRefURL = hrefMatcher.group(1);

								if (theTypeStr.equals("rss+xml"))
								{
									// System.out.println(" RSS 2.0: " + theHRefURL);

									if (theHRefURL.endsWith(".rdf"))	// yuk!
									{
										theFeedsList.add( new Feed( theHRefURL, FeedType.RSD) );
									}
									else
									{
										theFeedsList.add( new Feed( theHRefURL, FeedType.RSS) );
									}
								}
								else if (theTypeStr.equals("atom+xml"))
								{
									// System.out.println("    Atom: " + theHRefURL);
									theFeedsList.add( new Feed( theHRefURL, FeedType.ATOM) );
								}
								else if (theTypeStr.equals("rsd+xml"))
								{
		//										System.out.println(" RSS old: " + theHRefURL);
									theFeedsList.add( new Feed( theHRefURL, FeedType.RSD) );
								}
							}
						}
						else if (theTypeStr.startsWith("text/xml"))
						{
							Matcher	rss092Matcher = theRSS092Pattern.matcher(theLinkStr);
							if (rss092Matcher.find())
							{
								Matcher	hrefMatcher = theHrefPattern.matcher(theLinkStr);
								if (hrefMatcher.find())
								{
									// String	theHRefURL = hrefMatcher.group(1);
									// System.out.println("RSS 0.92: " + theHRefURL);
									theFeedsList.add( new Feed( hrefMatcher.group(1), FeedType.RSD) );
								}
							}
						}
						else	System.out.println("***NO*** " + theLinkStr);
					}
					else	System.out.println("***NO*** " + theLinkStr);
				}
			}

			Collections.sort(theFeedsList);

			return theFeedsList;
		}
		catch (ParserException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/********************************************************************
	********************************************************************/
	public HTMLParserTest()
	{
		TagNameFilter	tf = new TagNameFilter("link");
		int		i = 0;

		while ( i < s_Entries.length)
		{
			Entry	theEntry = new Entry( s_Entries[i], s_Entries[i+1], s_Entries[i+2], s_Entries[i+3] );

			try
			{
				System.out.println( "Doing: " + theEntry.m_Name + " / " + theEntry.m_URL);

				List<Feed>	theFeedsList = discoverFeeds( theEntry.m_URL);

				System.out.println(" => " + theFeedsList);

				System.out.println("  ACTUAL: " + theEntry.m_FeedURL);
				System.out.println("==========================================");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			i += 4;
		}
	}

	/********************************************************************
	********************************************************************/
	private class Entry
	{
		String	m_Name, m_URL, m_FeedURL, m_CommFeedURL;

		/********************************************************************
		********************************************************************/
		public Entry( String a, String b, String c, String d)
		{
			m_Name = a;
			m_URL = b;
			m_FeedURL = c;
			m_CommFeedURL = d;
		}
	}
	/********************************************************************
	********************************************************************/
	private final static String[]	s_Entries = { /*
"Andrew Brown","http://www.20six.co.uk/Cllr_Andrew_Brown","http://www.20six.co.uk/rss/Cllr_Andrew_Brown.rss",null,
"Fiona Colley","http://www.fiona-colley.net","http://www.fiona-colley.net/atom.xml",null,
"Bob Piper","http://councillorbobpiper.blogspot.com","http://councillorbobpiper.blogspot.com/atom.xml",null,
"Richard Corbett","http://www.corbett-euro.demon.co.uk/blog/","http://www.corbett-euro.demon.co.uk/atom.xml",null,
"Clive Soley","http://clivesoleymp.typepad.com/","http://clivesoleymp.typepad.com/clive_soley_mp/index.rdf",null,
"Tom Watson","http://www.tom-watson.co.uk/","http://www.tom-watson.co.uk/index.xml",null,
"Austin Mitchell's WeBLOG","http://www.austinmitchell.org/",null,null,
"Dave Bodimeade","http://www.20six.co.uk/bodimeade","http://www.20six.co.uk/rss/bodimeade.rss",null,
"Dan Whittle","http://www.wellslabour.org/blog.php?",null,null,
"Shaun Woodward","http://www.shaunwoodward.com/","http://www.shaunwoodward.com/go/rss.xml.html",null,
"Ministry of Truth","http://www.ministryoftruth.org.uk/","http://www.ministryoftruth.org.uk/feed/","http://www.ministryoftruth.org.uk/comments/feed/",
"The Westminster Gazette","http://www.20six.co.uk/Westminster","http://www.20six.co.uk/rss/Westminster.rss",null,
"Stuart Bruce (Old)","http://www.20six.co.uk/middletonpark","http://www.20six.co.uk/rss/middletonpark.rss",null,
"A Councillor Writes","http://cramlingtonvillagecouncillor.blogspot.com/","http://cramlingtonvillagecouncillor.blogspot.com/atom.xml",null,
"John Humphries","http://www.20six.co.uk/johnhumphries","http://www.20six.co.uk/rss/johnhumphries.rss",null, */
"Jamie Bolden","http://www.jamiebolden.com/","",null,
"Zoe Hopkins","http://www.20six.co.uk/kingstanding","http://www.20six.co.uk/rss/kingstanding.rss",null,
"Lea Bridge Life","http://leabridgelife.blogspot.com/","http://leabridgelife.blogspot.com/atom.xml",null,
"Billy Hayes","http://www.billyhayes.co.uk/","http://www.billyhayes.co.uk/index.xml",null,
"Phil Bateman Online","http://www.philbateman.com/","",null,
"Karen Bruce","http://www.20six.co.uk/karenmarshall","http://www.20six.co.uk/rss/karenmarshall.rss",null,
"Leighton Andrews (Old)","http://www.leightonandrews.blogspot.com/","http://www.leightonandrews.blogspot.com/atom.xml",null,
"Antonia Bance","http://www.antoniabance.org.uk/","http://www.antoniabance.org.uk/feed/","http://www.antoniabance.org.uk/comments/feed",
"The Virtual Stoa","http://users.ox.ac.uk/~magd1368/weblog/blogger.html","http://users.ox.ac.uk/~magd1368/weblog/vs.xml",null,
"Hat into the ring","http://hattieajderian.typepad.com/","http://feeds.feedburner.com/Hattie_Fixed",null,
"Battersea MP","http://battersea-mp.org.uk/","http://battersea-mp.org.uk/index.rdf",null,
"Jo's Journal","http://www.josalmon.co.uk","http://www.josalmon.co.uk/index.php/feed/","http://www.josalmon.co.uk/comments/feed/",
"Roehampton's Labour team","http://www.labourwandsworth.org.uk/roehampton/blog/","http://www.labourwandsworth.org.uk/roehampton/atom.xml",null,
"PoliticalHackUK","http://politicalhackuk.blogspot.com/","http://politicalhackuk.blogspot.com/atom.xml",null,
"Davos Newbies","http://www.davosnewbies.com","http://www.davosnewbies.com/index.php?feed=rss2",null,
"wongaBlog","http://wandwaver.co.uk/blog/","http://feeds.feedburner.com/wongablog","http://wongablog.co.uk/comments/feed/",
"Anne Campbell","http://www.annecampbell.org.uk/weblog/","",null,
"Gauche","http://libsoc.blogspot.com/","http://libsoc.blogspot.com/atom.xml",null,
"PooterGeek","http://www.pootergeek.com/","http://www.pootergeek.com/wordpress/wp-rss2.php","http://www.pootergeek.com/?feed=comments-rss2",
"British Politics","http://britishspin.blogspot.com","http://britishspin.blogspot.com/atom.xml",null,
"Grangetown Branch Labour Party","http://www.grangetownlabour.blogspot.com/","http://www.grangetownlabour.blogspot.com/atom.xml",null,
"Assembly Labour News","http://www.assemblylabour.blogspot.com/","http://www.assemblylabour.blogspot.com/atom.xml",null,
"normblog","http://normblog.typepad.com/normblog/","http://normblog.typepad.com/normblog/rss.xml",null,
"Eric the Unread","http://erictheunred.blogspot.com","http://erictheunred.blogspot.com/atom.xml",null,
"Harry's Place","http://hurryupharry.bloghouse.net","http://feeds.feedburner.com/HarrysPlaceFixed",null,
"mad musings of me","http://www.madmusingsof.me.uk/weblog/","http://www.madmusingsof.me.uk/weblog/index.rdf",null,
"Labour Friends of Iraq","http://www.labourfriendsofiraq.org.uk/","http://www.labourfriendsofiraq.org.uk/index.rdf",null,
"the thimble","http://thethimble.blogspot.com/","http://thethimble.blogspot.com/atom.xml",null,
"Tooting's Labour team","http://www.labourwandsworth.org.uk/tooting/blog/","",null,
"Battersea's Labour team","http://www.labourwandsworth.org.uk/latchmere/blog/index.htm","http://www.labourwandsworth.org.uk/latchmere/atom.xml",null,
"Sadiq Khan","http://www.sadiqkhan.org.uk/blog/sadiqblog.htm","http://www.sadiqkhan.org.uk/blog/atom.xml",null,
"Catherine Stihler","http://www.cstihlermep.com/ViewPage.cfm?Page=8894","",null,
"Guardian News Blog","http://blogs.guardian.co.uk/news/archives/cat_uk_politics.html","http://blogs.guardian.co.uk/news/atom.xml",null,
"Neil MacDonald","http://neilmacdonald.typepad.com/","http://feeds.feedburner.com/NeilMacdonald_Fixed",null,
"LabourStart","http://www.labourstart.org/","http://www.labourstart.org/rss/labourstart.uk.xml",null,
"Black Triangle","http://blacktriangle.org/blog","http://www.blacktriangle.org/wordpress/wp-rss2.php","http://www.blacktriangle.org/blog/?feed=comments-rss2",
"Let's be sensible","http://letsbesensible.blogspot.com","http://letsbesensible.blogspot.com/atom.xml",null,
"Stoke Labour Group","http://stokelabourgroup.blogspot.com/","http://stokelabourgroup.blogspot.com/atom.xml",null,
"Allan Wilson","http://allanwilsonmsp.com/v-web/b2/","http://allanwilsonmsp.com/v-web/b2/b2rss2.php",null,
"What Comes To Pass","http://www.whatcomestopass.com/","http://www.whatcomestopass.com/index.rdf",null,
"Election Blog 2005","http://blogs.guardian.co.uk/election2005/","http://blogs.guardian.co.uk/election2005/index.xml",null,
"A Cloud In Trousers","http://cloud-in-trousers.blogspot.com/","http://cloud-in-trousers.blogspot.com/atom.xml",null,
"Recess Monkey","http://www.recessmonkey.com/","http://www.recessmonkey.com/feed/","http://www.recessmonkey.com/comments/feed/",
"Delbert Wilkins","http://delbertwilkins.blogspot.com/","http://delbertwilkins.blogspot.com/atom.xml",null,
"Rob Newman","http://robnewman.typepad.com/","http://robnewman.typepad.com/rob_newman/rss.xml",null /* ,
"Bloggers4Labour","http://www.bloggers4labour.org/","http://www.bloggers4labour.org/atom.xml",null,
"Rullsenberg Rules","http://rullsenbergrules.blogspot.com/","http://rullsenbergrules.blogspot.com/atom.xml",null,
"Jonathan Derbyshire","http://jonathanderbyshire.typepad.com","http://feeds.feedburner.com/JonathanDerbyshireFixed",null,
"Bowblog","http://bowblog.com","http://feeds.feedburner.com/Bowblog_Fixed",null,
"Deep Calls to Deep","http://www.deepcallstodeep.sonafide.com","http://www.deepcallstodeep.sonafide.com/atom.xml",null,
"Derek Wyatt","http://www.derekwyatt.co.uk/pages/blog.asp?i_PageID=1817","",null,
"NightHawk","http://www.rogerdarlington.co.uk/nighthawk/","http://feeds.feedburner.com/Nighthawk_Fixed",null,
"I found that essence rare","http://skuds.blogspot.com/","http://skuds.blogspot.com/atom.xml",null,
"Michael Howard MP","http://www.michaelhowardmp.co.uk/","",null,
"Grammar Puss","http://thegrammaticalpuss.blogspot.com/","http://thegrammaticalpuss.blogspot.com/atom.xml",null,
"Dadblog","http://www.dadblog.co.uk/","http://www.dadblog.co.uk/atom.xml",null,
"Frank Dobson","http://www.channel4.com/news/microsites/E/election2005_blogs/dobson_blog.html","",null,
"Emily's election blog","http://www.islingtonlabour.org.uk","",null,
"popsensible","http://popsensible.modblog.com/","http://popsensible.modblog.com/backend/modblog/modblogs.rss.php",null,
"The Salmon of Doubt","http://smeeble.blogspot.com/","http://smeeble.blogspot.com/atom.xml",null,
"A General Theory of Rubbish","http://www.gentheoryrubbish.com/","http://www.gentheoryrubbish.com/index.xml",null,
"Westminster Village","http://www.westminstervillage.co.uk/","http://www.westminstervillage.co.uk/atom.xml",null,
"Small Town Scribbles","http://smalltownscribble.blogspot.com/","http://smalltownscribble.blogspot.com/atom.xml",null,
"Spirit of 1976","http://spiritof1976.livejournal.com","http://spiritof1976.livejournal.com/data/rss",null,
"Me and Ophelia","http://meandophelia.blogspot.com/","http://meandophelia.blogspot.com/rss/meandophelia.xml",null,
"Eric Lee","http://www.ericlee.info/","http://www.ericlee.info/index.xml",null,
"Never Trust a Hippy","http://nevertrustahippy.blogspot.com/","http://nevertrustahippy.blogspot.com/atom.xml",null,
"Paul Nowak's blog: Unions, politics and more","http://www.paulnowak.blogspot.com","http://paulnowak.blogspot.com/atom.xml",null,
"Panchromatica","http://ibanda.blogs.com/","http://ibanda.blogs.com/panchromatica/rss.xml",null,
"Rodney McAree Forever!","http://rodneymcaree.blogspot.com/","http://rodneymcaree.blogspot.com/atom.xml",null,
"DM Andy's Bits and Pieces","http://dm-andy.blogspot.com","http://dm-andy.blogspot.com/atom.xml",null,
"Julie Morgan (Old)","http://juliemorgan.blogdrive.com/","http://juliemorgan.blogdrive.com/atom.xml",null,
"Dirty Leftie (Old)","http://dirtyleftie.blogspot.com","http://dirtyleftie.blogspot.com/atom.xml",null,
"Blairite Bob","http://blairitebob.blogdrive.com/","",null,
"JamesZ_Blog","http://www.186mph.blogspot.com","http://186mph.blogspot.com/atom.xml",null,
"Carl Roper - Unions & Organising","http://www.carlroper.blogspot.com/","http://www.carlroper.blogspot.com/atom.xml",null,
"Anthony E. Mckeown","http://anthonymckeown.info/blog.html","http://www.anthonymckeown.info/atom.xml",null,
"Leighton Andrews","http://leightonandrews.typepad.com/leighton_andrews_am/","http://leightonandrews.typepad.com/leighton_andrews_am/rss.xml",null,
"Rhondda Today / Y Rhondda Heddiw","http://www.leightonandrews.typepad.com/rhondda_today/","http://leightonandrews.typepad.com/rhondda_today/rss.xml",null,
"BrightonRegencyLabourParty","http://brightonregencylabourparty.blogspot.com/","http://brightonregencylabourparty.blogspot.com/atom.xml",null,
"Trees for Labour (Old)","http://tamanou.blogspot.com/","http://tamanou.blogspot.com/atom.xml",null,
"Benjamin Comments","http://benjamincomments.wordpress.com/","http://benjamincomments.wordpress.com/feed/","http://benjamincomments.wordpress.com/comments/feed/",
"Mike's Little Red Page","http://www.oxyacetylene.blogspot.com/","http://oxyacetylene.blogspot.com/atom.xml",null,
"Just another false alarm...","http://rubberring.blogspot.com/","http://rubberring.blogspot.com/atom.xml",null,
"Pendre Ward Forum","http://www.pendre.blogspot.com/","http://pendre.blogspot.com/atom.xml",null,
"Elephunt","http://elephunt.blogspot.com/","http://elephunt.blogspot.com/atom.xml",null,
"Rachel's blog","http://nethesi.blogspot.com/","http://nethesi.blogspot.com/atom.xml",null,
"KERRON CROSS - The Voice of the Delectable Left","http://kerroncross.blogspot.com","http://kerroncross.blogspot.com/atom.xml",null,
"Politics for beginners","http://politicsforbeginners.blogspot.com/","http://politicsforbeginners.blogspot.com/atom.xml",null,
"Owen's Musings","http://www.owen.org/blog","http://www.owen.org/blog/?feed=rss2","http://www.owen.org/blog/?feed=comments-rss2",
"Stephen Newton's diary of sorts...","http://www.stephennewton.com/","http://feeds.feedburner.com/diaryofsorts",null,
"From Socialite to Socialist: Leftism in Surrey","http://redflagboy.blogspot.com/","http://redflagboy.blogspot.com/atom.xml",null,
"Andy Reed MP","http://www.andyreedmp.org.uk/","http://feeds.feedburner.com/andyreedmp",null,
"Mars Hill","http://www.paulburgin.blogspot.com","http://paulburgin.blogspot.com/atom.xml",null,
"Lee Gregory","http://www.leegregory.typepad.com/","http://leegregory.typepad.com/lee_gregory/rss.xml",null,
"Pete Morton","http://www.petemorton.blogspot.com/","http://petemorton.blogspot.com/atom.xml",null,
"Bratiaith","http://bratiaith.blogspot.com","http://bratiaith.blogspot.com/atom.xml",null,
"James' Nasty Political Problems","http://theboabie.blogspot.com/","http://theboabie.blogspot.com/atom.xml",null,
"To the Point","http://thekupfers.typepad.com/tothepoint/","http://thekupfers.typepad.com/tothepoint/rss.xml",null,
"Jonathan Bishop","http://www.jonathanbishop.org.uk/weblog/","",null,
"Huggy's Mind","http://huggysmind.blogspot.com/","http://huggysmind.blogspot.com/atom.xml",null,
"East Acton","http://www.east-acton.com","http://east-acton.blogspot.com/atom.xml",null,
"Eastcliff Matters","http://eastclifframsgate.blogspot.com/","http://eastclifframsgate.blogspot.com/atom.xml",null,
"Orlik Report","http://orlikreport.blogspot.com","http://orlikreport.blogspot.com/atom.xml",null,
"John West - Labour supporter and journalist","http://johnwestjourno.blogspot.com/","http://johnwestjourno.blogspot.com/atom.xml",null,
"Mike Ion","http://mike-ion.blogspot.com/","http://mike-ion.blogspot.com/atom.xml",null,
"Our Labour Manifesto","http://123.writeboard.com/a797eb2b85fb98580","http://123.writeboard.com/a797eb2b85fb98580/feed/18e3fcfdb9d52ee011fc4bde4ea3c207",null,
"Labour in London","http://www.labourinlondon.org.uk/blog","",null,
"Dirty Leftie","http://www.dirtyleftie.co.uk/","http://www.leftblogs.org.uk/?feed=rss2","http://www.dirtyleftie.co.uk/?feed=comments-rss2",
"Trees for Labour","http://treesforlabour.wordpress.com/","http://treesforlabour.wordpress.com/feed/","http://treesforlabour.wordpress.com/comments/feed/",
"Baggage Reclaim","http://www.livejournal.com/users/bagrec/","http://www.livejournal.com/users/bagrec/data/rss",null,
"Barry's Beef","http://www.barrysbeef.blogspot.com/","http://barrysbeef.blogspot.com/atom.xml",null,
"Cllr Geoff Lumley","http://geofflumley.blogspot.com/","http://geofflumley.blogspot.com/atom.xml",null,
"Fourth Term","http://fourthterm.net","http://fourthterm.net/postnuke/html/backend.php",null,
"Beyond The City Walls","http://yorkielabour.blogspot.com","http://yorkielabour.blogspot.com/atom.xml",null,
"Yellow Peril","http://libdems.fourthterm.net","http://fourthterm.net/plog/rss.php?blogId=1&profile=rss10",null,
"North Yorkshire Times (Old)","http://northyorkshiretimes.blogspot.com/","",null,
"B","http://everton.blogspot.com","http://everton.blogspot.com/rss/everton.xml",null,
"Madoc Councillor","http://madoccouncillor.blogspot.com/","http://madoccouncillor.blogspot.com/atom.xml",null,
"Julie Morgan","http://juliemorgan.typepad.com/","http://juliemorgan.typepad.com/julie_morgan_mp/rss.xml",null,
"About Whose News","http://about-whose-news.blogspot.com/","http://about-whose-news.blogspot.com/atom.xml",null,
"Sophia Howe","http://sophiehowe.blogs.com/","http://sophiehowe.blogs.com/sophie_howe/rss.xml",null,
"Marlyn Glen","http://www.marlynsblog.org.uk","http://northeastscotlandlabour.blogspot.com/atom.xml",null,
"The Silent Hunter","http://thesilenthunter.blogspot.com","http://thesilenthunter.blogspot.com/atom.xml",null,
"NewerLabour","http://www.newerlabour.blogspot.com/","http://newerlabour.blogspot.com/atom.xml",null,
"A Far Fetched Resolution","http://www.afarfetchedresolution.com/","http://afarfetchedresolution.blogspot.com/atom.xml",null,
"Nick Colbourne","http://ruabonlabourcouncillor.blogspot.com/","http://ruabonlabourcouncillor.blogspot.com/atom.xml",null,
"Stuart's Soapbox","http://www.stuartssoapbox.com/","http://www.stuartssoapbox.com/rss.xml",null,
"Stodge","http://blog.stodge.org","http://blog.stodge.org/feed",null,
"Skipper","http://skipper59.blogspot.com/","http://skipper59.blogspot.com/atom.xml",null,
"Richard Williams","http://labour4crystalpalace.blogspot.com/","http://labour4crystalpalace.blogspot.com/atom.xml",null,
"TUC Organising Academy","http://www.organisingacademy.blogspot.com/","http://organisingacademy.blogspot.com/atom.xml",null,
"Hakmao","http://blog.hakmao.com/","http://blog.hakmao.com/index.xml",null,
"Gareth Griffiths","http://coedpoeth-ward.blogspot.com/","http://coedpoeth-ward.blogspot.com/atom.xml",null,
"Take back the voice","http://www.thecrazyworldofpolitics.blogspot.com","http://thecrazyworldofpolitics.blogspot.com/atom.xml",null,
"North of the River","http://catherinedawson.typepad.com/north/","http://catherinedawson.typepad.com/north/rss.xml",null,
"Miranda Grell","http://www.mirandagrell.com/","http://www.mirandagrell.com/?feed=rss2","http://www.mirandagrell.com/?feed=comments-rss2",
"Khevyn Limbajee","http://www.labour.org.uk/blog/index.php?id=12","http://www.labour.org.uk/blog/index.php?id=12&type=100",null,
"Claire Hamilton","http://www.labour.org.uk/blog/index.php?id=1","http://www.labour.org.uk/blog/index.php?id=1&type=100",null,
"Seb's Blag","http://sebcarroll.blogspot.com/","http://sebcarroll.blogspot.com/atom.xml",null,
"Dlogs","http://dan-miller.co.uk/dlogs/","http://www.dan-miller.co.uk/dlogs/?feed=rss2","http://www.dan-miller.co.uk/dlogs/?feed=comments-rss2",
"Softy Lefty Catchy Monkey","http://softleft.blogspot.com","http://softleft.blogspot.com/atom.xml",null,
"Skuds' Sister's Brother","http://skuds.co.uk/","http://skuds.co.uk/index.php/?feed=rss2","http://skuds.co.uk/index.php/?feed=comments-rss2",
"John Tyrrell Blogs","http://www.johntyrrell.co.uk","http://johntyrrell.co.uk/atom.xml",null,
"New Jerusalem","http://moretomethodism.blogspot.com/","http://moretomethodism.blogspot.com/atom.xml",null,
"Waspish","http://www.etribes.com/waspish","http://www.etribes.com/waspish/feed","",
"David Miliband","http://www.odpm.gov.uk/cs/blogs/ministerial_blog/default.aspx","http://www.odpm.gov.uk/cs/blogs/ministerial_blog/rss.aspx",null,
"Jon Worth Euroblog","http://blog.jonworth.eu/","http://feeds.feedburner.com/jonworth_Fixed",null,
"Lewis Atkinson","http://www.lewisatkinson.co.uk","",null,
"Barbara Portwin","http://barbaraportwin.blogspot.com/","http://barbaraportwin.blogspot.com/atom.xml",null,
"One Perfect Rose...?","http://oneperfectrose.net/","http://oneperfectrose.net/?feed=rss2","http://oneperfectrose.net/?feed=comments-rss2",
"Julian Mott","http://uk.blog.360.yahoo.com/blog-kBDesRg7bqpUoVjIYZvpSs3reg--?cq=1","http://uk.blog.360.yahoo.com/rss-kBDesRg7bqpUoVjIYZvpSs3reg--?cq=1",null,
"Ian Robertson","http://www.readmyday.co.uk/ianr","http://www.readmyday.co.uk/ianr/rss/ianr.rss",null,
"EC1 Cruise Control","http://ec1cruisecontrol.blogspot.com/","http://ec1cruisecontrol.blogspot.com/atom.xml",null,
"Dear Diary","http://lithgo.wordpress.com","http://lithgo.wordpress.com/feed/",null,
"Eynesbury Rose","http://eynesburyrose.blogspot.com/","http://eynesburyrose.blogspot.com/atom.xml",null,
"Pat McFadden","http://www.patmcfadden.com/cgi-bin/cm.cgi?cmrid=2&cmpid=33","",null,
"Oxford Labour Party","http://www.oxfordlabour.org.uk/","http://www.oxfordlabour.org.uk/feed/",null,
"Mildmay Labour","http://www.labour4mildmay.blogspot.com","http://labour4mildmay.blogspot.com/atom.xml",null,
"Avondale Ward","http://www.cllrcorazzo.blogspot.com","http://cllrcorazzo.blogspot.com/atom.xml",null,
"Thirsk and Malton","http://www.thirskandmalton.blogspot.com","http://www.thirskandmalton.blogspot.com/atom.xml",null,
"Dan Jackson","http://www.dan-jackson.co.uk/news.html","",null,
"Living Ghosts Endurance Challenge","http://livingghostsendurancechallenge.blogspot.com","http://livingghostsendurancechallenge.blogspot.com/atom.xml",null,
"Blogging4Merton","http://whelton.blogspot.com","http://whelton.blogspot.com/atom.xml",null,
"Keep Labour In","http://keeplabourin.blogspot.com/","http://keeplabourin.blogspot.com/atom.xml",null,
"The Wonderful World Of Lola","http://thewonderfulworldoflola.blogspot.com/","http://thewonderfulworldoflola.blogspot.com/atom.xml",null,
"schmoo on the run","http://www.schmoontherun.blogspot.com","http://feeds.feedburner.com/schmooontherun",null,
"Rob Shorrock","http://www.robshorrock.me.uk","http://www.robshorrock.me.uk/rss/RobShorrock.rss",null,
"Chirk Labour Party","http://chirklabour.blogspot.com","http://chirklabour.blogspot.com/atom.xml",null,
"Paul Bell","http://www.paulbell.org/","http://feeds.feedburner.com/PaulBell_Fixed",null,
"janestheone","http://www.janestheones.blogspot.com/","http://janestheones.blogspot.com/atom.xml",null,
"The Poor Mouth","http://thepoormouth.blogspot.com/","http://thepoormouth.blogspot.com/atom.xml",null,
"Captain Smoo","http://captainsmoo.blogspot.com/","http://captainsmoo.blogspot.com/atom.xml",null,
"Warren's political blog","http://warrenmorgan.blogspot.com/","http://warrenmorgan.blogspot.com/atom.xml",null,
"Chris Gale's Weblog","http://chriswgale.typepad.com/chris_gale/","http://chriswgale.typepad.com/chris_gale/rss.xml",null,
"Hoxton Councillors","http://hoxtoncouncillors.blogspot.com/","http://hoxtoncouncillors.blogspot.com/atom.xml",null,
"The Biggs Report","http://thebiggsreport.blogspot.com/","http://thebiggsreport.blogspot.com/atom.xml",null,
"Labour Humanists","http://humanistsforlabour.typepad.com/","http://humanistsforlabour.typepad.com/labour_humanists/rss.xml",null,
"Peter John","http://cllrpeterjohn.blogspot.com/","http://cllrpeterjohn.blogspot.com/atom.xml",null,
"Bread and Circuses","http://www.20six.co.uk/breadandcircuses","http://www.20six.co.uk/rss/breadandcircuses.rss",null,
"Parks Ward Swindon - What have I been doing?","http://www.blog.co.uk/cllrfay","http://www.blog.co.uk/srv/xml/xmlfeed.php?blog=133789&mode=rss2.0","http://www.blog.co.uk/xmlsrv/rss2.comments.php?blog=133789",
"Ephems of BLB","http://www.barder.com/ephems/","http://www.barder.com/ephems/feed/",null,
"Someday I Will Treat You Good","http://andrewkbrown.wordpress.com/","http://andrewkbrown.wordpress.com/feed/",null,
"Reading From Rhoderick","http://readingfromrhoderick.blogspot.com","",null,
"The Ministry of Agitation and Propaganda","http://agitpropcentral.blogspot.com/","http://agitpropcentral.blogspot.com/atom.xml",null,
"John Paschoud","http://www.lewisham.org.uk/john/LBL/weblog/","http://www.lewisham.org.uk/john/LBL/weblog/atom.xml",null */
	};
}
