/*
 * RecommendationHandler.java
 *
 * Created on 19 June 2006, 21:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.recommend;

import com.hiatus.USQL_Utils;
import com.hiatus.UText;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.*;
import javax.sql.DataSource;
import org.bloggers4labour.sql.*;
import org.apache.log4j.Logger;

/**
 *
 * @author andrewre
 */
public class RecommendationHandler
{
	private DataSource		m_DataSource;

	private static Logger		s_Logger = Logger.getLogger("Main");

	/********************************************************************
	********************************************************************/
	public RecommendationHandler( DataSource inDS)
	{
		m_DataSource = inDS;
	}

	/********************************************************************
	********************************************************************/
	public boolean handleRequest( String inSessionID, String inURL)
	{
		DataSourceConnection	theConnectionObject = null;

		try
		{
			theConnectionObject = new DataSourceConnection(m_DataSource);
			if (theConnectionObject.Connect())
			{
				Statement	theS = null;

				try
				{
					theS = theConnectionObject.createStatement();

					return _handleRequest( theS, inSessionID, inURL);
				}
				catch (Exception e)
				{
					s_Logger.error("creating statement", e);
				}
				finally
				{
					USQL_Utils.closeStatementCatch(theS);
				}
			}
			else
			{
				s_Logger.warn("Cannot connect!");
			}
		}
		catch (Exception err)
		{
			s_Logger.error("???", err);
		}
		finally
		{
			if ( theConnectionObject != null)
			{
				theConnectionObject.CloseDown();
				theConnectionObject = null;
			}
		}

		return false;
	}

	/********************************************************************
	********************************************************************/
	private boolean _handleRequest( Statement inStatement, String inSessionID, String inURL) throws SQLException
	{
		if (UText.isNullOrBlank(inURL))
		{
			return false;
		}

		////////////////////////////////////////////////////////////////

		CharSequence	theAdjustedURL = USQL_Utils.getQuoted( inURL.trim().toLowerCase() );
		ResultSet	theRS = inStatement.executeQuery("SELECT 1 FROM recommendations R, recommendedURLs U WHERE R.url_recno=U.recno AND U.url=" + theAdjustedURL + " AND R.session_id=" + USQL_Utils.getQuoted(inSessionID) + " AND ( NOW() - R.date) < 86400");

		if (theRS.next())	// Session user (!) has already recommended this URL in the past day - reject
		{
			theRS.close();
			return false;
		}

		////////////////////////////////////////////////////////////////

		theRS = inStatement.executeQuery("SELECT recno FROM recommendedURLs WHERE url=" + theAdjustedURL);

		long	urlRecno;

		if (theRS.next())	// URL is already in DB, use recno
		{
			urlRecno = theRS.getLong(1);
			theRS.close();
		}
		else
		{
			inStatement.executeUpdate("INSERT INTO recommendedURLs (url) VALUES (" + theAdjustedURL + ")");

			theRS = inStatement.executeQuery("SELECT LAST_INSERT_ID()");
			if (theRS.next())	// phew...
			{
				urlRecno = theRS.getLong(1);
				theRS.close();
			}
			else
			{
				s_Logger.error("Could not get recno of new URL!!!");
				theRS.close();
				return false;
			}
		}

		////////////////////////////////////////////////////////////////

		if ( inStatement.executeUpdate("INSERT INTO recommendations (session_id, url_recno, date) VALUES (" + inSessionID + "," + urlRecno + ", NOW())") == 1)
		{
			return true;
		}

		s_Logger.error("Failed to insert recommendation record!!");
		return false;
	}

	private static String[]	testStrings = {
"http://123.writeboard.com/a797eb2b85fb98580",
"http://about-whose-news.blogspot.com/",
"http://adloyada.typepad.com/adloyada/2006/05/fighting_the_na.html",
"http://agitpropcentral.blogspot.com/",
"http://allanwilsonmsp.com/v-web/b2/",
"http://andrewkbrown.wordpress.com/",
"http://anthonymckeown.info/blog.html",
"http://assistantbrighton.blogspot.com/",
"http://bagrec.livejournal.com/175757.html",
"http://barbaraportwin.blogspot.com/",
"http://battersea-mp.org.uk/",
"http://benjamincomments.wordpress.com/",
"http://bigmacthered.blogspot.com/",
"http://bishophill.blogspot.com/",
"http://blacktriangle.org/blog",
"http://blairitebob.blogdrive.com/",
"http://blimpish.typepad.com/",
"http://blog.hakmao.com/",
"http://blog.jonworth.eu/",
"http://blog.jonworth.eu/?p=247",
"http://blog.stodge.org/",
"http://blogs.guardian.co.uk/election2005/",
"http://blogs.guardian.co.uk/news/archives/cat_uk_politics.html",
"http://blogshares.com/blogs.php?blog=http%3A%2F%2Fwww.bloggers4labour.org%2F&user=15906",
"http://bowblog.com/",
"http://bratiaith.blogspot.com/",
"http://brightonregencylabourparty.blogspot.com/",
"http://brightonregencylabourparty.blogspot.com/2006/06/did-jesus-die-on-cross.html",
"http://britishspin.blogspot.com/",
"http://bsscworld.blogspot.com/",
"http://callyskitchen.blogspot.com/",
"http://captainsmoo.blogspot.com/",
"http://catherinedawson.typepad.com/north/",
"http://chickyog.blogspot.com/",
"http://chirklabour.blogspot.com/",
"http://chriswgale.typepad.com/chris_gale/",
"http://clivesoleymp.typepad.com/",
"http://cllrpeterjohn.blogspot.com/",
"http://cloud-in-trousers.blogspot.com/",
"http://clustrmaps.com/counter/maps.php?url=http://www.bloggers4labour.org",
"http://coedpoeth-ward.blogspot.com/",
"http://councillorbobpiper.blogspot.com/",
"http://councillorbobpiper.blogspot.com/2006/05/collected-thoughts-of-steve-freedom.html",
"http://councillorbobpiper.blogspot.com/2006/05/nazi-blogger.html",
"http://councillorsmith.blogspot.com/",
"http://cramlingtonvillagecouncillor.blogspot.com/",
"http://dan-miller.co.uk/dlogs/",
"http://delbertwilkins.blogspot.com/",
"http://devilskitchen.blogspot.com/",
"http://dirtyleftie.blogspot.com/",
"http://dm-andy.blogspot.com/",
"http://drinksoakedtrotsforwar.blogspot.com/",
"http://eastclifframsgate.blogspot.com/",
"http://ec1cruisecontrol.blogspot.com/",
"http://elephunt.blogspot.com/",
"http://en.wikipedia.org/wiki/",
"http://en.wikipedia.org/wiki/Category:1933_deaths",
"http://en.wikipedia.org/wiki/Clara_Zetkin",
"http://en.wikipedia.org/wiki/John_Stuart_Mill",
"http://en.wikipedia.org/wiki/June_1",
"http://en.wikipedia.org/wiki/June_11",
"http://en.wikipedia.org/wiki/June_13",
"http://en.wikipedia.org/wiki/June_2",
"http://en.wikipedia.org/wiki/June_5",
"http://en.wikipedia.org/wiki/June_6",
"http://en.wikipedia.org/wiki/May_29",
"http://en.wikipedia.org/wiki/May_30",
"http://en.wikipedia.org/wiki/May_31",
"http://en.wikipedia.org/wiki/Non-alignment",
"http://en.wikipedia.org/wiki/The_Bash_Street_Kids",
"http://english-31473156356.spampoison.com/",
"http://erictheunred.blogspot.com/",
"http://eustonmanifesto.org/",
"http://everton.blogspot.com/",
"http://eynesburyrose.blogspot.com/",
"http://feeds.feedburner.com/Bloggers4Labour",
"http://feedvalidator.org/check.cgi?url=http://feeds.feedburner.com/Bloggers4Labour",
"http://fiskingcentral.typepad.com/",
"http://forcefulandmoderate.blogspot.com/",
"http://fourthterm.net/",
"http://freethoughtsofaman.blogspot.com/",
"http://geofflumley.blogspot.com/",
"http://hattieajderian.typepad.com/",
"http://hoxtoncouncillors.blogspot.com/",
"http://huggysmind.blogspot.com/",
"http://humanistsforlabour.typepad.com/",
"http://hurryupharry.bloghouse.net/",
"http://hurryupharry.bloghouse.net/archives/2006/06/11/blogging_with_alastair.php",
"http://hurryupharry.bloghouse.net/archives/2006/06/19/beware_the_tentacles.php",
"http://iaindale.blogspot.com/2006/06/my-top-political-blogs.html",
"http://ibanda.blogs.com/",
"http://johnwestjourno.blogspot.com/",
"http://jonathanderbyshire.typepad.com/",
"http://jtothak.blogspot.com/",
"http://juliemorgan.blogdrive.com/",
"http://juliemorgan.typepad.com/",
"http://keeplabourin.blogspot.com/",
"http://kerroncross.blogspot.com/",
"http://labour4crystalpalace.blogspot.com/",
"http://leabridgelife.blogspot.com/",
"http://leightonandrews.typepad.com/leighton_andrews_am/",
"http://letsbesensible.blogspot.com/",
"http://libdems.fourthterm.net/",
"http://liberoblog.com/",
"http://libsoc.blogspot.com/",
"http://lithgo.wordpress.com/",
"http://livingghostsendurancechallenge.blogspot.com/",
"http://lukeakehurst.blogspot.com/",
"http://madoccouncillor.blogspot.com/",
"http://marchforfreeexpression.blogspot.com/",
"http://marxist-org-uk.blogspot.com/",
"http://meandophelia.blogspot.com/",
"http://mike-ion.blogspot.com/",
"http://modies.blogspot.com/",
"http://moretomethodism.blogspot.com/",
"http://neilmacdonald.typepad.com/",
"http://nethesi.blogspot.com/",
"http://nevertrustahippy.blogspot.com/",
"http://nevertrustahippy.blogspot.com/2006/06/quick-question.html",
"http://newerlabour.blogspot.com/2006/05/compass-beef.html",
"http://newlinks.blogspot.com/2004/06/adding-to-blogger.html",
"http://news.bbc.co.uk/1/hi/england/5033508.stm",
"http://news.bbc.co.uk/1/hi/uk_politics/5064360.stm",
"http://news.bbc.co.uk/onthisday/hi/dates/stories/june/1/default.stm",
"http://news.bbc.co.uk/onthisday/hi/dates/stories/june/11/default.stm",
"http://news.bbc.co.uk/onthisday/hi/dates/stories/june/13/default.stm",
"http://news.bbc.co.uk/onthisday/hi/dates/stories/june/2/default.stm",
"http://news.bbc.co.uk/onthisday/hi/dates/stories/june/5/default.stm",
"http://news.bbc.co.uk/onthisday/hi/dates/stories/june/6/default.stm",
"http://news.bbc.co.uk/onthisday/hi/dates/stories/may/29/default.stm",
"http://news.bbc.co.uk/onthisday/hi/dates/stories/may/30/default.stm",
"http://news.bbc.co.uk/onthisday/hi/dates/stories/may/31/default.stm",
"http://normblog.typepad.com/normblog/",
"http://normblog.typepad.com/normblog/2006/05/platform_twelve.html",
"http://normblog.typepad.com/normblog/2006/06/no_one_left_beh.html",
"http://northyorkshiretimes.blogspot.com/",
"http://oliverkamm.typepad.com/",
"http://oliverkamm.typepad.com/blog/2006/06/world_cup_fever.html",
"http://oneperfectrose.net/",
"http://orlikreport.blogspot.com/",
"http://paulburgin.blogspot.com/2006/05/interviewing-brian-haw.html",
"http://paulburgin.blogspot.com/2006/06/bloggers-social-event.html",
"http://politicalhackuk.blogspot.com/",
"http://politicsforbeginners.blogspot.com/",
"http://popsensible.modblog.com/",
"http://pubphilosopher.blogs.com/",
"http://pubphilosopher.blogs.com/pub_philosopher/2006/05/multicultural_p.html",
"http://redflagboy.blogspot.com/",
"http://reunited.jrsconsultants-uk.com/",
"http://rhodonpublicaffairs.blogspot.com/",
"http://robnewman.typepad.com/",
"http://rodneymcaree.blogspot.com/",
"http://ruabonlabourcouncillor.blogspot.com/",
"http://rubberring.blogspot.com/",
"http://rullsenbergrules.blogspot.com/",
"http://samizdata.net/blog/",
"http://search.blogger.com/",
"http://sebcarroll.blogspot.com/",
"http://skipper59.blogspot.com/",
"http://skuds.blogspot.com/",
"http://skuds.co.uk/",
"http://smalltownscribble.blogspot.com/",
"http://smeeble.blogspot.com/",
"http://snowflake5.blogspot.com/",
"http://softleft.blogspot.com/",
"http://sophiehowe.blogs.com/",
"http://spiritof1976.livejournal.com/",
"http://stokelabourgroup.blogspot.com/",
"http://stumblingandmumbling.typepad.com/",
"http://stumblingandmumbling.typepad.com/stumbling_and_mumbling/2006/05/mills_vision.html",
"http://stumblingandmumbling.typepad.com/stumbling_and_mumbling/2006/05/tax_credits_vs_.html",
"http://stumblingandmumbling.typepad.com/stumbling_and_mumbling/2006/06/stupid_and_stup.html",
"http://tamanou.blogspot.com/",
"http://thebiggsreport.blogspot.com/",
"http://theboabie.blogspot.com/",
"http://thegrammaticalpuss.blogspot.com/",
"http://thekupfers.typepad.com/tothepoint/",
"http://thepoormouth.blogspot.com/",
"http://thepoormouth.blogspot.com/2006/05/all-roads-should-lead-to-euston.html",
"http://thesilenthunter.blogspot.com/",
"http://thethimble.blogspot.com/",
"http://thewonderfulworldoflola.blogspot.com/",
"http://thirdavenue.typepad.com/third_avenue/",
"http://timesonline.typepad.com/david_aaronovitch/",
"http://timrollpickering.blogspot.com/",
"http://timrollpickering.blogspot.com/2006/06/bloggers-social-last-night.html",
"http://tothetootingstation.typepad.com/blog/",
"http://treesforlabour.wordpress.com/",
"http://uk.blog.360.yahoo.com/blog-kBDesRg7bqpUoVjIYZvpSs3reg--?cq=1",
"http://ukcommentators.blogspot.com/",
"http://users.ox.ac.uk/~magd1368/weblog/2006_05_01_archive.html#114885592240451020",
"http://users.ox.ac.uk/~magd1368/weblog/blogger.html",
"http://wandwaver.co.uk/blog/",
"http://warrenmorgan.blogspot.com/",
"http://whelton.blogspot.com/",
"http://wongablog.co.uk/2006/06/01/blogging-is-the-new-fishing-or-something/",
"http://wongablog.co.uk/2006/06/03/pauls-meetup/",
"http://worldwarfour.blogspot.com/",
"http://www.186mph.blogspot.com/",
"http://www.20six.co.uk/bodimeade",
"http://www.20six.co.uk/breadandcircuses",
"http://www.20six.co.uk/Cllr_Andrew_Brown",
"http://www.20six.co.uk/johnhumphries",
"http://www.20six.co.uk/karenmarshall",
"http://www.20six.co.uk/kingstanding",
"http://www.20six.co.uk/middletonpark",
"http://www.20six.co.uk/Westminster",
"http://www.afarfetchedresolution.com/",
"http://www.againstcorruption.org/BriefingsItem.asp?id=12824",
"http://www.alun-clwydwest.blogspot.com/",
"http://www.amazon.com/exec/obidos/ASIN/000610939X/ref=nosim/librarything-20",
"http://www.amazon.com/exec/obidos/ASIN/0753817578/ref=nosim/librarything-20",
"http://www.amazon.com/exec/obidos/ASIN/1843171082/ref=nosim/librarything-20",
"http://www.andyreedmp.org.uk/",
"http://www.annecampbell.org.uk/weblog/",
"http://www.antoniabance.org.uk/",
"http://www.apple.com/",
"http://www.assemblylabour.blogspot.com/",
"http://www.austinmitchell.org/",
"http://www.barder.com/ephems/",
"http://www.barrysbeef.blogspot.com/",
"http://www.beerintheevening.com/pubs/s/81/813/Coal_Hole/Strand",
"http://www.billyhayes.co.uk/",
"http://www.blog.co.uk/cllrfay",
"http://www.blogarama.com/",
"http://www.blogcatalog.com/",
"http://www.blogger.com/",
"http://www.blogger.com/email-post.g?blogID=10883926&postID=114826294491898690",
"http://www.blogger.com/email-post.g?blogID=10883926&postID=114884029734917637",
"http://www.blogger.com/email-post.g?blogID=10883926&postID=114884045968148512",
"http://www.blogger.com/email-post.g?blogID=10883926&postID=114890304227769358",
"http://www.blogger.com/email-post.g?blogID=10883926&postID=114892227172222976",
"http://www.blogger.com/email-post.g?blogID=10883926&postID=114899249342078671",
"http://www.blogger.com/email-post.g?blogID=10883926&postID=114910582922506390",
"http://www.blogger.com/email-post.g?blogID=10883926&postID=114910966963872240",
"http://www.blogger.com/email-post.g?blogID=10883926&postID=114914077482811460",
"http://www.blogger.com/email-post.g?blogID=10883926&postID=114954505729912097",
"http://www.blogger.com/email-post.g?blogID=10883926&postID=114962538699524126",
"http://www.blogger.com/email-post.g?blogID=10883926&postID=115004183987300966",
"http://www.blogger.com/email-post.g?blogID=10883926&postID=115004487861336545",
"http://www.blogger.com/email-post.g?blogID=10883926&postID=115005341940646356",
"http://www.blogger.com/email-post.g?blogID=10883926&postID=115020609951645285",
"http://www.blogger.com/feeds/10883926/posts/full",
"http://www.blogger.com/post-edit.g?blogID=10883926&postID=114826294491898690&quickEdit=true",
"http://www.blogger.com/post-edit.g?blogID=10883926&postID=114884029734917637&quickEdit=true",
"http://www.blogger.com/post-edit.g?blogID=10883926&postID=114884045968148512&quickEdit=true",
"http://www.blogger.com/post-edit.g?blogID=10883926&postID=114890304227769358&quickEdit=true",
"http://www.blogger.com/post-edit.g?blogID=10883926&postID=114892227172222976&quickEdit=true",
"http://www.blogger.com/post-edit.g?blogID=10883926&postID=114899249342078671&quickEdit=true",
"http://www.blogger.com/post-edit.g?blogID=10883926&postID=114910582922506390&quickEdit=true",
"http://www.blogger.com/post-edit.g?blogID=10883926&postID=114910966963872240&quickEdit=true",
"http://www.blogger.com/post-edit.g?blogID=10883926&postID=114914077482811460&quickEdit=true",
"http://www.blogger.com/post-edit.g?blogID=10883926&postID=114954505729912097&quickEdit=true",
"http://www.blogger.com/post-edit.g?blogID=10883926&postID=114962538699524126&quickEdit=true",
"http://www.blogger.com/post-edit.g?blogID=10883926&postID=115004183987300966&quickEdit=true",
"http://www.blogger.com/post-edit.g?blogID=10883926&postID=115004487861336545&quickEdit=true",
"http://www.blogger.com/post-edit.g?blogID=10883926&postID=115005341940646356&quickEdit=true",
"http://www.blogger.com/post-edit.g?blogID=10883926&postID=115020609951645285&quickEdit=true",
"http://www.blogger.com/redirect/next_blog.pyra?navBar=true",
"http://www.blogger.com/rsd.g?blogID=10883926",
"http://www.bloggers4labour.org/",
"http://www.bloggers4labour.org/2006/05/alternative-view.jsp",
"http://www.bloggers4labour.org/2006/05/alternative-view.jsp#comments",
"http://www.bloggers4labour.org/2006/05/bloggers4labour-forums.jsp",
"http://www.bloggers4labour.org/2006/05/brian-haw-compass.jsp",
"http://www.bloggers4labour.org/2006/05/brian-haw-compass.jsp#comments",
"http://www.bloggers4labour.org/2006/05/forums-reminder.jsp",
"http://www.bloggers4labour.org/2006/05/forums-reminder.jsp#comments",
"http://www.bloggers4labour.org/2006/05/racist-holocaust-denier-nutter.jsp",
"http://www.bloggers4labour.org/2006/05/racist-holocaust-denier-nutter.jsp#comments",
"http://www.bloggers4labour.org/2006/05/smiffy.jsp",
"http://www.bloggers4labour.org/2006/05/smiffy.jsp#comments",
"http://www.bloggers4labour.org/2006/05/world-cup-fever.jsp",
"http://www.bloggers4labour.org/2006/05/world-cup-fever.jsp#comments",
"http://www.bloggers4labour.org/2006/06/belated-js-mill-post.jsp",
"http://www.bloggers4labour.org/2006/06/belated-js-mill-post.jsp#comments",
"http://www.bloggers4labour.org/2006/06/bloggers-unite.jsp",
"http://www.bloggers4labour.org/2006/06/bloggers-unite.jsp#comments",
"http://www.bloggers4labour.org/2006/06/custom-feeds.jsp",
"http://www.bloggers4labour.org/2006/06/custom-feeds.jsp#comments",
"http://www.bloggers4labour.org/2006/06/economist.jsp",
"http://www.bloggers4labour.org/2006/06/economist.jsp#comments",
"http://www.bloggers4labour.org/2006/06/link-log-5.jsp",
"http://www.bloggers4labour.org/2006/06/link-log-5.jsp#comments",
"http://www.bloggers4labour.org/2006/06/pardon-us-for-breathing.jsp",
"http://www.bloggers4labour.org/2006/06/pardon-us-for-breathing.jsp#comments",
"http://www.bloggers4labour.org/2006/06/putting-it-down.jsp",
"http://www.bloggers4labour.org/2006/06/putting-it-down.jsp#comments",
"http://www.bloggers4labour.org/2006/06/top-political-blogs.jsp",
"http://www.bloggers4labour.org/2006/06/top-political-blogs.jsp#comments",
"http://www.bloggers4labour.org/2006/06/world-cup-blogs.jsp",
"http://www.bloggers4labour.org/2006/06/world-cup-blogs.jsp#comments",
"http://www.bloggers4labour.org/archive/2005_02_01_archive.jsp",
"http://www.bloggers4labour.org/archive/2005_03_01_archive.jsp",
"http://www.bloggers4labour.org/archive/2005_04_01_archive.jsp",
"http://www.bloggers4labour.org/archive/2005_05_01_archive.jsp",
"http://www.bloggers4labour.org/archive/2005_06_01_archive.jsp",
"http://www.bloggers4labour.org/archive/2005_07_01_archive.jsp",
"http://www.bloggers4labour.org/archive/2005_08_01_archive.jsp",
"http://www.bloggers4labour.org/archive/2005_09_01_archive.jsp",
"http://www.bloggers4labour.org/archive/2005_10_01_archive.jsp",
"http://www.bloggers4labour.org/archive/2005_11_01_archive.jsp",
"http://www.bloggers4labour.org/archive/2005_12_01_archive.jsp",
"http://www.bloggers4labour.org/archive/2006_01_01_archive.jsp",
"http://www.bloggers4labour.org/archive/2006_02_01_archive.jsp",
"http://www.bloggers4labour.org/archive/2006_03_01_archive.jsp",
"http://www.bloggers4labour.org/archive/2006_04_01_archive.jsp",
"http://www.bloggers4labour.org/archive/2006_05_01_archive.jsp",
"http://www.bloggers4labour.org/archive/2006_06_01_archive.jsp",
"http://www.bloggers4labour.org/atom.xml",
"http://www.bloggers4labour.org/comments.jsp",
"http://www.bloggers4labour.org/contact.jsp",
"http://www.bloggers4labour.org/create.jsp",
"http://www.bloggers4labour.org/css/b4l_2006.css",
"http://www.bloggers4labour.org/download/stationary_state.pdf",
"http://www.bloggers4labour.org/DSW.jsp",
"http://www.bloggers4labour.org/forum/index.php",
"http://www.bloggers4labour.org/links2.jsp",
"http://www.bloggers4labour.org/login.jsp",
"http://www.bloggers4labour.org/members_feeds.jsp",
"http://www.bloggers4labour.org/new.jsp",
"http://www.bloggers4labour.org/recent_posts.jsp",
"http://www.bloggers4labour.org/rss.xml",
"http://www.bloggers4labour.org/servlet/b4l_main?opml=true",
"http://www.bloggers4labour.org/servlet/b4l_main?rss=true",
"http://www.bloggers4labour.org/stats.jsp",
"http://www.bloggers4labour.org/tags.jsp",
"http://www.bloglines.com/",
"http://www.bloglines.com/sub/http://feeds.feedburner.com/Bloggers4Labour",
"http://www.blogtopsites.com/politics/",
"http://www.bookstoiraq.org.uk/",
"http://www.carlroper.blogspot.com/",
"http://www.celiabarlowmp.com/",
"http://www.channel4.com/news/microsites/E/election2005_blogs/dobson_blog.html",
"http://www.clivedavis-online.com/",
"http://www.cllrcorazzo.blogspot.com/",
"http://www.compassonline.org.uk/",
"http://www.compassonline.org.uk/conference/",
"http://www.corbett-euro.demon.co.uk/blog/",
"http://www.councillor.info/_Councils/sandwell/m1546/SCSmith4.JPG?Alias=simonsmith&TabId=0",
"http://www.cricketworldcup.com/news.aspx",
"http://www.cstihlermep.com/ViewPage.cfm?Page=8894",
"http://www.dadblog.co.uk/",
"http://www.dan-jackson.co.uk/news.html",
"http://www.davosnewbies.com/",
"http://www.davosnewbies.com/2006/05/30/politics-at-bloggercon/",
"http://www.deepcallstodeep.sonafide.com/",
"http://www.demosgreenhouse.co.uk/",
"http://www.derekwyatt.co.uk/pages/blog.asp?i_PageID=1817",
"http://www.dirtyleftie.co.uk/",
"http://www.east-acton.com/",
"http://www.economist.com/",
"http://www.electthelords.org.uk/",
"http://www.ericlee.info/",
"http://www.etribes.com/waspish",
"http://www.fiona-colley.net/",
"http://www.frappr.com/bloggers4labour",
"http://www.gentheoryrubbish.com/",
"http://www.grangetownlabour.blogspot.com/",
"http://www.grumpyoldben.blogspot.com/",
"http://www.homeusers.prestel.co.uk/grayling/beano/smiffy.gif",
"http://www.hulc.org/blog/",
"http://www.iaindale.blogspot.com/",
"http://www.imdb.com/OnThisDay?day=1&month=June",
"http://www.imdb.com/OnThisDay?day=11&month=June",
"http://www.imdb.com/OnThisDay?day=13&month=June",
"http://www.imdb.com/OnThisDay?day=2&month=June",
"http://www.imdb.com/OnThisDay?day=29&month=May",
"http://www.imdb.com/OnThisDay?day=30&month=May",
"http://www.imdb.com/OnThisDay?day=31&month=May",
"http://www.imdb.com/OnThisDay?day=5&month=June",
"http://www.imdb.com/OnThisDay?day=6&month=June",
"http://www.islingtonlabour.org.uk/",
"http://www.jamiebolden.com/",
"http://www.janestheones.blogspot.com/",
"http://www.johninnit.co.uk/",
"http://www.johntyrrell.co.uk/",
"http://www.jonathanbishop.org.uk/weblog/",
"http://www.josalmon.co.uk/",
"http://www.keep-the-faith.org/",
"http://www.labour.org.uk/blog/index.php?id=1",
"http://www.labour.org.uk/blog/index.php?id=106",
"http://www.labour.org.uk/blog/index.php?id=12",
"http://www.labour.org.uk/blog/index.php?id=90",
"http://www.labour4mildmay.blogspot.com/",
"http://www.labourfriendsofiraq.org.uk/",
"http://www.labourinlondon.org.uk/blog",
"http://www.labourstart.org/",
"http://www.labourwandsworth.org.uk/latchmere/blog/index.htm",
"http://www.labourwandsworth.org.uk/roehampton/blog/",
"http://www.labourwandsworth.org.uk/tooting/blog/",
"http://www.laws.sandwell.gov.uk/ccm/content/corporateservices/legalanddemocratic/election-2006/great-bridge-ward---results---elections-2006.en",
"http://www.leegregory.typepad.com/",
"http://www.leightonandrews.blogspot.com/",
"http://www.leightonandrews.typepad.com/rhondda_today/",
"http://www.lewisatkinson.co.uk/",
"http://www.lewisham.org.uk/john/LBL/weblog/",
"http://www.libertycentral.org.uk/",
"http://www.librarything.com/",
"http://www.librarything.com/author/christieagatha",
"http://www.librarything.com/author/cooperrobin",
"http://www.librarything.com/author/picardliza",
"http://www.librarything.com/catalog.php?book=664011",
"http://www.librarything.com/catalog.php?book=666196",
"http://www.librarything.com/catalog.php?book=669922",
"http://www.librarything.com/catalog/hiatusuk",
"http://www.livejournal.com/users/bagrec/",
"http://www.madmusingsof.me.uk/weblog/",
"http://www.marlynsblog.org.uk/",
"http://www.mediaprof.typepad.com/",
"http://www.michaelhowardmp.co.uk/",
"http://www.ministryoftruth.org.uk/",
"http://www.ministryoftruth.org.uk/2006/05/30/the-name%e2%80%99s-freedom-%e2%80%93-steve-freedom/",
"http://www.ministryoftruth.org.uk/2006/05/31/tokenism-rules-okay/",
"http://www.ministryoftruth.org.uk/2006/06/05/and-still-no-sign-of-the-amazing-dancing-bear/",
"http://www.mirandagrell.com/",
"http://www.newerlabour.blogspot.com/",
"http://www.nickcohen.net/",
"http://www.odpm.gov.uk/cs/blogs/ministerial_blog/default.aspx",
"http://www.omarsalem.com/",
"http://www.organisingacademy.blogspot.com/",
"http://www.owen.org/blog",
"http://www.oxfordlabour.org.uk/",
"http://www.oxyacetylene.blogspot.com/",
"http://www.patmcfadden.com/cgi-bin/cm.cgi?cmrid=2&cmpid=33",
"http://www.paulbell.org/",
"http://www.paulburgin.blogspot.com/",
"http://www.paulnowak.blogspot.com/",
"http://www.pdet.blogspot.com/",
"http://www.pendre.blogspot.com/",
"http://www.peteashton.com/linkfarm/06/06/01/paul_daniels_ebay_transaction.html",
"http://www.petemorton.blogspot.com/",
"http://www.philbateman.com/",
"http://www.philippelegrain.com/legrain/",
"http://www.pootergeek.com/",
"http://www.pootergeek.com/?p=2191",
"http://www.pootergeek.com/?p=2250",
"http://www.progressives.org.uk/",
"http://www.readmyday.co.uk/ianr",
"http://www.recessmonkey.com/",
"http://www.ridiculouspolitics.blogspot.com/",
"http://www.robshorrock.me.uk/",
"http://www.rogerdarlington.co.uk/nighthawk/",
"http://www.sadiqkhan.org.uk/blog/sadiqblog.htm",
"http://www.schmoontherun.blogspot.com/",
"http://www.shaunwoodward.com/",
"http://www.snedds.co.uk/",
"http://www.statcounter.com/",
"http://www.stephennewton.com/",
"http://www.stephenpollard.net/",
"http://www.stuartssoapbox.com/",
"http://www.technorati.com/search/http%3A%2F%2Fmike-ion.blogspot.com%2F",
"http://www.technorati.com/search/http%3A%2F%2Fnevertrustahippy.blogspot.com%2F2006%2F06%2Fquick-question.html",
"http://www.thecrazyworldofpolitics.blogspot.com/",
"http://www.thesharpener.net/",
"http://www.thesharpener.net/2006/05/31/educational-selection-in-an-age-of-meritocracy/",
"http://www.theskepticsguide.org/",
"http://www.theyworkforyou.com/",
"http://www.theyworkforyou.com/mp/",
"http://www.theyworkforyou.com/search/",
"http://www.thirskandmalton.blogspot.com/",
"http://www.tom-watson.co.uk/",
"http://www.truthlaidbear.com/ecosystem.php",
"http://www.truthlaidbear.com/showdetails.php?host=http://bloggers4labour.org",
"http://www.urltrends.com/",
"http://www.wellslabour.org/blog.php?",
"http://www.westminstervillage.co.uk/",
"http://www.whatcomestopass.com/",
"http://www.whatisliberalism.com/",
"http://yglesias.typepad.com/matthew/2006/03/rawls_on_capita.html",
"http://yorkielabour.blogspot.com/",
"https://www.blogger.com/atom/10883926",
"https://www.paypal.com/cgi-bin/webscr"
	};

	/********************************************************************
	********************************************************************/
	public static void main( String[] args)
	{
		MysqlDataSource	theSource = new MysqlDataSource();
		theSource.setUrl("jdbc:mysql://localhost:3306/Bloggers4Labour?user=root&password=Militant&useUnicode=true");

		RecommendationHandler	rh = new RecommendationHandler(theSource);

		for ( int i = 0; i < 10000; i++)
		{
			String	ssnID = String.valueOf( Math.random() * 500);
			double	d = Math.random() * testStrings.length;

			System.out.println("[" + (i++) + "]: " + rh.handleRequest( ssnID, testStrings[(int) d] ));
		}
	}
}
