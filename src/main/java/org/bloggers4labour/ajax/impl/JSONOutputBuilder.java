/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.ajax.impl;

import com.hiatus.text.UText;
import java.util.Map;
import org.bloggers4labour.ajax.*;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author andrewregan
 */
public class JSONOutputBuilder extends AbstractOutputBuilder
{
	private String	m_CallbackFunctionName;

	/*******************************************************************************
	*******************************************************************************/
	public JSONOutputBuilder( final String inRootName, final String inCallbackFunctionName)
	{
		super(inRootName);

		m_CallbackFunctionName = inCallbackFunctionName;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setContentType( final HttpServletResponse inResponse)
	{
		inResponse.setContentType("text/plain; charset=\"UTF-8\"");
	}

	/*******************************************************************************
	*******************************************************************************/
	public StringBuilder complete()
	{
		if (UText.isValidString(m_CallbackFunctionName))	// Got one?
		{
			StringBuilder	theBuilder = new StringBuilder(1000);

			return theBuilder.append(m_CallbackFunctionName).append("(").append( m_Root.complete() ).append(")");
		}

		return m_Root.complete();
	}

	/*******************************************************************************
	*******************************************************************************/
	public OutputElementIF newElement( final String inName, final Map<String,Object> inAttrs)
	{
		return new JSONELement( inName, inAttrs);
	}

	/*******************************************************************************
	*******************************************************************************/
	public OutputElementIF newWrapperElement( final String inName)
	{
		return new JSONListElement(inName);
	}

	/*******************************************************************************
	*******************************************************************************/
	private static class JSONELement extends AbstractOutputElement
	{
		private boolean	m_AddedElements = false;

		/*******************************************************************************
		*******************************************************************************/
		public JSONELement( final String inName, final Map<String,Object> inAttrs)
		{
			m_Builder.append("{\"").append(inName).append("\":");

			if ( inAttrs != null)
			{
				for ( Map.Entry<String,Object> eachAttr : inAttrs.entrySet())
				{
					_addProperty( eachAttr.getKey(), eachAttr.getValue());
				}
			}
		}

		/*******************************************************************************
		*******************************************************************************/
		@Override public void addCDataElement( final String inElementName, final Object inContent)
		{
			addElement( inElementName, inContent);
		}

		/*******************************************************************************
		*******************************************************************************/
		public void add( final OutputElementIF inElement)
		{
			if (m_AddedElements)
			{
				m_Builder.append(",");
			}
			else
			{
				m_AddedElements = true;
			}

			m_Builder.append( inElement.complete() );
		}

		/*******************************************************************************
		*******************************************************************************/
		@Override public void addElement( final String inElementName, final Object inContent)
		{
			_addProperty( inElementName, inContent);
		}

		/*******************************************************************************
		*******************************************************************************/
		private void _addProperty( final String inName, final Object inContent)
		{
			if (m_AddedElements)
			{
				m_Builder.append(",");
			}
			else
			{
				m_AddedElements = true;

				m_Builder.append(" {");
			}

			if ( inContent instanceof String)
			{
				StringBuffer	theBuf = new StringBuffer((String) inContent);

				theBuf = UText.replaceMatches( UText.replaceMatches( UText.replaceMatches( theBuf, "\"", "\\\""), "\n", " "), "\r", " ");

				m_Builder.append("\"").append(inName).append("\":\"").append(theBuf).append("\"");
			}
			else
			{
				m_Builder.append("\"").append(inName).append("\":\"").append(inContent).append("\"");
			}
		}

		/*******************************************************************************
		*******************************************************************************/
		@Override public StringBuilder complete()
		{
			if (m_AddedElements)
			{
				m_Builder.append("}");
			}

			return m_Builder.append("}");
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private static class JSONListElement extends AbstractOutputElement
	{
		private boolean	m_AddedElements = false;

		/*******************************************************************************
		*******************************************************************************/
		public JSONListElement( final String inName)
		{
			m_Builder.append("\"").append(inName).append("\": [");
		}

		/*******************************************************************************
		*******************************************************************************/
		public void add( final OutputElementIF inElement)
		{
			if (m_AddedElements)
			{
				m_Builder.append(",");
			}
			else
			{
				m_AddedElements = true;
			}

			m_Builder.append( inElement.complete() );
		}

		/*******************************************************************************
		*******************************************************************************/
		@Override public StringBuilder complete()
		{
			return m_Builder.append("]");
		}
	}
}