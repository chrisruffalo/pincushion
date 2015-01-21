package com.github.chrisruffalo.pincushion.tunnel.impl.control;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

@Sharable
public class PauseController extends ChannelHandlerAdapter {

    private static final AtomicIntegerFieldUpdater<PauseController> PAUSE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(PauseController.class, "paused");
    
	private volatile int paused;
	
	public PauseController() {
		this.paused = 0;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		// if paused
		if(PAUSE_UPDATER.get(this) > 0) {
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
	    PAUSE_UPDATER.set(this, shouldPause ? 1 : 0);
	}

    public int getPaused() {
        return paused;
    }

    public void setPaused(int paused) {
        this.paused = paused;
    }	
}
