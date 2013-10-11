package com.ctrip.sysdev.das.netty4;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import com.ctrip.sysdev.das.domain.Request;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class Netty4ChannelInitializer extends ChannelInitializer<Channel> {
	@SuppressWarnings("rawtypes")
	@Inject
	private Provider<SimpleChannelInboundHandler> netty4HandlerProvider;
	@Inject
	@Named("businessGroup")
	private Provider<EventExecutorGroup> businessGroupProvider;

	@SuppressWarnings({ "unchecked" })
	@Override
	protected void initChannel(Channel ch) throws Exception {

		ChannelPipeline p = ch.pipeline();
		SimpleChannelInboundHandler<Request> netty4Handler = netty4HandlerProvider
				.get();

		p.addLast("logger", new DasLoggingHandler(LogLevel.DEBUG));
		p.addLast("decoder", new RequestDecoder());
		p.addLast(businessGroupProvider.get(), "handler", netty4Handler);

	}
	
	private static class DasLoggingHandler extends LoggingHandler {
		DasLoggingHandler(LogLevel level) {
			super(level);
		}
		
		@Override
		public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
			logger.debug("channelRegistered {}", ctx.channel());
//			try {
//				super.channelUnregistered(ctx);
//			} catch (Exception e) {
//				logger.debug(e.toString());
//			}
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) {
			logger.info("channelInactive {}", ctx.channel());
//			try {
//				super.channelInactive(ctx);
//			} catch (Exception e) {
//				logger.debug(e.toString());
//			}
		}
	}
}
