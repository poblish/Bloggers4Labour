/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.bridge.channel.item;

import org.bloggers4labour.bridge.channel.ChannelIF;

/**
 *
 * @author andrewregan
 */
public class DefaultItemBridgeFactory implements ItemBridgeFactoryIF
{
	/*******************************************************************************
	*******************************************************************************/
	public ItemBridgeIF getInstance()
	{
		return new ItemBridgeIF()
		{
			/*******************************************************************************
			*******************************************************************************/
			public ItemIF bridge( final de.nava.informa.core.ItemIF inOriginal)
			{
				if ( inOriginal instanceof ItemIF)
				{
					return (ItemIF) inOriginal;
				}

				return new DefaultItemImpl(inOriginal);
			}

			/*******************************************************************************
			*******************************************************************************/
			public ItemIF bridge( final de.nava.informa.core.ItemIF inOriginal, ChannelIF inChannel)
			{
				if ( inOriginal instanceof ItemIF)
				{
					return (ItemIF) inOriginal;
				}

				return new DefaultItemImpl( inOriginal, inChannel);
			}

			/*******************************************************************************
			*******************************************************************************/
			public de.nava.informa.core.ItemIF bridge( final ItemIF inOurs)
			{
				return inOurs;
			}
		};
	}
}
