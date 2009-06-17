/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.bridge.cats;

import de.nava.informa.core.CategoryIF;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author andrewregan
 */
public class DefaultCategoryImpl implements CategoryIF
{
	private long		m_Id;
	private String		m_Title;

	/*******************************************************************************
	*******************************************************************************/
	public DefaultCategoryImpl( final de.nava.informa.core.CategoryIF inCategory)
	{
		m_Id = inCategory.getId();
		m_Title = inCategory.getTitle();
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public boolean equals(Object obj)
	{
		if (!(obj instanceof CategoryIF))
		{
			return false;
		}

		CategoryIF cmp = (CategoryIF) obj;

		return cmp.getTitle().equals(m_Title) && ( cmp.getId() == m_Id);
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public int hashCode()
	{
		return m_Title.hashCode() + Long.valueOf(m_Id).hashCode();
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public String toString()
	{
		return "[Category (" + m_Id + "): " + m_Title + "]";
	}

	/*******************************************************************************
	*******************************************************************************/
	public CategoryIF getParent()
	{
		return null;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setParent( final de.nava.informa.core.CategoryIF parent)
	{
		// NOOP
	}

	/*******************************************************************************
	*******************************************************************************/
	public void addChild( final de.nava.informa.core.CategoryIF child)
	{
		// NOOP
	}

	/*******************************************************************************
	*******************************************************************************/
	public void removeChild( final de.nava.informa.core.CategoryIF child)
	{
		// NOOP
	}

	/*******************************************************************************
	*******************************************************************************/
	public long getId()
	{
		return m_Id;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setId( final long inId)
	{
		m_Id = inId;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getTitle()
	{
		return m_Title;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setTitle( String inTitle)
	{
		m_Title = inTitle;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Collection getChildren()
	{
		return Collections.emptyList();
	}
}
