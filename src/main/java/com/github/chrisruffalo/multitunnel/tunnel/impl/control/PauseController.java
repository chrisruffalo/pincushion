package com.github.chrisruffalo.multitunnel.tunnel.impl.control;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.atomic.AtomicBoolean;

@Sharable
public class PauseController extends ChannelHandlerAdapter {

	private AtomicBoolean paused;
	
	public PauseController() {
		this.paused = new AtomicBoolean(false);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		// if paused
		if(this.paused.get()) {
			// release
			if(msg instanceof ByteBuf) {
				((ByteBuf) msg).discardReadBytes();
			}
			ReferenceCountUtil.release(msg);
			
			// force shut of channel
			ctx.disconnect().syncUninterruptibly();
			
			// do nothing else!
			return;
		}
		
		super.channelRead(ctx, msg);
	}

	/**
	 * Pauses (or unpauses) the controller
	 * 
	 * @param shouldPause true to pause, false to unpause
	 */
	public void pause(boolean shouldPause) {
		this.paused.lazySet(shouldPause);
	}
	
}
