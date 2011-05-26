/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.ajax.impl;

import org.bloggers4labour.ajax.*;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author andrewregan
 */
public class XMLOutputBuilder extends AbstractOutputBuilder
{
	/*******************************************************************************
	*******************************************************************************/
	public XMLOutputBuilder( final String inRootName)
	{
		super(inRootName);
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setContentType( final HttpServletResponse inResponse)
	{
		inResponse.setContentType("text/xml; charset=\"UTF-8\"");
	}

	/*******************************************************************************
	*******************************************************************************/
	public StringBuilder complete()
	{
		StringBuilder	theBuilder = new StringBuilder("<?xml version=\"1.0\" ?>");

		return theBuilder.append( m_Root.complete() );
	}

	/*******************************************************************************
	*******************************************************************************/
	public OutputElementIF newElement( final String inName, final Map<String,Object> inAttrs)
	{
		return new XMLELement( inName, inAttrs);
	}

	/*******************************************************************************
	*******************************************************************************/
	public OutputElementIF newWrapperElement( final String inName)
	{
		return new IgnoreElement();
	}

	/*******************************************************************************
	*******************************************************************************/
	private static class XMLELement extends AbstractOutputElement
	{
		private String	m_Name;

		/*******************************************************************************
		*******************************************************************************/
		public XMLELement( final String inName, final Map<String,Object> inAttrs)
		{
			m_Name = inName;

			m_Builder.append("<").append(inName);

			if ( inAttrs != null)
			{
				for ( Map.Entry<String,Object> eachAttr : inAttrs.entrySet())
				{
					m_Builder.append(" ").append( eachAttr.getKey() ).append("=\"").append( eachAttr.getValue() ).append("\"");
				}
			}

			m_Builder.append(">");
		}

		/*******************************************************************************
			FIXME
		*******************************************************************************/
		@Override public void addElement( final String inElementName, final Object inContent)
		{
			m_Builder.append("<" + inElementName + ">").append(inContent).append("</" + inElementName + ">");
		}

		/*******************************************************************************
		*******************************************************************************/
		public void add( final OutputElementIF inElement)
		{
			m_Builder.append( inElement.complete() );
		}

		/*******************************************************************************
			FIXME
		*******************************************************************************/
		@Override public void addCDataElement( final String inElementName, final Object inContent)
		{
			m_Builder.append("<" + inElementName + "><![CDATA[").append(inContent).append("]]></" + inElementName + ">");
		}

		/*******************************************************************************
		*******************************************************************************/
		@Override public StringBuilder complete()
		{
			return m_Builder.append("</").append(m_Name).append(">");
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private static class IgnoreElement extends AbstractOutputElement
	{
		/*******************************************************************************
		*******************************************************************************/
		public void add( final OutputElementIF inElement)
		{
			m_Builder.append( inElement.complete() );
		}
	}
}