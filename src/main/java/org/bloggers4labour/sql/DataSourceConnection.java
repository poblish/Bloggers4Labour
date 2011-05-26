/*
 * DataSourceConnection.java
 *
 * Created on 19 February 2006, 10:32
 *
 * Based upon "com.hiatus.SQL_Connection.java". Created by AGR on 24th August 2000
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.sql;

import com.hiatus.sql.USQL_Utils;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

/**
 *
 * @author andrewre
 */
public class DataSourceConnection
{
	private DataSource	m_DataSource;
	private Connection	m_DbConnection = null;

	private static Logger	s_Logger = Logger.getLogger( DataSourceConnection.class );

	/*******************************************************************************
	*******************************************************************************/
	public DataSourceConnection( DataSource inDataSource)
	{
		m_DataSource = inDataSource;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Connection getConnection()
	{
		return m_DbConnection;
	}

	/*******************************************************************************
	*******************************************************************************/
	private boolean isConnected()
	{
		if ( m_DbConnection == null)
		{
			return false;
		}
		else
		{
			try
			{
				if (m_DbConnection.isClosed())
				{
					return false;
				}
			}
			catch (SQLException err)
			{
				s_Logger.error("???", err);
				return false;
			}
		}

		return true;
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean Connect()
	{
		try
		{
			m_DbConnection = m_DataSource.getConnection();

			return ( m_DbConnection != null);
		}
		catch (NullPointerException err)
		{
			if ( s_Logger != null)
			{
				s_Logger.error("*** Could not open Connection: MySQL is dead.");
			}
			else
			{
				System.err.println("Could not open Connection: MySQL is dead.");
			}
		}
		catch (Exception err)
		{
			s_Logger.error( "*** Could NOT open Connection!", err);
		}

		return false;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Deprecated public long GetIdentity() throws SQLException
	{
		long	theAnswer = -999;

		if (isConnected())
		{
			Statement	theStatement = createStatement();
			ResultSet	theResults = theStatement.executeQuery("SELECT LAST_INSERT_ID()");

			if (theResults.next())
			{
				theAnswer = theResults.getLong(1);
			}
			else
			{
				throw new SQLException("LAST_INSERT_ID() returned nothing!");
			}

			USQL_Utils.closeStatement(theStatement);
		}

		return theAnswer;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Statement createStatement() throws SQLException
	{
		if (isConnected())
		{
			return m_DbConnection.createStatement();
		}
		else
		{
			throw new SQLException("Cannot create Statement - we're not connected!");
		}
	}

	/*******************************************************************************
		6 May 2002
	*******************************************************************************/
	public PreparedStatement prepareStatement( String inQuery) throws SQLException
	{
		if (isConnected())
		{
			return m_DbConnection.prepareStatement(inQuery);
		}
		else
		{
			throw new SQLException("Cannot create PreparedStatement - we're not connected!");
		}
	}

	/*******************************************************************************
		29 November 2006
	*******************************************************************************/
	public CallableStatement prepareCall( String inQueryStr) throws SQLException
	{
		if (isConnected())
		{
			return m_DbConnection.prepareCall("{call " + inQueryStr + "}");
		}
		else
		{
			throw new SQLException("Cannot create CallableStatement - we're not connected!");
		}
	}

	/*******************************************************************************
		1 April 2002
	*******************************************************************************/
	public Statement createStatement( int inResultSetType, int inResultSetConcurrency) throws SQLException
	{
		if (isConnected())
		{
			return m_DbConnection.createStatement( inResultSetType, inResultSetConcurrency);
		}
		else
		{
			throw new SQLException("Cannot create Statement(2) - we're not connected!");
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean CloseDown()
	{
		if (isConnected())
		{
			boolean	theResult;

			try
			{
				m_DbConnection.close();		// returns the Connection to the pool
				theResult = true;
			}
			catch (Exception theErr)
			{
				Logger.getLogger("Main").error( "*** Could NOT close Connection!", theErr);

				theResult = false;
			}
			finally
			{
				m_DbConnection = null;
			}

			return theResult;
		}
		else
		{
			return false;
		}
	}

	/*******************************************************************************
		28 April 2007
	*******************************************************************************/
	public void setAutoCommit( boolean x) throws SQLException
	{
		if (isConnected())
		{
			m_DbConnection.setAutoCommit(x);
		}
		else
		{
			throw new SQLException("Cannot setAutoCommit() - we're not connected!");
		}
	}

	/*******************************************************************************
		28 April 2007
	*******************************************************************************/
	public void commit() throws SQLException
	{
		if (isConnected())
		{
			m_DbConnection.commit();
		}
		else
		{
			throw new SQLException("Cannot commit() - we're not connected!");
		}
	}

	/*******************************************************************************
		28 April 2007
	*******************************************************************************/
	public void rollback() throws SQLException
	{
		if (isConnected())
		{
			m_DbConnection.rollback();
		}
		else
		{
			throw new SQLException("Cannot commit() - we're not connected!");
		}
	}
}
