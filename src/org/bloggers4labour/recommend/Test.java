/*
 * Test.java
 *
 * Created on 10 July 2006, 20:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.recommend;

import com.hiatus.USQL_Utils;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;
import org.bloggers4labour.sql.*;

/**
 *
 * @author andrewre
 */
public class Test
{
	private static Logger		s_Logger = Logger.getLogger("Main");
	private final static String	CONVERTED_URL = "IF( LOCATE(\"http://www.\",url)=1, CONCAT('http://',LCASE( SUBSTRING(url, 12, LENGTH(url)-11))), LCASE(url))";

	/********************************************************************
	********************************************************************/
	public Test()
	{
		DataSourceConnection	theConnectionObject = null;
		StringBuffer		theBuf;
		long			currTimeMSecs = System.currentTimeMillis();
		boolean			isGood = false;

		try
		{
			MysqlDataSource	theSource = new MysqlDataSource();
			theSource.setUrl("jdbc:mysql://localhost:3306/Bloggers4Labour?user=root&password=Militant&useUnicode=true");

			theConnectionObject = new DataSourceConnection(theSource);
			if (theConnectionObject.Connect())
			{
				// s_Logger.info("conn = " + theConnectionObject);

				Statement	theS = null;
				ResultSet	rs;

				try
				{
					theS = theConnectionObject.createStatement();

					for ( int i = 0; i < s_TestURLsAndRecnos.length; i += 2)
					{
						int		expectedRecno = Integer.parseInt( s_TestURLsAndRecnos[i] );
						String		adjustedURL = s_TestURLsAndRecnos[i+1].toLowerCase();

						if (adjustedURL.startsWith("http://feeds.feedburner.com/wongablog"))
						{
							adjustedURL = "http://wandwaver.co.uk/blog/";
						}
						else if (adjustedURL.startsWith("http://bagrec.livejournal.com/"))
						{
							adjustedURL = "http://livejournal.com/users/bagrec/";
						}
						else if (adjustedURL.startsWith("http://afarfetchedresolution."))
						{
							adjustedURL = "http://afarfetchedresolution.com/";
						}
						else if (adjustedURL.startsWith("http://www.madmusingsof."))
						{
							adjustedURL = "http://madmusingsof.me.uk/weblog/";
						}
						else if (adjustedURL.startsWith("http://www.rogerdarlington."))
						{
							adjustedURL = "http://rogerdarlington.co.uk/nighthawk/";
						}
						else if (adjustedURL.startsWith("http://blogs.guardian.co.uk/news/archives"))
						{
							adjustedURL = "http://blogs.guardian.co.uk/news/archives/cat_uk_politics.html";
						}
						else if (adjustedURL.startsWith("http://cllrfay."))
						{
							adjustedURL = "http://blog.co.uk/cllrfay";
						}
						else	adjustedURL = adjustedURL.startsWith("http://www.") ? ( "http://" + adjustedURL.substring(11)) : adjustedURL;

						String		s = "SELECT site_recno FROM site WHERE LOCATE(" + CONVERTED_URL + "," + USQL_Utils.getQuoted(adjustedURL) + ") > 0";

						rs = theS.executeQuery(s);
						if (rs.next())
						{
							int	actualRecno = rs.getInt(1);

							if (rs.next())
							{
								System.out.println("Multiple matches for " + s_TestURLsAndRecnos[i+1]);
							}
							else if ( actualRecno != expectedRecno)
							{
								System.out.println("Wrong recno for " + s_TestURLsAndRecnos[i+1]);
							}
							// else	System.out.println("OK: " + s_TestURLsAndRecnos[i+1]);
						}
						else
						{
							System.out.println("No match for: " + s); // " + s_TestURLsAndRecnos[i+1]);
						}

						rs.close();
					}

					System.out.println("DONE!");
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
			// s_FL_Logger.info("m_FeedChannels = " + m_FeedChannels);

			if ( theConnectionObject != null)
			{
				theConnectionObject.CloseDown();
				theConnectionObject = null;
			}
		}
	}

	/********************************************************************
	********************************************************************/
	public static void main( String[] args)
	{
		new Test();
	}

	private final static String	s_TestURLsAndRecnos[] =
					{
					"212","http://afarfetchedresolution.blogspot.com/2006/07/idiots-please-read-this-understand-it.html",
					"302","http://agitpropcentral.blogspot.com/2006/07/compass-and-party-in-fighting.html",
					"317","http://alun-clwydwest.blogspot.com/2006/07/tories-clueless-on-cost-of.html",
					"300","http://andrewkbrown.wordpress.com/2006/06/29/stupid-council/",
					"300","http://andrewkbrown.wordpress.com/2006/07/02/kent-spitfires/",
					"300","http://andrewkbrown.wordpress.com/2006/07/03/andriano/",
					"300","http://andrewkbrown.wordpress.com/2006/07/05/do-scottish-mps-tell-england-what-to-do/",
					"300","http://andrewkbrown.wordpress.com/2006/07/06/history-matters/",
					"300","http://andrewkbrown.wordpress.com/2006/07/07/same-speech-different-interpretation/",
					"189","http://bagrec.livejournal.com/193891.html",
					"189","http://bagrec.livejournal.com/195588.html",
					"189","http://bagrec.livejournal.com/202062.html",
					"190","http://barrysbeef.blogspot.com/2006/06/england-thrown-into-disarray_29.html",
					"190","http://barrysbeef.blogspot.com/2006/06/i-thought-it-was-ribery-who-was-on.html",
					"190","http://barrysbeef.blogspot.com/2006/06/too-chav-or-not-too-chav-that-is.html",
					"190","http://barrysbeef.blogspot.com/2006/07/beautiful-game.html",
					"190","http://barrysbeef.blogspot.com/2006/07/cheating-bstards.html",
					"190","http://barrysbeef.blogspot.com/2006/07/do-what-you-can-do-punish-ronaldo.html",
					"190","http://barrysbeef.blogspot.com/2006/07/italia-italia-italia.html",
					"190","http://barrysbeef.blogspot.com/2006/07/missing-person.html",
					"190","http://barrysbeef.blogspot.com/2006/07/viva-las-vegas.html",
					"227","http://blog.hakmao.com/archives/001638.html",
					"227","http://blog.hakmao.com/archives/001644.html",
					"85","http://blogs.guardian.co.uk/news/archives/2006/06/28/gordon_1_brussels_0.html",
					"85","http://blogs.guardian.co.uk/news/archives/2006/06/29/gitmo_to_close.html",
					"85","http://blogs.guardian.co.uk/news/archives/2006/06/29/these_kidnappings_change_everything.html",
					"85","http://blogs.guardian.co.uk/news/archives/2006/07/03/dont_despair_on_doha_yet.html",
					"85","http://blogs.guardian.co.uk/news/archives/2006/07/06/not_so_secret.html",
					"150","http://brightonregencylabourparty.blogspot.com/2006/06/did-you-ever-have-wank-about-thatcher.html",
					"150","http://brightonregencylabourparty.blogspot.com/2006/06/its-labour-bias-ok.html",
					"150","http://brightonregencylabourparty.blogspot.com/2006/06/my-thoughts-on-thatcher-and-gordons.html",
					"150","http://brightonregencylabourparty.blogspot.com/2006/06/thomas-paine-festival.html",
					"345","http://brownswoodcouncillors.blogspot.com/2006/06/brownswood-councillors_25.html",
					"290","http://chriswgale.typepad.com/chris_gale/2006/06/dont_panic.html",
					"290","http://chriswgale.typepad.com/chris_gale/2006/06/nuclear_weapons.html",
					"290","http://chriswgale.typepad.com/chris_gale/2006/07/bloodsports_bil.html",
					"290","http://chriswgale.typepad.com/chris_gale/2006/07/pure_evil.html",
					"259","http://cllrcorazzo.blogspot.com/2006/06/ever-been-so-angered-by-incomptence.html",
					"259","http://cllrcorazzo.blogspot.com/2006/07/salute-local-party.html",
					"298","http://cllrfay.blog.co.uk/2006/06/29/title~919715",
					"95","http://cloud-in-trousers.blogspot.com/2006/07/amnesty-international-on-gilad-shalit.html",
					"95","http://cloud-in-trousers.blogspot.com/2006/07/private-reasons-great-or-small.html",
					"4","http://councillorbobpiper.blogspot.com/2006/06/bland-and-blander.html",
					"4","http://councillorbobpiper.blogspot.com/2006/06/in-search-of-right.html",
					"4","http://councillorbobpiper.blogspot.com/2006/06/johnathan-ross-for-a-list.html",
					"4","http://councillorbobpiper.blogspot.com/2006/07/andy-marr-this-morning-had-vivienne.html",
					"4","http://councillorbobpiper.blogspot.com/2006/07/dirty-politics.html",
					"4","http://councillorbobpiper.blogspot.com/2006/07/history-man.html",
					"4","http://councillorbobpiper.blogspot.com/2006/07/not-counting-chickens.html",
					"4","http://councillorbobpiper.blogspot.com/2006/07/thats-more-like-it.html",
					"4","http://councillorbobpiper.blogspot.com/2006/07/this-law-must-go.html",
					"17","http://cramlingtonvillagecouncillor.blogspot.com/2006/06/challenge-for-bloggers-4-labour_29.html",
					"17","http://cramlingtonvillagecouncillor.blogspot.com/2006/06/welcome-to-world-of-blogging.html",
					"17","http://cramlingtonvillagecouncillor.blogspot.com/2006/07/english-votes-for-english-laws.html",
					"178","http://eastclifframsgate.blogspot.com/2006/06/david-cameron-to-abolish-human-rights.html",
					"363","http://emmajonesbrucegrove.blogspot.com/2006/07/first-post.html",
					"363","http://emmajonesbrucegrove.blogspot.com/2006/07/sport-relief-mile.html",
					"252","http://everton.blogspot.com/2006/06/dashed-into-record-shop-in-hebden.html",
					"252","http://everton.blogspot.com/2006/06/victoria-reveals-that-david-never.html",
					"252","http://everton.blogspot.com/2006/07/am-i-only-one-to-notice-common-theme.html",
					"252","http://everton.blogspot.com/2006/07/anthropologist-writes.html",
					"252","http://everton.blogspot.com/2006/07/kenny-writes.html",
					"252","http://everton.blogspot.com/2006/07/plaicekicks.html",
					"210","http://feeds.feedburner.com/councillorstuartbruce?m=151",
					"162","http://feeds.feedburner.com/diaryofsorts?m=611",
					"162","http://feeds.feedburner.com/diaryofsorts?m=618",
					"266","http://feeds.feedburner.com/schmooontherun?m=184",
					"134","http://feeds.feedburner.com/wongablog?m=1287",
					"134","http://feeds.feedburner.com/wongablog?m=1289",
					"134","http://feeds.feedburner.com/wongablog?m=1291",
					"134","http://feeds.feedburner.com/wongablog?m=1292",
					"134","http://feeds.feedburner.com/wongablog?m=1294",
					"134","http://feeds.feedburner.com/wongablog?m=1295",
					"134","http://feeds.feedburner.com/wongablog?m=1296",
					"134","http://feeds.feedburner.com/wongablog?m=1297",
					"134","http://feeds.feedburner.com/wongablog?m=1299",
					"134","http://feeds.feedburner.com/wongablog?m=1302",
					"134","http://feeds.feedburner.com/wongablog?m=1308",
					"134","http://feeds.feedburner.com/wongablog?m=1313",
					"134","http://feeds.feedburner.com/wongablog?m=1315",
					"198","http://fourthterm.net/plog/index.php?op=viewarticle",
					"197","http://fourthterm.net/postnuke/html/index.php?name=news",
					"364","http://georgeeaton.blogspot.com/2006/07/first-challenge.html",
					"314","http://grumpyoldben.blogspot.com/2006/06/1-day-until-morocco-heist-pair.html",
					"314","http://grumpyoldben.blogspot.com/2006/06/kings-cross-fire-causes-grumpyoldben.html",
					"293","http://humanistsforlabour.typepad.com/labour_humanists/2006/06/a_salute_to_our.html",
					"293","http://humanistsforlabour.typepad.com/labour_humanists/2006/06/compass_confere.html",
					"293","http://humanistsforlabour.typepad.com/labour_humanists/2006/06/fed_up_with_tho.html",
					"293","http://humanistsforlabour.typepad.com/labour_humanists/2006/06/so_who_objects_.html",
					"293","http://humanistsforlabour.typepad.com/labour_humanists/2006/07/an_excellent_po.html",
					"293","http://humanistsforlabour.typepad.com/labour_humanists/2006/07/a_new_political.html",
					"293","http://humanistsforlabour.typepad.com/labour_humanists/2006/07/cosy_relationsh.html",
					"293","http://humanistsforlabour.typepad.com/labour_humanists/2006/07/heres_a_plan_fo.html",
					"293","http://humanistsforlabour.typepad.com/labour_humanists/2006/07/innerchange_are.html",
					"293","http://humanistsforlabour.typepad.com/labour_humanists/2006/07/right_wing_blog.html",
					"293","http://humanistsforlabour.typepad.com/labour_humanists/2006/07/viva_zapatero.html",
					"65","http://hurryupharry.bloghouse.net/archives/2006/06/28/aki_nawaz_is_a_tosser.php",
					"65","http://hurryupharry.bloghouse.net/archives/2006/06/29/gaddafi_the_opera.php",
					"65","http://hurryupharry.bloghouse.net/archives/2006/06/29/murdoch_tells_tony_its_time_to_go.php",
					"65","http://hurryupharry.bloghouse.net/archives/2006/07/07/unite_against_terror_statement_in_arabic.php",
					"272","http://janestheones.blogspot.com/2006/06/it-wouldnt-stand-up-in-court.html",
					"329","http://jtothak.blogspot.com/2006/06/blaenau-blues.html",
					"167","http://kerroncross.blogspot.com/2006/06/alarming-situation.html",
					"167","http://kerroncross.blogspot.com/2006/06/beaver-watch.html",
					"167","http://kerroncross.blogspot.com/2006/06/has-anyone-seen-shaun-woodward.html",
					"167","http://kerroncross.blogspot.com/2006/06/is-it-something-i-said-part-2.html",
					"167","http://kerroncross.blogspot.com/2006/06/lib-dems-get-caught-mis-leading-voters.html",
					"167","http://kerroncross.blogspot.com/2006/06/luis-figo-is-not-god.html",
					"167","http://kerroncross.blogspot.com/2006/06/standard-housing-in-watford.html",
					"167","http://kerroncross.blogspot.com/2006/06/strange-political-double-acts.html",
					"167","http://kerroncross.blogspot.com/2006/07/basking-shark-eats-man.html",
					"167","http://kerroncross.blogspot.com/2006/07/beaver-watch-part-2.html",
					"167","http://kerroncross.blogspot.com/2006/07/breaking-into-parliament-is-childs.html",
					"167","http://kerroncross.blogspot.com/2006/07/cam-and-have-go-if-you-think-youre.html",
					"167","http://kerroncross.blogspot.com/2006/07/explosive-results.html",
					"167","http://kerroncross.blogspot.com/2006/07/families-at-work-week.html",
					"167","http://kerroncross.blogspot.com/2006/07/feeling-cross-and-pub.html",
					"167","http://kerroncross.blogspot.com/2006/07/hoff-to-hospital.html",
					"167","http://kerroncross.blogspot.com/2006/07/i-like-big-bibles-i-cannot-lie.html",
					"167","http://kerroncross.blogspot.com/2006/07/increased-security-at-wimbledon.html",
					"167","http://kerroncross.blogspot.com/2006/07/labour-party-to-issue-yellow-cards-to.html",
					"167","http://kerroncross.blogspot.com/2006/07/recommendations.html",
					"167","http://kerroncross.blogspot.com/2006/07/sacked-for-being-tory-in-wales.html",
					"167","http://kerroncross.blogspot.com/2006/07/savage-way-to-lose-world-cup.html",
					"167","http://kerroncross.blogspot.com/2006/07/sexual-discrimination-can-come-in-many.html",
					"224","http://labour4crystalpalace.blogspot.com/2006/06/results-from-this-weeks-by-election.html",
					"170","http://leegregory.typepad.com/lee_gregory/2006/07/so_long_and_far.html",
					"148","http://leightonandrews.typepad.com/leighton_andrews_am/2006/07/allez_les_bleus.html",
					"148","http://leightonandrews.typepad.com/leighton_andrews_am/2006/07/commemorating_t.html",
					"148","http://leightonandrews.typepad.com/leighton_andrews_am/2006/07/echo_on_plaid_s.html",
					"148","http://leightonandrews.typepad.com/leighton_andrews_am/2006/07/plaid_cymru_wan.html",
					"148","http://leightonandrews.typepad.com/leighton_andrews_am/2006/07/well_done_tessa.html",
					"149","http://leightonandrews.typepad.com/rhondda_today/2006/06/i_attended_cros.html",
					"149","http://leightonandrews.typepad.com/rhondda_today/2006/06/rhondda_diabete.html",
					"149","http://leightonandrews.typepad.com/rhondda_today/2006/07/children_from_y.html",
					"90","http://letsbesensible.blogspot.com/2006/06/mods-cons-neocons-and-rinos.html",
					"90","http://letsbesensible.blogspot.com/2006/06/shining.html",
					"90","http://letsbesensible.blogspot.com/2006/06/tops-and-bottoms.html",
					"90","http://letsbesensible.blogspot.com/2006/06/unfunny-politicians.html",
					"90","http://letsbesensible.blogspot.com/2006/07/not-serious-political-project.html",
					"90","http://letsbesensible.blogspot.com/2006/07/please.html",
					"90","http://letsbesensible.blogspot.com/2006/07/solution-to-west-lothian-question-and.html",
					"255","http://lithgo.wordpress.com/2006/07/05/25/",
					"324","http://lukeakehurst.blogspot.com/2006/06/i-support.html",
					"324","http://lukeakehurst.blogspot.com/2006/06/people-liberation-front-of-judea-etc.html",
					"324","http://lukeakehurst.blogspot.com/2006/07/coded-critiques.html",
					"324","http://lukeakehurst.blogspot.com/2006/07/picture-time.html",
					"182","http://mike-ion.blogspot.com/",
					"182","http://mike-ion.blogspot.com/2006/06/compass-taking-labour-in-right.html",
					"129","http://nevertrustahippy.blogspot.com/2006/06/decent-blogging.html",
					"129","http://nevertrustahippy.blogspot.com/2006/06/quick-question.html",
					"129","http://nevertrustahippy.blogspot.com/2006/06/towards-charter-for-representation.html",
					"205","http://newerlabour.blogspot.com/2006/06/political-commitment-is-not-about-true.html",
					"205","http://newerlabour.blogspot.com/2006/07/compass-keeps-on-turning.html",
					"343","http://newgolddream.dyndns.info/blog/?p=17",
					"343","http://newgolddream.dyndns.info/blog/?p=18",
					"343","http://newgolddream.dyndns.info/blog/?p=21",
					"343","http://newgolddream.dyndns.info/blog/?p=6",
					"351","http://nicolaheaton.blogspot.com/2006/07/day-britain-became-proper-democracy.html",
					"351","http://nicolaheaton.blogspot.com/2006/07/in-rush-to-centre-dont-forget-your.html",
					"351","http://nicolaheaton.blogspot.com/2006/07/muslim-community.html",
					"351","http://nicolaheaton.blogspot.com/2006/07/one-cannot-have-system-of-criminal.html",
					"351","http://nicolaheaton.blogspot.com/2006/07/partisan-results-of-war.html",
					"351","http://nicolaheaton.blogspot.com/2006/07/problem-of-being-scottish.html",
					"351","http://nicolaheaton.blogspot.com/2006/07/problem-with-being-white.html",
					"351","http://nicolaheaton.blogspot.com/2006/07/where-should-labour-go-now.html",
					"351","http://nicolaheaton.blogspot.com/2006/07/world-cup-update.html",
					"351","http://nicolaheaton.blogspot.com/2006/07/world-cup-wrongs.html",
					"63","http://normblog.typepad.com/normblog/2006/06/a_step.html",
					"63","http://normblog.typepad.com/normblog/2006/06/cycle_talk.html",
					"63","http://normblog.typepad.com/normblog/2006/06/dignity_period.html",
					"63","http://normblog.typepad.com/normblog/2006/06/left_look.html",
					"63","http://normblog.typepad.com/normblog/2006/06/legless.html",
					"63","http://normblog.typepad.com/normblog/2006/06/oops_the_world_.html",
					"63","http://normblog.typepad.com/normblog/2006/06/playing_a_game.html",
					"63","http://normblog.typepad.com/normblog/2006/06/short_short_sto_10.html",
					"63","http://normblog.typepad.com/normblog/2006/06/short_short_sto_3.html",
					"63","http://normblog.typepad.com/normblog/2006/06/short_short_sto_5.html",
					"63","http://normblog.typepad.com/normblog/2006/06/short_short_sto_8.html",
					"63","http://normblog.typepad.com/normblog/2006/06/the_normblog_pr_4.html",
					"63","http://normblog.typepad.com/normblog/2006/06/top_aussies.html",
					"63","http://normblog.typepad.com/normblog/2006/06/unhealthy_attit.html",
					"63","http://normblog.typepad.com/normblog/2006/07/blind_alley.html",
					"63","http://normblog.typepad.com/normblog/2006/07/dance_of_death_.html",
					"63","http://normblog.typepad.com/normblog/2006/07/different_route.html",
					"63","http://normblog.typepad.com/normblog/2006/07/frederick_sewar.html",
					"63","http://normblog.typepad.com/normblog/2006/07/high_and_low.html",
					"63","http://normblog.typepad.com/normblog/2006/07/history_nation.html",
					"63","http://normblog.typepad.com/normblog/2006/07/jungle_message.html",
					"63","http://normblog.typepad.com/normblog/2006/07/now_and_then.html",
					"63","http://normblog.typepad.com/normblog/2006/07/nun_too_subtle.html",
					"63","http://normblog.typepad.com/normblog/2006/07/or_something.html",
					"63","http://normblog.typepad.com/normblog/2006/07/short_short_sto_10.html",
					"63","http://normblog.typepad.com/normblog/2006/07/short_short_sto_3.html",
					"63","http://normblog.typepad.com/normblog/2006/07/short_short_sto_9.html",
					"365","http://parburypolitica.blogspot.com/2006/07/5-times-in-one-night.html",
					"365","http://parburypolitica.blogspot.com/2006/07/homer-simpson-energy-option.html",
					"365","http://parburypolitica.blogspot.com/2006/07/peter-and-ann.html",
					"169","http://paulburgin.blogspot.com/2006/06/cross-labourshire-twinned-with-dale.html",
					"169","http://paulburgin.blogspot.com/2006/06/first-anniversary-post-margaret.html",
					"169","http://paulburgin.blogspot.com/2006/06/first-anniversary.html",
					"169","http://paulburgin.blogspot.com/2006/06/free-upgrades.html",
					"169","http://paulburgin.blogspot.com/2006/06/le-divorce.html",
					"169","http://paulburgin.blogspot.com/2006/07/and-doctors-new-assistant-is.html",
					"169","http://paulburgin.blogspot.com/2006/07/fourteen-years-ago.html",
					"169","http://paulburgin.blogspot.com/2006/07/in-praise-of-female-bloggers.html",
					"169","http://paulburgin.blogspot.com/2006/07/popular-blogs-at-bloggers4labour.html",
					"169","http://paulburgin.blogspot.com/2006/07/sunday-blogging-on-gmtv.html",
					"53","http://politicalhackuk.blogspot.com/2006/07/oh-no-john-no-john-no-john-no.html",
					"301","http://rhodonpublicaffairs.blogspot.com/2006/06/no-private-funds-control-of-nhs.html",
					"332","http://ridiculouspolitics.blogspot.com/2006/06/left-wing-tory-attacks-david-cameron.html",
					"332","http://ridiculouspolitics.blogspot.com/2006/07/lib-dems-in-major-new-funding-row.html",
					"332","http://ridiculouspolitics.blogspot.com/2006/07/old-etonians-stage-coup-de-partie.html",
					"135","http://rodneymcaree.blogspot.com/2006/06/cameron-and-human-rights.html",
					"211","http://ruabonlabourcouncillor.blogspot.com/2006/06/my-first-world-cup-posting.html",
					"211","http://ruabonlabourcouncillor.blogspot.com/2006/07/londons-pain-was-felt-by-many.html",
					"211","http://ruabonlabourcouncillor.blogspot.com/2006/07/suffering-animal-made-me-feel-ashamed_06.html",
					"103","http://rullsenbergrules.blogspot.com/2006/06/dear-lord-of-rings.html",
					"103","http://rullsenbergrules.blogspot.com/2006/06/fiskers-and-blog-anniversaries.html",
					"103","http://rullsenbergrules.blogspot.com/2006/06/house.html",
					"103","http://rullsenbergrules.blogspot.com/2006/06/on-this-note-with-just-another-14.html",
					"103","http://rullsenbergrules.blogspot.com/2006/07/apparantly-i-dont-exist.html",
					"103","http://rullsenbergrules.blogspot.com/2006/07/is-this-first-time.html",
					"103","http://rullsenbergrules.blogspot.com/2006/07/who-else-sleeps-like-pornographic.html",
					"225","http://skipper59.blogspot.com/2006/06/blair-busted-flush-on-crime-and-much.html",
					"225","http://skipper59.blogspot.com/2006/06/charles-still-very-angry.html",
					"225","http://skipper59.blogspot.com/2006/06/every-day-blair-remains-is-gift-to.html",
					"225","http://skipper59.blogspot.com/2006/07/english-votes-for-english-issues-would.html",
					"225","http://skipper59.blogspot.com/2006/07/of-fat-tories-and-eastern-europe.html",
					"225","http://skipper59.blogspot.com/2006/07/this-prezza-problem-is-now-urgent.html",
					"239","http://skuds.co.uk/index.php/2006/06/another-false-alarm/",
					"239","http://skuds.co.uk/index.php/2006/06/crawley-in-bloom/",
					"239","http://skuds.co.uk/index.php/2006/06/gerrit-dou/",
					"239","http://skuds.co.uk/index.php/2006/06/lord-of-the-rings-the-musical/",
					"239","http://skuds.co.uk/index.php/2006/06/more-suspicious-behaviour/",
					"239","http://skuds.co.uk/index.php/2006/06/some-of-my-best-friends-are-chicken-sexers/",
					"239","http://skuds.co.uk/index.php/2006/06/the-easiest-job-in-the-world/",
					"239","http://skuds.co.uk/index.php/2006/07/bloggers4labour-recommendations/",
					"239","http://skuds.co.uk/index.php/2006/07/england-the-verdict/",
					"239","http://skuds.co.uk/index.php/2006/07/foreign-accent-syndrome/",
					"239","http://skuds.co.uk/index.php/2006/07/goriest-goof/",
					"239","http://skuds.co.uk/index.php/2006/07/ian-wright-wright-wright/",
					"239","http://skuds.co.uk/index.php/2006/07/look-on-the-bright-side/",
					"239","http://skuds.co.uk/index.php/2006/07/manbags-and-gladrags/",
					"239","http://skuds.co.uk/index.php/2006/07/nanny-state-2/",
					"239","http://skuds.co.uk/index.php/2006/07/two-minutes/",
					"124","http://smalltownscribble.blogspot.com/2006/06/germany-win-hurrah.html",
					"124","http://smalltownscribble.blogspot.com/2006/07/and-so-end.html",
					"327","http://snowflake5.blogspot.com/2006/06/capitalism-and-centre-left.html",
					"327","http://snowflake5.blogspot.com/2006/06/uk-q1-2006-gdp-revised-upwards-to-07.html",
					"327","http://snowflake5.blogspot.com/2006/06/unemployment-in-uk.html",
					"327","http://snowflake5.blogspot.com/2006/07/are-tories-snp-of-england.html",
					"327","http://snowflake5.blogspot.com/2006/07/pound-passes-yen-as-reserve-currency.html",
					"327","http://snowflake5.blogspot.com/2006/07/times-reviews-tacky-offensive-tory.html",
					"327","http://snowflake5.blogspot.com/2006/07/tories-wish-to-leave-eu.html",
					"327","http://snowflake5.blogspot.com/2006/07/total-taxes-as-of-gdp.html",
					"327","http://snowflake5.blogspot.com/2006/07/why-labour-should-embrace-idea-of.html",
					"238","http://softleft.blogspot.com/2006/06/proportional-response.html",
					"238","http://softleft.blogspot.com/2006/07/parting-is-such-sweet-sorrow_08.html",
					"229","http://thecrazyworldofpolitics.blogspot.com/2006/06/good-luck-comrades.html",
					"229","http://thecrazyworldofpolitics.blogspot.com/2006/06/neal-lawsons-letter-to-blair.html",
					"229","http://thecrazyworldofpolitics.blogspot.com/2006/06/why-labour-activists-cant-stand-lib.html",
					"229","http://thecrazyworldofpolitics.blogspot.com/2006/07/are-they-legitimate-political-party.html",
					"229","http://thecrazyworldofpolitics.blogspot.com/2006/07/face-of-cuddly-caring-conservatism.html",
					"229","http://thecrazyworldofpolitics.blogspot.com/2006/07/final-reflection.html",
					"229","http://thecrazyworldofpolitics.blogspot.com/2006/07/life-goes-on-in-nus.html",
					"229","http://thecrazyworldofpolitics.blogspot.com/2006/07/morning-after-night-before.html",
					"229","http://thecrazyworldofpolitics.blogspot.com/2006/07/principled-ones.html",
					"229","http://thecrazyworldofpolitics.blogspot.com/2006/07/tonys-speech-to-national-policy-forum.html",
					"229","http://thecrazyworldofpolitics.blogspot.com/2006/07/what-on-earth.html",
					"114","http://thegrammaticalpuss.blogspot.com/2006/06/hello-kettle-this-is-pot.html",
					"277","http://thepoormouth.blogspot.com/2006/06/burghers-of-calais-london.html",
					"277","http://thepoormouth.blogspot.com/2006/06/churchill-parliament-square.html",
					"277","http://thepoormouth.blogspot.com/2006/06/goodish-news-or-bad-news-or-worse-or.html",
					"277","http://thepoormouth.blogspot.com/2006/06/proof-that-age-does-not-always-confer.html",
					"277","http://thepoormouth.blogspot.com/2006/07/caedite-eos-novit-enim-dominus-qui.html",
					"277","http://thepoormouth.blogspot.com/2006/07/give-it-up.html",
					"277","http://thepoormouth.blogspot.com/2006/07/how-london-defeated-bombers.html",
					"277","http://thepoormouth.blogspot.com/2006/07/saddams-road-to-hell.html",
					"204","http://thesilenthunter.blogspot.com/2006/06/ind-hold-blaenau-gwent-con-hold.html",
					"204","http://thesilenthunter.blogspot.com/2006/07/yet-another-four-years-of-hurt.html",
					"265","http://thewonderfulworldoflola.blogspot.com/2006/06/lets-hear-it-for-doctor.html",
					"265","http://thewonderfulworldoflola.blogspot.com/2006/06/megans-law.html",
					"265","http://thewonderfulworldoflola.blogspot.com/2006/06/more-thursday-geekery.html",
					"265","http://thewonderfulworldoflola.blogspot.com/2006/06/on-weight-loss.html",
					"265","http://thewonderfulworldoflola.blogspot.com/2006/06/sorry-were-closed.html",
					"265","http://thewonderfulworldoflola.blogspot.com/2006/07/pleased-to-report.html",
					"186","http://treesforlabour.wordpress.com/2006/06/27/fighting-the-wrong-battles-at-the-wrong-time-in-the-wrong-places/",
					"186","http://treesforlabour.wordpress.com/2006/06/27/fighting-the-wrong-battles-at-the-wrong-time-in-the-wrong-places/#comment-1005",
					"353","http://tygerland.net/?p=702",
					"353","http://tygerland.net/?p=705",
					"353","http://tygerland.net/?p=709",
					"353","http://tygerland.net/?p=710",
					"353","http://tygerland.net/?p=712",
					"251","http://uk.blog.360.yahoo.com/blog-kbdesrg7bqpuovjiyzvpss3reg--?cq=1",
					"47","http://users.ox.ac.uk/~magd1368/weblog/2006_06_01_archive.html",
					"288","http://warrenmorgan.blogspot.com/2006/07/me-on-web.html",
					"263","http://whelton.blogspot.com/2006/06/bromley-blaenau-by-elections.html",
					"263","http://whelton.blogspot.com/2006/07/comic-davidson-declared-bankrupt.html",
					"263","http://whelton.blogspot.com/2006/07/tony-blair-speech-to-labour-party.html",
					"37","http://www.antoniabance.org.uk/2006/06/24/being-a-guardian-cack-inspired-councillor/",
					"37","http://www.antoniabance.org.uk/2006/06/29/18-lib-dem-councillors-sitting-on-the-wall/",
					"37","http://www.antoniabance.org.uk/2006/06/29/zoe-williams-on-lad-mags/",
					"37","http://www.antoniabance.org.uk/2006/06/30/just-for-the-boys/",
					"37","http://www.antoniabance.org.uk/2006/07/06/votes-for-the-nec/",
					"299","http://www.barder.com/ephems/512",
					"144","http://www.bloggers4labour.org/2006/06/compass.jsp",
					"144","http://www.bloggers4labour.org/2006/06/recommend-posts-at-b4l.jsp",
					"144","http://www.bloggers4labour.org/2006/07/lest-we-forget.jsp",
					"54","http://www.davosnewbies.com/2006/06/24/wholesale-to-retail-politics/",
					"185","http://www.dirtyleftie.co.uk/?p=148",
					"185","http://www.dirtyleftie.co.uk/?p=154",
					"121","http://www.gentheoryrubbish.com/archives/000688.html",
					"121","http://www.gentheoryrubbish.com/archives/000690.html",
					"49","http://www.josalmon.co.uk/2006/06/football-crazy/",
					"49","http://www.josalmon.co.uk/2006/06/just-for-the-boys/",
					"49","http://www.josalmon.co.uk/2006/06/politics-online/",
					"49","http://www.josalmon.co.uk/2006/06/pompeii-live/",
					"49","http://www.josalmon.co.uk/2006/06/weight-watchers/",
					"49","http://www.josalmon.co.uk/2006/07/another-2lbs/",
					"49","http://www.josalmon.co.uk/2006/07/bnp-in-westminster/",
					"49","http://www.josalmon.co.uk/2006/07/break-the-abortion-taboo/",
					"49","http://www.josalmon.co.uk/2006/07/defined-in-the-dictionary/",
					"49","http://www.josalmon.co.uk/2006/07/internet-fast-and-slow-lanes/",
					"49","http://www.josalmon.co.uk/2006/07/labour-nec-elections/",
					"49","http://www.josalmon.co.uk/2006/07/new-plugins/",
					"49","http://www.josalmon.co.uk/2006/07/oxford-is-being-invaded/",
					"49","http://www.josalmon.co.uk/2006/07/site-stats-for-june-2006/",
					"49","http://www.josalmon.co.uk/2006/07/suspended-for-disagreeing-with-the-government/",
					"49","http://www.josalmon.co.uk/2006/07/wealth-vs-life-expectancy/",
					"321","http://www.labour.org.uk/blog/index.php?id=106",
					"320","http://www.labour.org.uk/blog/index.php?id=90",
					"303","http://www.lewisham.org.uk/john/lbl/weblog/2006/07/i-predict-riot-or-back-to-nineties.html",
					"66","http://www.madmusingsof.me.uk/archives/2006/07/lets_blame_roon.php",
					"66","http://www.madmusingsof.me.uk/archives/2006/07/were_coming_hom.php",
					"102","http://www.ministryoftruth.org.uk/2006/06/22/were-not-homophobic-but/",
					"102","http://www.ministryoftruth.org.uk/2006/06/28/schrodingers-baby/",
					"102","http://www.ministryoftruth.org.uk/2006/06/29/doin-it-for-the-kids/",
					"102","http://www.ministryoftruth.org.uk/2006/06/30/myth-conceptions/",
					"102","http://www.ministryoftruth.org.uk/2006/06/30/what-it-really-means-to-be-british/",
					"102","http://www.ministryoftruth.org.uk/2006/07/05/following-the-money/",
					"102","http://www.ministryoftruth.org.uk/2006/07/05/i-suppose-i-should-know-better-but/",
					"102","http://www.ministryoftruth.org.uk/2006/07/05/picture-exclusive-ronaldo-apologises-to-rooney-after-world-cup-incident/",
					"102","http://www.ministryoftruth.org.uk/2006/07/06/getting-murkier-by-the-minute/",
					"102","http://www.ministryoftruth.org.uk/2006/07/09/taking-his-ball-home/",
					"102","http://www.ministryoftruth.org.uk/2006/07/10/are-you-sure-you-wouldnt-prefer-st-giles/",
					"102","http://www.ministryoftruth.org.uk/2006/07/10/hit-on-a-sore-point-did-i/",
					"247","http://www.odpm.gov.uk/cs/blogs/ministerial_blog/archive/2006/06/22/1203.aspx",
					"165","http://www.owen.org/blog/527",
					"165","http://www.owen.org/blog/536",
					"276","http://www.oxfordlabour.org.uk/2006/06/29/councillor-saj-malik-joins-labour/",
					"276","http://www.oxfordlabour.org.uk/2006/07/03/lib-dem-gaffe-outrages-community-groups/",
					"58","http://www.pootergeek.com/?p=2300",
					"58","http://www.pootergeek.com/?p=2306",
					"58","http://www.pootergeek.com/?p=2311",
					"58","http://www.pootergeek.com/?p=2313",
					"58","http://www.pootergeek.com/?p=2314",
					"58","http://www.pootergeek.com/?p=2319",
					"58","http://www.pootergeek.com/?p=2326",
					"58","http://www.pootergeek.com/?p=2329",
					"58","http://www.pootergeek.com/?p=2336",
					"58","http://www.pootergeek.com/?p=2338",
					"58","http://www.pootergeek.com/?p=2344",
					"97","http://www.recessmonkey.com/2006/06/23/tory-wingnuts-choose-new-party-logo/",
					"97","http://www.recessmonkey.com/2006/06/27/clarke-praises-and-advises-gordon/",
					"97","http://www.recessmonkey.com/2006/06/29/bromley-by-election-gets-a-bit-tense/",
					"97","http://www.recessmonkey.com/2006/06/29/support-lyrical-lucie-sings-for-ms/",
					"97","http://www.recessmonkey.com/2006/06/30/cameron-in-despair-over-mp-selections/",
					"97","http://www.recessmonkey.com/2006/07/01/operation-petal-the-lilac-larceny/",
					"295","http://www.rogerdarlington.co.uk/nighthawk/index.php?id=p2736",
					"295","http://www.rogerdarlington.me.uk/nighthawk/2006/07/why_does_history_matter.html",
					"311","http://www.snedds.co.uk/?p=84",
					"311","http://www.snedds.co.uk/?p=86",
					"210","http://www.stuartssoapbox.com/2006/06/why_we_need_com.html"
					};
}
