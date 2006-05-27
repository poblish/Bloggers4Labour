/*
 * SiteAdministration.java
 *
 * Created on 27 May 2006, 09:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.admin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

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
	public SiteAdministration()
	{
	}

	/*******************************************************************************
	*******************************************************************************/
	public void cleanup( Connection inConn, boolean inCommitChanges) throws SQLException
	{
		Statement	s = inConn.createStatement();
		ResultSet	rs = s.executeQuery("SELECT site_recno FROM site");
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

		rs = s.executeQuery("SELECT creator_recno FROM creator");

		List<Number>	crList = new ArrayList<Number>(100);

		while (rs.next())
		{
			crList.add( new Long( rs.getLong("creator_recno") ) );
		}

		// %><p><%= crList %> = <%= crList.size() %> "creator" recs</p><%

		////////////////////////////////////////////////////////////////////////

		rs = s.executeQuery("SELECT creator_recno FROM siteCreators");

		List	crList2 = new ArrayList(100);

		while (rs.next())
		{
			crList2.add( new Long( rs.getLong("creator_recno") ) );
		}

		// %><p><%= crList2 %> = <%= crList2.size() %> "creator" recs</p><%

		////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////

		rs = s.executeQuery("SELECT * FROM siteCreators WHERE site_recno NOT IN " + srBuf);

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
				int	numUpdates = s.executeUpdate(delSCQuery);
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
				int	numUpdates = s.executeUpdate(q);
				s_Logger.info( LINE_PREFIX + numUpdates + " updates from running \"" + q + "\"");
			}
			else
			{
				s_Logger.info( LINE_PREFIX + "Changes required: " + q);
			}
		}
		else if ( scrCount == 0)
		{
			s_Logger.info( LINE_PREFIX + "No cleanup required.");
		}
	}
}
