package com.github.chrisruffalo.pincushion.tunnel.impl.forward;

import com.github.chrisruffalo.pincushion.tunnel.impl.initializer.ResponseForwarderInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Recycler;

public class RequestForwarder extends ChannelForwarder {

	private final Bootstrap bootstrap;

	private Channel remote;

    public RequestForwarder(Bootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}
	
	@Override
	protected Channel target() {
		return this.remote;
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {

		final Channel target = this.target();
		if(target != null) {
			return;
		}

		// save origin channel
		final Channel origin = ctx.channel();

		// update bootstrap with pipeline
		this.bootstrap.handler(new ResponseForwarderInitializer(origin));

		// bootstrap connection
		final ChannelFuture future = this.bootstrap.connect();

        // if channel opens ok, start read
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()) {
                    // kick read because remote is open
                    origin.read();
                } else {
                    origin.close();
                }
            }
        });

		// save target
		this.remote = future.channel();

		// start read
		this.logger().trace("request: starting read");
		
		// forward event
		super.channelActive(ctx);
	}
}
