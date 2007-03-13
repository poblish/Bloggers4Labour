/*
 * DataSourceConnTag.java
 *
 * Created on 02 April 2006, 00:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.sql;

import com.hiatus.UText;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.InstallationManager;

/**
 *
 * @author andrewre
 */
public class DataSourceConnTag extends TagSupport implements TryCatchFinally
{
	private String			m_InstallationStr;
	private String			m_InstallName;		// (AGR) 19 Feb 2007
	private Logger			m_Logger;

	private DataSourceConnection	m_Connection;
	private boolean			m_ConnectedOK;
	private Exception		m_Exception;

	private static Logger		s_Logger = Logger.getLogger("Main");

	/*******************************************************************************
	*******************************************************************************/
	public DataSourceConnTag()
	{
		super();

		s_Logger = Logger.getLogger( getClass() );
		// System.out.println("in DataSourceConnTag ctor");
	}

	/*******************************************************************************
	*******************************************************************************/
	public DataSourceConnection getConnectionOpened()
	{
		return m_Connection;
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean isConnectedOK()
	{
		return m_ConnectedOK;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Exception getExceptionThrown()
	{
		return m_Exception;
	}

	/*******************************************************************************
		from TagLib descriptor
 	*******************************************************************************/
	public void setInstallation( String x)
	{
		m_InstallationStr = x;
	}

	/*******************************************************************************
		from TagLib descriptor
 	*******************************************************************************/
	public String getInstallation()
	{
		return m_InstallationStr;
	}

	/*******************************************************************************
 		from TagLib descriptor. (AGR) 19 Feb 2007
 	*******************************************************************************/
	public void setInstallName( String x)
	{
		m_InstallName = x;
	}

	/*******************************************************************************
		from TagLib descriptor. (AGR) 19 Feb 2007
 	*******************************************************************************/
	public String getInstallName()
	{
		return m_InstallName;
	}

	/*******************************************************************************
 	*******************************************************************************/
	public int doStartTag() throws JspException
	{
		// System.out.println("in doStartTag(), m_InstallationStr = " + m_InstallationStr);

		try
		{
			////////////////////////////////////////////////////////  (AGR) 19 Feb 2007

			if (UText.isValidString(m_InstallName))
			{
				InstallationIF theInstall = InstallationManager.getInstallation(m_InstallName);
				if ( theInstall != null)
				{
					m_Connection = new DataSourceConnection( theInstall.getDataSource() );
					// System.out.println("Using new DSC  with a " + theInstall.getDataSource());
					m_ConnectedOK = m_Connection.Connect();

					return BodyTag.EVAL_BODY_INCLUDE;
				}
			}

			////////////////////////////////////////////////////////

		//	System.out.println("in doStartTag(), def = " + InstallationManager.getDefaultInstallation());
		//	System.out.println("install = " + InstallationManager.getInstallation(m_InstallationStr));
		//	DataSource	theDS = InstallationManager.getInstallation(m_InstallationStr).getDataSource();

			MysqlDataSource	theDS = new MysqlDataSource();

			if (UText.isValidString(m_InstallationStr))
			{
				theDS.setUrl(m_InstallationStr);
			}
		//	System.out.println("theDS = " + theDS);

			m_Connection = new DataSourceConnection(theDS);
		//	System.out.println("m_Connection = " + m_Connection);
			m_ConnectedOK = m_Connection.Connect();

	//		System.out.println("in doStartTag(), conn = " + m_Connection.getConnection() + ", " + m_ConnectedOK);
		} 
		catch ( Exception err)
		{
			m_Exception = err;
		//	m_Logger.debug("store exception = " + err);
			err.printStackTrace( System.out );
		}

		return BodyTag.EVAL_BODY_INCLUDE;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int doEndTag() throws JspException
	{
		s_Logger.debug("in doEndTag(), conn = " + m_Connection);

		releaseConnection();

		return BodyTag.EVAL_PAGE;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void doCatch( Throwable inThrowable)
	{
		s_Logger.error( "doCatch()", inThrowable);
	}

	/*******************************************************************************
	*******************************************************************************/
	public void doFinally()
	{
		releaseConnection();
	}

	/*******************************************************************************
	*******************************************************************************/
	private void releaseConnection()
	{
		if ( m_Connection != null)
		{
			m_Connection.CloseDown();
			m_Connection = null;
		}

		m_ConnectedOK = false;	// (AGR) 22 Feb 2006
	}
}