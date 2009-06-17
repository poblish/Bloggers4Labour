/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.bridge.channel;

/**
 *
 * @author andrewregan
 */
public class DefaultChannelBridgeFactory implements ChannelBridgeFactoryIF
{
	/*******************************************************************************
	*******************************************************************************/
	public ChannelBridgeIF getInstance()
	{
		return new ChannelBridgeIF()
		{
			/*******************************************************************************
			*******************************************************************************/
			public ChannelIF bridge( de.nava.informa.core.ChannelIF inOriginal)
			{
				return new DefaultChannelImpl(inOriginal);
			}

			/*******************************************************************************
			*******************************************************************************/
			public de.nava.informa.core.ChannelIF bridge( ChannelIF inOriginal)
			{
				return inOriginal;
			}
		};
	}
}
