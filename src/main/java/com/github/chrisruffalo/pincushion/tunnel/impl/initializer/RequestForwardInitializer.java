package com.github.chrisruffalo.pincushion.tunnel.impl.initializer;

import com.github.chrisruffalo.pincushion.client.ClientBootstrapFactory;
import com.github.chrisruffalo.pincushion.tunnel.impl.control.PauseController;
import com.github.chrisruffalo.pincushion.tunnel.impl.control.StatisticsCollector;
import com.github.chrisruffalo.pincushion.tunnel.impl.forward.RequestForwarder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;

/**
 *
 */
public class RequestForwardInitializer extends ChannelInitializer<SocketChannel> {

    private final PauseController pauseController;

    private final StatisticsCollector statisticsCollector;

    private final EventLoopGroup group;

    private final String destinationAddress;

    private final int destinationPort;

    public RequestForwardInitializer(StatisticsCollector statisticsCollector, PauseController pauseController, EventLoopGroup group, String destinationAddress, int destinationPort) {
        // set collectors
        this.pauseController = pauseController;
        this.statisticsCollector = statisticsCollector;

        // group
        this.group = group;

        // destination
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // add stats collector to head of pipeline
        ch.pipeline().addFirst("stats", this.statisticsCollector);

        // add pause controller even before that...
        ch.pipeline().addFirst("pause", this.pauseController);

        // log
        //ch.pipeline().addLast(new LoggingHandler("forward-log", LogLevel.INFO));

        final Bootstrap bootstrap = ClientBootstrapFactory.INSTANCE.bootstrap(this.group, this.destinationAddress, this.destinationPort);

        // add a forwarder from this server connection to the client
        ch.pipeline().addLast("request-forwarder", new RequestForwarder(bootstrap));
    }

}
