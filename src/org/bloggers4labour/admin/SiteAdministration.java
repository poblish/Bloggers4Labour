/*
 * SiteAdministration.java
 *
 * Created on 27 May 2006, 09:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.admin;

import com.hiatus.USQL_Utils;
import com.hiatus.sql.ResultSetList;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.bloggers4labour.*;
import org.bloggers4labour.conf.Configuration;
import org.bloggers4labour.sql.*;

/**
 *
 * @author andrewre
 */
public class SiteAdministration
{
	private static Logger		s_Logger = Logger.getLogger("Main");

	private final static String	LINE_PREFIX = ">>> SiteAdmin.cleanup(): ";

	/*******************************************************************************
	*******************************************************************************/
	public static void main( String[] args)
	{
		Configuration.getInstance().setDirectoryIfNotSet("/Users/andrewre/www/htdocs/bloggers4labour/conf/");

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
				// s_FL_Logger.info("conn = " + theConnectionObject);

				Statement	theS = null;

				try
				{
					SiteAdministration	sa = new SiteAdministration();

					theS = theConnectionObject.createStatement();

					for ( int siteRecno = 200; siteRecno > 0; siteRecno--)
					{
						sa.deleteSite( theS, siteRecno, true);
					}
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

	/*******************************************************************************
	*******************************************************************************/
	public SiteAdministration()
	{
	}

	/*******************************************************************************
	*******************************************************************************/
	public ResultSet getUnapprovedBlogs( Statement inStatement, int inSiteRecno, boolean inCommitChanges) throws SQLException
	{
		ResultSet	theRS = inStatement.executeQuery( QueryBuilder.getUnapprovedBlogsQuery() );

		return theRS; // new ResultSetList(theRS);
	}

	/*******************************************************************************
	*******************************************************************************/
	public void deleteSite( Statement inStatement, int inSiteRecno, boolean inCommitChanges) throws SQLException
	{
		ResultSet	theRS = inStatement.executeQuery("SELECT site_recno FROM site WHERE site_recno=" + inSiteRecno);

		if (theRS.next())
		{
			if (inCommitChanges)
			{
				int	numDeletions = inStatement.executeUpdate("DELETE FROM site WHERE site_recno=" + inSiteRecno);

				if ( numDeletions > 0)
				{
					s_Logger.info(">>> SiteAdmin.deleteSite(): Site " + inSiteRecno + " deleted OK.");
				}
				else
				{
					s_Logger.error(">>> SiteAdmin.deleteSite(): Site " + inSiteRecno + " deletion FAILED.");
				}
			}
			else
			{
				s_Logger.info(">>> SiteAdmin.deleteSite(): Site " + inSiteRecno + " found OK.");
			}
		}
		else
		{
			s_Logger.warn(">>> SiteAdmin.deleteSite(): Site " + inSiteRecno + " not found.");
		}

		cleanup( inStatement, inCommitChanges);
	}

	/*******************************************************************************
	*******************************************************************************/
	public void cleanup( Statement inStatement, boolean inCommitChanges) throws SQLException
	{
		ResultSet	rs = inStatement.executeQuery("SELECT site_recno FROM site");
		StringBuffer	srBuf = new StringBuffer(500);
		int		srCount = 0;

		while (rs.next())
		{
			if ( srBuf.length() < 1)
			{
				srBuf.append("(" + rs.getLong(1));
			}
			else	srBuf.append("," + rs.getLong(1));

			srCount++;
		}

		if ( srBuf.length() >= 1)
		{
			srBuf.append(")");
		}

		// %><p>Sites: <%= srBuf %> = <%= srCount %> "site" recs</p><%

		////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////

		rs = inStatement.executeQuery("SELECT creator_recno FROM creator");

		List<Number>	crList = new ArrayList<Number>(100);

		while (rs.next())
		{
			crList.add( new Long( rs.getLong("creator_recno") ) );
		}

		// %><p><%= crList %> = <%= crList.size() %> "creator" recs</p><%

		////////////////////////////////////////////////////////////////////////

		rs = inStatement.executeQuery("SELECT creator_recno FROM siteCreators");

		List	crList2 = new ArrayList(100);

		while (rs.next())
		{
			crList2.add( new Long( rs.getLong("creator_recno") ) );
		}

		// %><p><%= crList2 %> = <%= crList2.size() %> "creator" recs</p><%

		////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////

		rs = inStatement.executeQuery("SELECT * FROM siteCreators WHERE site_recno NOT IN " + srBuf);

		StringBuffer	scrBuf = new StringBuffer(500);
		int		scrCount = 0;

		while (rs.next())
		{
			if ( scrBuf.length() < 1)
			{
				scrBuf.append("(" + rs.getLong(1));
			}
			else	scrBuf.append("," + rs.getLong(1));

			scrCount++;
		}

		if ( scrBuf.length() >= 1)
		{
			scrBuf.append(")");
		}

		// %><p><%= scrBuf %> = <%= scrCount %> "siteCreators" recs to delete</p><%

		////////////////////////////////////////////////////////////////////////

		if ( scrCount > 0)
		{
			String	delSCQuery = "DELETE FROM siteCreators WHERE site_creator_recno IN " + scrBuf;

			if (inCommitChanges)
			{
				int	numUpdates = inStatement.executeUpdate(delSCQuery);
				s_Logger.info( LINE_PREFIX + numUpdates + " updates from running \"" + delSCQuery + "\"");
			}
			else
			{
				s_Logger.info( LINE_PREFIX + "Changes required: " + delSCQuery);
			}
		}

		////////////////////////////////////////////////////////////////////////

		StringBuffer	crDelBuf = new StringBuffer(500);
		int		crDelCount = 0;

		for ( Number each : crList)
		{
			if (!crList2.contains(each))
			{
				if ( crDelBuf.length() < 1)
				{
					crDelBuf.append("(" + each);
				}
				else	crDelBuf.append("," + each);

				crDelCount++;
			}
		}

		if ( crDelBuf.length() >= 1)
		{
			crDelBuf.append(")");
		}

		// %><p><%= crDelBuf %> = <%= crDelCount %> "creator" recs to delete</p><%

		if ( crDelCount > 0)
		{
			String	q = "DELETE FROM creator WHERE creator_recno IN " + crDelBuf;

			if (inCommitChanges)
			{
				int	numUpdates = inStatement.executeUpdate(q);
				s_Logger.info( LINE_PREFIX + numUpdates + " updates from running \"" + q + "\"");
			}
			else
			{
				s_Logger.info( LINE_PREFIX + "Changes required: " + q);
			}
		}
		else if ( scrCount == 0)
		{
			s_Logger.debug( LINE_PREFIX + "No cleanup required.");
		}
	}
}
