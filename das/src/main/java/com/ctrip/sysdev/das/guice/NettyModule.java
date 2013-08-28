package com.ctrip.sysdev.das.guice;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.sysdev.das.commons.TcpServer;
import com.ctrip.sysdev.das.netty4.Netty4ChannelInitializer;
import com.ctrip.sysdev.das.netty4.Netty4Handler;
import com.ctrip.sysdev.das.netty4.Netty4ProtocolDecoder;
import com.ctrip.sysdev.das.netty4.Netty4ProtocolEncoder;
import com.ctrip.sysdev.das.netty4.Netty4Server;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class NettyModule extends AbstractModule {

	private static final Logger logger = LoggerFactory
			.getLogger(NettyModule.class);

	private final int CPUCNT = Runtime.getRuntime().availableProcessors();

	@Override
	protected void configure() {
		bind(TcpServer.class).to(Netty4Server.class).in(Scopes.SINGLETON);
		bind(ByteToMessageDecoder.class).to(Netty4ProtocolDecoder.class);
		bind(MessageToByteEncoder.class).to(Netty4ProtocolEncoder.class);
		bind(SimpleChannelInboundHandler.class).to(Netty4Handler.class);
		bind(ChannelInitializer.class).to(Netty4ChannelInitializer.class).in(
				Scopes.SINGLETON);
		logger.info("NettyModule loaded");
	}

	@Provides
	@Singleton
	@Named("bossGroup")
	NioEventLoopGroup provideBossGroup() {
		return new NioEventLoopGroup();
	}

	@Provides
	@Singleton
	@Named("ioGroup")
	NioEventLoopGroup provideIoGroup() {
		return new NioEventLoopGroup();
	}

	@Provides
	@Singleton
	@Named("businessGroup")
	EventExecutorGroup provideBusinessGroup() {
		return new DefaultEventExecutorGroup(CPUCNT * 5);
	}

	@Provides
	@Singleton
	@Named("ChannelGroup")
	ChannelGroup provideChannelGroup() {
		ChannelGroup channelGroup = new DefaultChannelGroup(
				GlobalEventExecutor.INSTANCE);
		return channelGroup;
	}

}
