/*
 * DataSourceConnTag_Not.java
 *
 * Created on 02 April 2006, 00:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.sql;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;
import org.apache.log4j.Logger;

/**
 *
 * @author andrewre
 */
public class DataSourceConnTag_Not extends BodyTagSupport
{
	private Logger		m_Logger;

	private static final long serialVersionUID = 1L;

	/*******************************************************************************
	*******************************************************************************/
	public DataSourceConnTag_Not()
	{
		super();
		m_Logger = Logger.getLogger( getClass() );

//		m_Logger.debug("in DataSourceConnTag_Not ctor");
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public int doStartTag() throws JspException
	{
//		m_Logger.debug("in doStartTag()");

		int	theResult = BodyTag.SKIP_BODY;

		try
		{
			DataSourceConnTag	theParentTag;
			theParentTag = (DataSourceConnTag) findAncestorWithClass( this, Class.forName("org.bloggers4labour.sql.DataSourceConnTag") );

			if (( theParentTag != null) && ( theParentTag.getConnectionOpened() != null) && !theParentTag.isConnectedOK())
			{
				theResult = EVAL_BODY_BUFFERED;

				m_Logger.debug("NOT connected - ok, include body");
			}
		} 
		catch (Exception err)
		{
			m_Logger.error( "DataSourceConnTag_Not::doStartTag()", err);
		}

		return theResult;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public int doEndTag() throws JspException
	{
//		m_Logger.debug("in doEndTag()");

		return BodyTag.EVAL_PAGE;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public int doAfterBody() throws JspException
	{
		BodyContent	theBody = getBodyContent();

		try
		{
			// m_Logger.debug("in doAfterBody()");

			theBody.writeOut( getPreviousOut() );
		}
		catch (IOException err)
		{
			m_Logger.error("in DataSourceConnTag_Not::doAfterBody()", err);
		}

		theBody.clearBody();

		return BodyTag.SKIP_BODY;
	}
}
