/*
 * PreferenceBitmap.java
 *
 * Created on 29 May 2007, 20:53
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.login;

/**
 *
 * @author andrewre
 */
public enum PreferenceBits
{
	FRONTPAGE_DISPLAYPOSTS_DEFAULT		(0),
	FRONTPAGE_DISPLAYPOSTS_1		(1),
	FRONTPAGE_DISPLAYPOSTS_2		(2),
	FRONTPAGE_DISPLAYPOSTS_3		(3),
	FRONTPAGE_DISPLAYPOSTS_20		(20),

	FRONTPAGE_DISPLAYPOSTS_STYLE_DEFAULT	(21),
	FRONTPAGE_DISPLAYPOSTS_STYLE_ALT1	(22),
	FRONTPAGE_DISPLAYPOSTS_STYLE_ALT2	(23),
	FRONTPAGE_DISPLAYPOSTS_STYLE_ALT9	(30);

	private final int	m_Bit;

	/*******************************************************************************
	*******************************************************************************/
	PreferenceBits( int inBit)
	{
		m_Bit = inBit;
	}

	public int getBit() { return m_Bit; }
}
