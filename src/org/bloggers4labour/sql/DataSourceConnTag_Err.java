/*
 * DataSourceConnTag_Err.java
 *
 * Created on 02 April 2006, 00:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.sql;

import java.io.IOException;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import org.apache.log4j.Logger;

/**
 *
 * @author andrewre
 */
public class DataSourceConnTag_Err extends BodyTagSupport
{
	private DataSourceConnTag	m_ParentTag;
//	private String			m_ExceptionStr;
	private Logger			m_Logger;

	/*******************************************************************************
	*******************************************************************************/
	public DataSourceConnTag_Err()
	{
		super();

		m_Logger = Logger.getLogger( getClass() );
	}

	/*******************************************************************************
	*******************************************************************************/
	public int doStartTag() throws JspException
	{
//		m_Logger.debug("in doStartTag()");

		pageContext.removeAttribute("ds_exception");	// m_ExceptionStr

		int	theResult = BodyTag.SKIP_BODY;

		try
		{
			if ( m_ParentTag == null)
			{
				m_ParentTag = (DataSourceConnTag) findAncestorWithClass( this, Class.forName("org.bloggers4labour.sql.DataSourceConnTag") );
				// m_Logger.debug("m_ParentTag = " + m_ParentTag);
			}

			if ( m_ParentTag.getExceptionThrown() != null)
			{
				theResult = EVAL_BODY_BUFFERED;

				pageContext.setAttribute( "ds_exception", m_ParentTag.getExceptionThrown());

				m_Logger.debug("threw exception - ok, include body");
			}
		} 
		catch (Exception err)
		{
			m_Logger.error( "DataSourceConnTag_Err::doStartTag()", err);
		}

		return theResult;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int doEndTag() throws JspException
	{
//		m_Logger.debug("in doEndTag()");

		return BodyTag.EVAL_PAGE;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int doAfterBody() throws JspException
	{
		BodyContent	theBody = getBodyContent();

		try
		{
			// m_Logger.debug("in doAfterBody()");

			theBody.writeOut( getPreviousOut() );
		}
		catch (IOException err)
		{
			m_Logger.error("in DataSourceConnTag_Err::doAfterBody()", err);
		}

		theBody.clearBody();

		return BodyTag.SKIP_BODY;
	}
}
