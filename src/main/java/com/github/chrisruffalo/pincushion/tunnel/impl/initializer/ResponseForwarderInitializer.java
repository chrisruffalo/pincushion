package com.github.chrisruffalo.pincushion.tunnel.impl.initializer;

import com.github.chrisruffalo.pincushion.tunnel.impl.forward.ResponseForwarder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;

/**
*/
public class ResponseForwarderInitializer extends ChannelInitializer<Channel> {

    private final Channel origin;

    public ResponseForwarderInitializer(Channel origin) {
        this.origin = origin;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        // log
        //ch.pipeline().addLast(new LoggingHandler("response-log", LogLevel.INFO));

        // add returner that will return values back
        // to origin
        ch.pipeline().addLast("response-forwarder", new ResponseForwarder(this.origin));
    }
}
