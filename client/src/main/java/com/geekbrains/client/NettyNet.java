package com.geekbrains.client;

import com.geekbrains.common.AbstractMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyNet {

    private SocketChannel channel;
    private OnMessageReceived callback;

    public NettyNet(OnMessageReceived callback){
        this.callback = callback;
        new Thread(()-> {
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                ChannelFuture future = bootstrap.channel(NioSocketChannel.class)
                        .group(group)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                channel = ch;
                                ch.pipeline().addLast(
                                        new ObjectEncoder(),
                                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                        new UploadFileHandler(callback)
                                );
                            }
                        }).connect("localhost", 8189).sync();
                future.channel().closeFuture().sync();
            } catch (Exception e){
                log.error("e=", e);
            } finally {
                group.shutdownGracefully();
            }

        }).start();
    }

    public void sendMessage(AbstractMessage message){

        log.debug(message.toString());
        channel.writeAndFlush(message);

    }

}
