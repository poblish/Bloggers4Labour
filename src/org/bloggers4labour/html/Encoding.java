/*
 * Encoding.java
 *
 * Created on 28 August 2006, 19:02
 *
 * (AGR) Original disclaimer follow...
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 *
 * ====================================================================
 * Copyright (c) 1995-1999 Purple Technology, Inc. All rights
 * reserved.
 * 
 * PLAIN LANGUAGE LICENSE: Do whatever you like with this code, free
 * of charge, just give credit where credit is due. If you improve it,
 * please send your improvements to server@purpletech.com. Check
 * http://www.purpletech.com/server/ for the latest version and news.
 *
 * LEGAL LANGUAGE LICENSE: Redistribution and use in source and binary
 * forms, with or without modification, are permitted provided that
 * the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. The names of the authors and the names "Purple Technology,"
 * "Purple Server" and "Purple Chat" must not be used to endorse or
 * promote products derived from this software without prior written
 * permission. For written permission, please contact
 * server@purpletech.com.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND PURPLE TECHNOLOGY ``AS
 * IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE
 * AUTHORS OR PURPLE TECHNOLOGY BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 **/

package org.bloggers4labour.html;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;
import org.apache.log4j.Logger;

/**
 *
 * @author andrewre
 */
public class Encoding	// Formerly com.purpletech.util.Purple_Utils
{
	private static Logger	s_Logger = Logger.getLogger("Main");

	public static void main( String[] args)
	{
		String[]	testStrings = {
				"takes, please get in touch.<a href=\"http://www.recessmonkey.com\"><img src=\"http://bill.verity-networks.com/ext/recess/media/votemonkey.gif\" alt=\"Recess Monkey\" title=\"Recess Monkey\" /></a></p>",
				"This evening's meeting heard reports on: The A2 - Blackheath to <st1:Street w:st=\"on\"><st1:address w:st=\"on\">Shooters Hill Rd</st1:address></st1:Street> Coach f...",
				"arse<a href=\"http://www.tribute.r8.org\" /> <b> Woot</b>!!</a>!!",
				"<P><!--StartFragment -->Sophia over at&nbsp;<A href=\"http://www.demosgreenhouse.co.uk/archives/000970.html\">Demos</A> has been reflecting on whether there might be lessons to be learned from some bits of the private sector, in particular <A href=\"http://www.pret.com/\">Pret-a-Manger</A>.&nbsp; Im intrigued by some of the ideas (without being clear whether theyre practical in a local government setting), for example:</P>",
				"Chuckie Kennedy party of <strike>not very</strike> prestigious jobs",
				"<P class=MsoNormal style=\"MARGIN: 0cm 0cm 0pt\"><TT><SPAN style=\"FONT-SIZE: 10pt; FONT-FAMILY: 'Lucida Sans Typewriter'\"><FONT face=\"Arial, Helvetica, sans-serif\">A&nbsp;really interesting email from a member of the Hastings Creative and Media Community&nbsp;came to my attention today, about what is claimed to be Ôthe 1<SUP>st</SUP> genuinely independent public access web tv station in HasingsÕ.<?xml:namespace prefix = o ns = \"urn:schemas-microsoft-com:office:office\" /><o:p></o:p></FONT></SPAN></TT></P>\n<P class=MsoNormal style=\"MARGIN: 0cm 0cm 0pt\"><TT><SPAN style=\"FONT-SIZE: 10pt; FONT-FAMILY: 'Lucida Sans Typewriter'\"><FONT face=\"Arial, Helvetica, sans-serif\">&nbsp;<o:p></o:p></FONT></SPAN></TT></P>\n<P class=MsoNormal style=\"MARGIN: 0cm 0cm 0pt\"><TT><SPAN style=\"FONT-SIZE: 10pt; FONT-FAMILY: 'Lucida Sans Typewriter'\"><FONT face=\"Arial, Helvetica, sans-serif\">Its presently posted </FONT><A href=\"http://www.angusg.f2s.com/\"><FONT face=\"Arial, Helvetica, sans-serif\">here</FONT></A><FONT face=\"Arial, Helvetica, sans-serif\"> but from tomorrow will be running from:</FONT></SPAN></TT></P>\n<P class=MsoNormal style=\"MARGIN: 0cm 0cm 0pt\"><TT><SPAN style=\"FONT-SIZE: 10pt; FONT-FAMILY: 'Lucida Sans Typewriter'\"><FONT face=\"Arial, Helvetica, sans-serif\">&nbsp;<o:p></o:p></FONT></SPAN></TT></P>\n<P class=MsoNormal style=\"MARGIN: 0cm 0cm 0pt\"><TT><SPAN style=\"FONT-SIZE: 10pt; FONT-FAMILY: 'Lucida Sans Typewriter'\"><A href=\"http://www.tvhastings.org/\"><FONT face=\"Arial, Helvetica, sans-serif\">www.tvhastings.org</FONT></A><o:p></o:p></SPAN></TT></P>\n<P class=MsoNormal style=\"MARGIN: 0cm 0cm 0pt\"><TT><SPAN style=\"FONT-SIZE: 10pt; FONT-FAMILY: 'Lucida Sans Typewriter'\"><FONT face=\"Arial, Helvetica, sans-serif\">&nbsp;<o:p></o:p></FONT></SPAN></TT></P><TT><SPAN style=\"FONT-SIZE: 10pt; FONT-FAMILY: 'Lucida Sans Typewriter'; mso-ansi-language: EN-GB; mso-fareast-language: EN-US; mso-bidi-language: AR-SA\"><FONT face=\"Arial, Helvetica, sans-serif\">Its early days but all credit to the creators - IÕm sure it canÕt fail to be better than the standard of the local dead tree press.</FONT></SPAN></TT>",
				"spotted at<a href=\"http://foo.com\"> normblog</a>.",
				"spotted at <a href=\"http://foo.com\">normblog </a>Hullo",
				"<p>This site lists Blogs in the UK. Woo.<BR><br  /><a href=\"http://www.britblog.com\"  target=\'_blank\'><p style=\"text-align:center;\"><img src=\"http://www.westminstervillage.co.uk/images/icon_britblog_80x15.gif\" border=\"0\" title=\"\" alt=\"\" class=\"pivot-image\" /></p></a></p>",
				"rewind<a href=\"#footnote-1-1581\" id=\"footnote-link-1-1581\" title=\"See the footnote.\"><strike>fldflfslsjfs</strike></a> end.",
				"<div class=\"feedflare\"><a href=\"http://feeds.feedburner.com/~f/wongablog?a=ylCzK0wE\"><img src=\"http://feeds.feedburner.com/~f/wongablog?i=ylCzK0wE\" border=\"0\"></img></a> <a href=\"http://feeds.feedburner.com/~f/wongablog?a=msj6myka\"><img src=\"http://feeds.feedburner.com/~f/wongablog?i=msj6myka\" border=\"0\"></img></a> <a href=\"http://feeds.feedburner.com/~f/wongablog?a=iOpV25QU\"><img src=\"http://feeds.feedburner.com/~f/wongablog?i=iOpV25QU\" border=\"0\"></img></a> <a href=\"http://feeds.feedburner.com/~f/wongablog?a=rV8omHpX\"><img src=\"http://feeds.feedburner.com/~f/wongablog?i=rV8omHpX\" border=\"0\"></img></a></div>",
				"<a href=\"http://feeds.feedburner.com/~f/wongablog?a=ylCzK0wE\"><img src=\"http://feeds.feedburner.com/~f/wongablog?i=ylCzK0wE\" border=\"0\"></img></a>",
				"<a href=\"http://feeds.feedburner.com/~f/wongablog?a=ylCzK0wE\"><img src=\"http://feeds.feedburner.com/~f/wongablog?i=ylCzK0wE\" border=\"0\" /></a>",
				"<p><a id=\"more-788\"></a><center><a href=\"http://www.flickr.com/photos/josalmon/86375059/\"><img src=\"http://static.flickr.com/41/86375059_71b88aaa15.jpg\" width=\"375\" height=\"500\" alt=\"Pitt Rivers Museum, Oxford\" title=\"Pitt Rivers Museum, Oxford\" /></a></center></p>",
				"<div xmlns=\"http://www.w3.org/1999/xhtml\"><div align=\"left\"><a href=\"http://photos1.blogger.com/blogger/6247/500/1600/ramsrightlogowhite.jpg\"><img alt=\"\" border=\"0\" height=\"197\" src=\"http://photos1.blogger.com/blogger/6247/500/320/ramsrightlogowhite.jpg\" style=\"DISPLAY: block; MARGIN: 0px auto 10px; WIDTH: 181px; CURSOR: hand; HEIGHT: 137px; TEXT-ALIGN: center\" width=\"246\"/></a><br/><strong>                               Tooting and Mitcham v Ramsgate Sat 14th January</strong></div><div align=\"left\"><strong/><br/>Third placed Ramsgate visited second placed Tooting this afternoon, and the strength and pace of both teams produced an entertaining fixture that showed why they are both near the top.<br/>The first chance of the match fell to Ramsgate in the 2nd minute, when Shaun Welford's header went just over the bar. The next 6 minutes saw 6 corners - the first 2 falling to Tooting and the remainder to the Rams, the last one being fumbled by 'keeper Scarcella to allow Stuart Vahid to flick the ball into the net in the 8th minute.<br/>A stunning 'Banks-style' save by Plumley from Mikael Munday at point blank range in the 64th minute retained the Rams' lead in a hard fought 2nd half, whilst in the 80th minute Dave Hastings went down in the Rams' area under a challenge from Warren Schulz, only to receive a yellow card rather than the hoped for penalty. Two minutes into stoppage time Hastings went down in the area between Edd Vahid and Liam Morris, and this time was rewarded with a spot kick that Danny Twin converted for a late equaliser. </div><div align=\"left\"> </div><div align=\"left\">Next Home Match is Sat 21st Jan 06 - 3PM KO against Molesey</div></div>",
				"&#60;i&#62;For as long as we are subjects, not citizens, of our country&#60;/i&#62;Have a <i><b>woo!</b></i>, yeah!",
				"<a href=\"XXX\"><img src=\"YYY.gif\" /></a>",
				"I co-chair the neighbourhood management forum for Heathside and <?xml:namespace prefix=st1 ns= \"urn:schemas-microsoft-com:office:smarttags\" />Lethbridge with Tracey Skingly",
				"<p><a href=\"http://www.amazon.co.uk/exec/obidos/ASIN/B000024V1U/madmusinofme-21\"><img src=\"http://www.madmusingsof.me.uk/archives/suburbs.jpeg\" border=\"1\" title=\"Sound of the Suburbs\"alt=\"Sound of the Suburbs\" hspace=\"3\" vspace=\"3\"></a>&nbsp;A compilation of songs taken from the Golden Age of Pop",
				"<img alt=\"New Labour MPs\" src=\"http://www.andrewregan.com/images/mp/welcome.jpg\"/> Or, \"Yet another newspaper (and blogger) jumps on the bandwagon\"...<br/>\n<a href=\"http://www.mirror.co.uk/news/tm_objectid=15512533%26method=full%26siteid=94762%26headline=the-great-su-doku-challenge-name_page.html\">Have a go yourself</a> then send in your completed entry - plus a note of how long it took you - to see if you've beaten that legendary brainbox, <span style=\"font-weight:bold;\">Carol Vorderman</span> (an <a href=\"http://en.wikipedia.org/wiki/Alan_Turing\">Alan Turing</a> for a simpler age).<br/>\n<br/>I won't reveal the solution (unless you donate to the site!), but I will say that it took me <span style=\"font-weight:bold;\">15 minutes and 50 seconds.</span>\n\n<br/>\n<br/>Actually I must admit that I've been doing a hell of a lot of these recently. <a href=\"http://www.griffiths-jones.co.uk/sudoku/\">This site</a> offers a new one every day, plus there's an archive of easy, medium, hard, and very hard ones going back a month or two. You can also <a href=\"http://www.griffiths-jones.co.uk/sudoku/downloads.html\">download</a> a grid (as a PDF), which is just the right size for printing.<br/>\n<br/>If you still don't know what SuDoku is, look it up at the <a href=\"http://en.wikipedia.org/wiki/Sudoku\">Wikipedia</a>.",
				"<div xmlns=\"http://www.w3.org/1999/xhtml\">Here's Celia, with other new Labour MPs at Westminster today:<br/><br/><img alt=\"New Labour MPs\" src=\"http://www.andrewregan.com/images/mp/welcome.jpg\"/>Andrew</div>",
				"<a name=\"c111529093173103484\" />Hilary Wade",
				"A reminder: Vote early and vote often.<sup class=\"foo\" >*</sup><SUP   >*</sup>Where often is defined as more than zero times and not more than one time.",
				"I've only just caught up with this <u class=\"foo\" title=\"bar\" >splendid</u> piece by Michael Lynch in the Chronicle of Higher Education.",
				"<a href=\"apple.com\">Apple</a> same way as ...<b><abbr class=\"col\" title=\"Tosser Ex-Manager\">WTEM</ABBR>-Hello <a href=\"ms.com\">MS</a>  bye",
				"<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><style type=\"text/css\">.flickr-photo { border: solid 2px #000000; }.flickr-yourcomment { }.flickr-frame { text-align: left; padding: 3px; }.flickr-caption { font-size: 0.8em; margin-top: 0px; }</style><div class=\"flickr-frame\">	<a href=\"http://www.flickr.com/photos/yukihisa/12894905/\" title=\"photo sharing\"><img src=\"http://photos10.flickr.com/12894905_de034941bc.jpg\" class=\"flickr-photo\" alt=\"\" /></a><br />	<span class=\"flickr-caption\"><a href=\"http://www.flickr.com/photos/yukihisa/12894905/\">_DSC5760</a>, originally uploaded by <a href=\"http://www.flickr.com/people/yukihisa/\">yukihisa</a>.</span></div>				<p class=\"flickr-yourcomment\">	One of the things that always gives me hope is the behaviour of children - curious, energetic, demanding and generous. What we do to them as they grow is another matter - but the potential is always there.</p></p></div>",
				"<object width=\"425\" height=\"350\"><param name=\"movie\" value=\"http://www.youtube.com/v/79T96_WFKRM\"></param><embed src=\"http://www.youtube.com/v/79T96_WFKRM\" type=\"application/x-shockwave-flash\" width=\"425\" height=\"350\"></embed></object><br /><br /><br /><br />Leonard Cohen's song from \"Dear Heather\" album, PV by his daughter, Lorca, featuring Anjani Thomas.",
				"<p>Lets see if this works&#8230;</p>\n<p><object width=\"425\" height=\"350\"><br />\n<param name=\"movie\" value=\"http://www.youtube.com/v/z5e4L3LtYw4\"></param><embed src=\"http://www.youtube.com/v/z5e4L3LtYw4\" type=\"application/x-shockwave-flash\" width=\"425\" height=\"350\"></embed></object></p>\n<p>Hmmm&#8230; Seems to work OK, but what a lot of hoops to jump through!  I had to deactivate the WYSIWYG editing plugin AND create the post in Internet Explorer - for some reason it would not work properly in Firefox. I think I shall push my luck and try to embed the video for the single too.</p>\n<p><object width=\"425\" height=\"350\"><br />\n<param name=\"movie\" value=\"http://www.youtube.com/v/7p7XiPymhYw\"></param><embed src=\"http://www.youtube.com/v/7p7XiPymhYw\" type=\"application/x-shockwave-flash\" width=\"425\" height=\"350\"></embed></object></p>",
				"&lt;a href=\"http://eustonmanifesto.org/joomla/content/view/72/46/\">Nice paper&lt;/a> at euston, largely agree with it. Only reccomended for those with a serious case of time on hands!"
		};

		int	i = 0;

		for ( String s : testStrings)
		{
			String	escapedS = htmlescape( s, false);
			String	processedS = htmlunescape(escapedS);

			if (!s.equals(processedS))
			{
				s_Logger.info("Test string [" + i + "] failed...");
				s_Logger.info("   BEFORE: " + s);
				s_Logger.info("  ESCAPED: " + s);
				s_Logger.info("    AFTER: " + processedS);
				s_Logger.info("=============================================================================");
			}

			i++;
		}
	}
/**
     * fills the left side of a number with zeros <br>
     * e.g. zerofill(14, 3) -> "014" <br>
     * e.g. zerofill(187, 6) -> "000014"
     * Note: doesn't work with negative numbers
    public static String zerofill(int x, int d) {
	String s = "";
	switch (d) {
	case 7:
	    if (x<1000000) s += "0";
	case 6:
	    if (x<100000) s += "0";
	case 5:
	    if (x<10000) s += "0";
	case 4:
	    if (x<1000) s += "0";
	case 3:
	    if (x<100) s += "0";
	case 2:
	    if (x<10) s += "0";
	}
	return s+x;
    }
     **/

/*
    public static void printIndent(PrintWriter out, int indent) {
	out.print(indent(indent));
    }

    public static String indent(int indent) {	
	switch (indent) {
	case 8:
	    return("        ");
	case 7:
	    return("       ");
	case 6:
	    return("      ");
	case 5:
	    return("     ");
	case 4:
	    return("    ");
	case 3:
	    return("   ");
	case 2:
	    return("  ");
	case 1:
	    return(" ");
	default:
	    StringBuffer buf = new StringBuffer();	    
	    for (int i=0; i<indent; ++i) { buf.append(" "); }
	    return buf.toString();
	}
    }
    public static boolean isWhitespace(String s) {
	for (int i=0; i<s.length(); ++i) {
	    if (!Character.isWhitespace(s.charAt(i))) return false;
	}
	return true;
    }

    public static class ExecInfo {
	public int exit;
	public String stdout;
	public String stderr;
    }
*/

    /**
     * Wrapper for Runtime.exec. Takes input as a String. Times out
     * after sleep msec. Returns an object containing exit value,
     * standard output, and error output.
     * @param sleep msec to wait before terminating process
     **
    public static ExecInfo exec(String command, String input, long sleep)  throws IOException {
	Process process = null;		    
	ExecInfo info = new ExecInfo();
	try {
	    Alarm a = null;
	    if (sleep>0) {
		a = new Alarm(Thread.currentThread(), sleep);
		a.start();
	    }
			
	    process = Runtime.getRuntime().exec(command);

	    if (input != null) {
		PrintWriter pw = new PrintWriter(process.getOutputStream());
		pw.print(input);
		pw.close();
	    }
	    
	    info.stdout = IOUtils.readStream(process.getInputStream());
			
	    info.stderr = IOUtils.readStream(process.getErrorStream());
			
	    process.waitFor();
	    if (a!=null)    a.stop = true;
	}
	catch (InterruptedIOException iioe) {
	    throw new IOException("Process '" + command + "' took more than " + sleep/1000 + " sec");
	}
	catch (InterruptedException ie) {
	    throw new IOException("Process '" + command + "' took more than " + sleep/1000 + " sec");
	}

	finally {
	    if (process != null)
		process.destroy();
	}

	info.exit = process.exitValue();
	return info;
    }
    /

    /**
     * Turn "Now is the time for all good men" into "Now is the time for..."
     * @param max maximum length of result string
    public static String abbreviate(String s, int max) {
	if (s.length() < max) return s;
	// todo: break into words
	return s.substring(0, max-3) + "...";
    }
     **/

    /**
     * pad or truncate
    public static String pad(String s, int length) {
	if (s.length() < length) return s + indent(length - s.length());
	else return s.substring(0,length);
    }
     **/

    /**
     * returns the part of the second string from where it's different
     * from the first <p>
     * strdiff("i am a machine", "i am a robot") -> "robot"
     *
     **/
/*
    public static String strdiff(String s1, String s2) {
	int i;
	for (i=0; i<s1.length() && i<s2.length(); ++i) {
	    if (s1.charAt(i) != s2.charAt(i)) {
		break;
	    }
	}
	if (i<s2.length())
	    return s2.substring(i);
	return "";
    }
*/

    /**
     * count the number of occurences of ch inside s
     **/
    public static int count(String s, char ch) {
	int c=0;
	for (int i=0; i<s.length(); ++i) {
	    if (s.charAt(i) == ch) c++;
	}
	return c;
    }

    /**
     * Replace all occurences of target inside source with replacement.
     * E.g. replace("fee fie fo fum", "f", "gr") -> "gree grie gro grum"
     **/
/*
    public static String replace(String source, String target, String replacement)
    {
	// could use a regular expression, but this keeps it portable
	StringBuffer result = new StringBuffer(source.length());
	int i = 0, j = 0;
	int len = source.length();
	while (i < len) {	    
	    j = source.indexOf(target, i);
	    if (j == -1) {
		result.append( source.substring(i,len) );
		break;
	    }
	    else {
		result.append( source.substring(i,j) );
		result.append( replacement );
		i = j + target.length();
	    }
	}
	return result.toString();
    }
*/
    // see http://hotwired.lycos.com/webmonkey/reference/special_characters/
    static Object[][] entities = {
		{"#39", Integer.valueOf(39)},		// ' - apostrophe
		{"quot", Integer.valueOf(34)},		// " - double-quote
		{"amp", Integer.valueOf(38)},		// & - ampersand 
		{"lt", Integer.valueOf(60)},		// < - less-than
		{"gt", Integer.valueOf(62)},		// > - greater-than
		{"nbsp", Integer.valueOf(160)},		// non-breaking space
		{"copy", Integer.valueOf(169)},		// © - copyright
		{"reg", Integer.valueOf(174)},		// ® - registered trademark
		{"Agrave", Integer.valueOf(192)},	// À - uppercase A, grave accent
		{"Aacute", Integer.valueOf(193)},	// Á - uppercase A, acute accent
		{"Acirc", Integer.valueOf(194)},	// Â - uppercase A, circumflex accent
		{"Atilde", Integer.valueOf(195)},	// Ã - uppercase A, tilde
		{"Auml", Integer.valueOf(196)},		// Ä - uppercase A, umlaut
		{"Aring", Integer.valueOf(197)},	// Å - uppercase A, ring
		{"AElig", Integer.valueOf(198)},	// Æ - uppercase AE
		{"Ccedil", Integer.valueOf(199)},	// Ç - uppercase C, cedilla
		{"Egrave", Integer.valueOf(200)},	// È - uppercase E, grave accent
		{"Eacute", Integer.valueOf(201)},	// É - uppercase E, acute accent
		{"Ecirc", Integer.valueOf(202)},	// Ê - uppercase E, circumflex accent
		{"Euml", Integer.valueOf(203)},		// Ë - uppercase E, umlaut
		{"Igrave", Integer.valueOf(204)},	// Ì - uppercase I, grave accent
		{"Iacute", Integer.valueOf(205)},	// Í - uppercase I, acute accent
		{"Icirc", Integer.valueOf(206)},	// Î - uppercase I, circumflex accent
		{"Iuml", Integer.valueOf(207)},		// Ï - uppercase I, umlaut
		{"ETH", Integer.valueOf(208)},		// Ð - uppercase Eth, Icelandic
		{"Ntilde", Integer.valueOf(209)},	// Ñ - uppercase N, tilde
		{"Ograve", Integer.valueOf(210)},	// Ò - uppercase O, grave accent
		{"Oacute", Integer.valueOf(211)},	// Ó - uppercase O, acute accent
		{"Ocirc", Integer.valueOf(212)},	// Ô - uppercase O, circumflex accent
		{"Otilde", Integer.valueOf(213)},	// Õ - uppercase O, tilde
		{"Ouml", Integer.valueOf(214)},		// Ö - uppercase O, umlaut
		{"Oslash", Integer.valueOf(216)},	// Ø - uppercase O, slash
		{"Ugrave", Integer.valueOf(217)},	// Ù - uppercase U, grave accent
		{"Uacute", Integer.valueOf(218)},	// Ú - uppercase U, acute accent
		{"Ucirc", Integer.valueOf(219)},	// Û - uppercase U, circumflex accent
		{"Uuml", Integer.valueOf(220)},		// Ü - uppercase U, umlaut
		{"Yacute", Integer.valueOf(221)},	// Ý - uppercase Y, acute accent
		{"THORN", Integer.valueOf(222)},	// Þ - uppercase THORN, Icelandic
		{"szlig", Integer.valueOf(223)},	// ß - lowercase sharps, German
		{"agrave", Integer.valueOf(224)},	// à - lowercase a, grave accent
		{"aacute", Integer.valueOf(225)},	// á - lowercase a, acute accent
		{"acirc", Integer.valueOf(226)},	// â - lowercase a, circumflex accent
		{"atilde", Integer.valueOf(227)},	// ã - lowercase a, tilde
		{"auml", Integer.valueOf(228)},		// ä - lowercase a, umlaut
		{"aring", Integer.valueOf(229)},	// å - lowercase a, ring
		{"aelig", Integer.valueOf(230)},	// æ - lowercase ae
		{"ccedil", Integer.valueOf(231)},	// ç - lowercase c, cedilla
		{"egrave", Integer.valueOf(232)},	// è - lowercase e, grave accent
		{"eacute", Integer.valueOf(233)},	// é - lowercase e, acute accent
		{"ecirc", Integer.valueOf(234)},	// ê - lowercase e, circumflex accent
		{"euml", Integer.valueOf(235)},		// ë - lowercase e, umlaut
		{"igrave", Integer.valueOf(236)},	// ì - lowercase i, grave accent
		{"iacute", Integer.valueOf(237)},	// í - lowercase i, acute accent
		{"icirc", Integer.valueOf(238)},	// î - lowercase i, circumflex accent
		{"iuml", Integer.valueOf(239)},		// ï - lowercase i, umlaut
		{"eth", Integer.valueOf(240)},		// ð - lowercase eth, Icelandic
		{"ntilde", Integer.valueOf(241)},	// ñ - lowercase n, tilde
		{"ograve", Integer.valueOf(242)},	// ò - lowercase o, grave accent
		{"oacute", Integer.valueOf(243)},	// ó - lowercase o, acute accent
		{"ocirc", Integer.valueOf(244)},	// ô - lowercase o, circumflex accent
		{"otilde", Integer.valueOf(245)},	// õ - lowercase o, tilde
		{"ouml", Integer.valueOf(246)},		// ö - lowercase o, umlaut
		{"oslash", Integer.valueOf(248)},	// ø - lowercase o, slash
		{"ugrave", Integer.valueOf(249)},	// ù - lowercase u, grave accent
		{"uacute", Integer.valueOf(250)},	// ú - lowercase u, acute accent
		{"ucirc", Integer.valueOf(251)},	// û - lowercase u, circumflex accent
		{"uuml", Integer.valueOf(252)},		// ü - lowercase u, umlaut
		{"yacute", Integer.valueOf(253)},	// ý - lowercase y, acute accent
		{"thorn", Integer.valueOf(254)},	// þ - lowercase thorn, Icelandic
		{"yuml", Integer.valueOf(255)},		// ÿ - lowercase y, umlaut
		{"euro", Integer.valueOf(8364)},	// Euro symbol

		// 25 July 2002 ...

		{"cent", Integer.valueOf(162)},		// cent
		{"pound", Integer.valueOf(163)},	// pound sterling
		{"laquo", Integer.valueOf(171)},	// left angle quote
		{"middot", Integer.valueOf(183)},	// middle dot
		{"raquo", Integer.valueOf(187)},	// right angle quote
		{"iquest", Integer.valueOf(191)},	// inverted question mark
    };
    static Map<String,Integer> e2i = new HashMap<String,Integer>();
    static Map<Integer,String> i2e = new HashMap<Integer,String>();

    static {
	for (int i=0; i<entities.length; ++i) {
	    e2i.put( (String) entities[i][0], (Integer) entities[i][1]);
	    i2e.put( (Integer) entities[i][1], (String) entities[i][0]);	    
	}
    }

	/**
	* Turns funky characters into HTML entity equivalents<p>
	* e.g. <tt>"bread" & "butter"</tt> => <tt>&amp;quot;bread&amp;quot; &amp;amp; &amp;quot;butter&amp;quot;</tt>.
	* Update: supports nearly all HTML entities, including funky accents. See the source code for more detail.
	**/
	public static String htmlescape(String s1)
	{
		return htmlescape( s1, true);
	}

	public static String htmlescape(String s1, boolean inConvertAmp)
	{
		StringBuffer	buf = new StringBuffer();
		int		i, theLength = s1.length();

		for ( i = 0; i < theLength; ++i)
		{
			char ch = s1.charAt(i);

			if (( ch == '&') && ( i <= ( theLength - 6)) && ( s1.charAt(i + 1) == '#'))
			{
				buf.append('&');	// if it's... &#1234; ... do NOT escape the & !! 14 Sept 2001
			}
			else
			{
				String entity = (String)i2e.get( Integer.valueOf((int)ch) );	    

				// debug("char = '" + ch + "', int = " + (int)ch + ", entity = \"" + entity + "\"");

				if ( entity == null)
				{
					if (((int)ch) > 128)
					{
						// debug("over 128...");

						buf.append("&#" + ((int)ch) + ";");
					}
					else	buf.append(ch);
				}
				else if ( !inConvertAmp && entity.equals("amp"))	// (AGR) 13 April 2005
				{
					buf.append("&");
				}
				else
				{
					buf.append("&" + entity + ";");
				}
			}
		}
		return buf.toString();
	}

/*	public static StringBuffer htmlescape_buf(String s1)	// MY VERSION (AGR) 11 Sep 2001
	{
		StringBuffer	buf = new StringBuffer();
		int		i, theLength = s1.length();

		for ( i = 0; i < theLength; ++i)
		{
			char ch = s1.charAt(i);

			if (( ch == '&') && ( i <= ( theLength - 6)) && ( s1.charAt(i + 1) == '#'))
			{
				buf.append('&');	// if it's... &#1234; ... do NOT escape the & !! 14 Sept 2001
			}
			else
			{
				String entity = (String)i2e.get( Integer.valueOf((int)ch) );	    
				if ( entity == null)
				{
					if (((int)ch) > 128)
					{
						buf.append("&#" + ((int)ch) + ";");
					}
					else	buf.append(ch);
				}
				else	buf.append("&" + entity + ";");
			}
		}
		return buf;
	}
*/
	/*******************************************************************************
	*******************************************************************************/
	public static int unenscapedLength( String inStr)	// This is totally mine! 18 September 2001
	{
		int	theCount = 0;

		// println("unenscapedLength: input = \"" + inStr + "\" ................");

		for ( int i = 0; i < inStr.length(); i++)
		{
			if ( inStr.charAt(i) == '&')
			{
				int	theSemiPos = inStr.indexOf( ';', i + 2);	// can't legally be any earlier than that!

				if ( theSemiPos == -1)		// no ';' so treat as ordinary & and go through other chars as normal
				{
					theCount++;
				}
				else
				{
					String	theEntity = inStr.substring( i + 1, theSemiPos - 1);

					// println("unenscapedLength: entity = " + theEntity);

					if ( theEntity.charAt(0) == '#')	// e.g. "#12934"
					{
						if ( theEntity.length() > 1)	// "#" does not represent an acceptable character
						{
							theCount++;		// character #12934 is only one character
						}
					}
					else
					{
						Integer	iso = (Integer) e2i.get(theEntity);

						// println("unenscapedLength: iso = " + iso);

						if ( iso == null)	// this 'mystery' entity code has no number for it
						{
							theCount += ( 2 + theEntity.length());	// '&' + theEntity + ';'
						}
						else	theCount++;	// character #12934 is still only one character
					}

					i = theSemiPos;		// now skip the whole &#1234; section
				}
			}
			else	theCount++;	// ordinary non-& character
		}

		// println("unenscapedLength: returning " + theCount + " __________________-");

		return theCount;
	}

	/**
	* Reverses htmlescape.
	* @see htmlescape */
	public static String htmlunescape(String s1)
	{
		StringBuilder buf = new StringBuilder( s1.length() );

//		Matcher	strEntityMatcher = Pattern.compile( "&[A-Z]+;", Pattern.CASE_INSENSITIVE).matcher(s1);

		for ( int i = 0; i < s1.length(); ++i)
		{
			char ch = s1.charAt(i);
			if (ch == '&')
			{
				int semi = s1.indexOf(';', i+1);

				// s_Logger.info("i = " + i + ", semi = " + semi);

				if (semi == -1)
				{
					buf.append(ch);
					continue;
				}

				String entity = s1.substring(i+1, semi); // -1);

				// s_Logger.info("entity=\"" + entity + "\"");

				Integer iso;
				if ( entity.charAt(0) == '#')
				{
					// s_Logger.info("sub=\"" + entity.substring(1) + "\"");

					iso = Integer.valueOf( entity.substring(1) );
				}
				else
				{
					iso = (Integer)e2i.get(entity);
				}

				////////////////////////////////////////////////

				if ( iso == null)
				{
					buf.append("&" + entity + ";");
				}
				else
				{
					buf.append((char) iso.intValue() );

					i = semi;	// (AGR) 28 August 2006
				}
			}
			else
			{
				buf.append(ch);
			}
		}

		return buf.toString();
	}

    /**
     *  Filter out Windows and Mac curly quotes, replacing them with
     *  the non-curly versions. Note that this doesn't actually do any
     *  checking to verify the input codepage. Instead it just
     *  converts the more common code points used on the two platforms
     *  to their equivalent ASCII values. As such, this method
     *  <B>should not be used</b> on ISO-8859-1 input that includes
     *  high-bit-set characters, and some text which uses other
     *  codepoints may be rendered incorrectly.
     *
     * @author Ian McFarland
    public static String uncurlQuotes(String input)
    {
	if (input==null)
	    return "";
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < input.length(); i++)
	{
	    char ch = input.charAt(i);
	    int code = (int) ch;
	    if (code == 210 || code == 211 || code == 147 || code == 148)
	    {
		ch = (char) 34; // double quote
	    }
	    else if (code == 212 || code == 213 || code == 145 || code == 146)
	    {
		ch = (char) 39; // single quote
	    }
	    sb.append(ch);	
	}
	return sb.toString();
    }
     **/

    /**
     * capitalize the first character of s
    public static String capitalize(String s) {
	return s.substring(0,1).toUpperCase() + s.substring(1);
    }
     **/

    /**
     * lowercase the first character of s
    public static String lowerize(String s) {
	return s.substring(0,1).toLowerCase() + s.substring(1);
    }
     **/

    /**
     * turn String s into a plural noun (doing the right thing with
     * "story" -> "stories" and "mess" -> "messes")
    public static String pluralize(String s) {
	if (s.endsWith("y"))
	    return s.substring(0, s.length()-1) + "ies";

	else if (s.endsWith("s"))
	    return s + "es";

	else
	    return s + "s";
    }
     **/

/*
    public static boolean ok(String s) {
      return (!(s == null || s.equals("")));
    }

    public static String toUnderscore(String s) {
	StringBuffer buf = new StringBuffer();
	char[] ch = s.toCharArray();
	for (int i=0; i<ch.length; ++i) {
	    if (Character.isUpperCase(ch[i])) {
		buf.append('_');
		buf.append(Character.toLowerCase(ch[i]));
	    }
	    else {
		buf.append(ch[i]);
	    }
	}
	//System.err.println(s + " -> " + buf.toString());
	return buf.toString();
    }
    
    public static String getStackTrace(Throwable t) {
	StringWriter s = new StringWriter();
	PrintWriter p = new PrintWriter(s);
	t.printStackTrace(p);
	p.close();
	return s.toString();
    }
*/
    public static void sleep(long msec) {
	try {
	    Thread.sleep(msec);
	}		
	catch (InterruptedException ie) {}	
    }	
}
