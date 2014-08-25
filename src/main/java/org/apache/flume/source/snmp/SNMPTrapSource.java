/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 *  The pollable source will get regularly polled by the source runner asking it 
 *  to generate events. Nevertheless this is a event driven source which is 
 *  responsible for generating the events itself and normally does this 
 *  in response to some event happening, in this case the SNMP Trap.
 */
package org.apache.flume.source;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.flume.ChannelException;
import org.apache.flume.Context;
import org.apache.flume.CounterGroup;
import org.apache.flume.Event;
import org.apache.flume.event.SimpleEvent;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.conf.Configurables;
import org.apache.flume.source.SyslogUtils;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.oio.OioDatagramChannelFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SNMPTrapSource extends AbstractSource
      implements EventDrivenSource, Configurable {

  private String bindAddress;
  private int bindPort;
  private static final int DEFAULT_PORT = 5140;
  private static final String DEFAULT_BIND = "127.0.0.1";

  private int maxsize = 1 << 16; // 64k is max allowable in RFC 5426
  private Channel nettyChannel;
  private Map<String, String> formaterProp;

  private static final Logger logger = LoggerFactory
      .getLogger(SNMPTrapSource.class);

  private CounterGroup counterGroup = new CounterGroup();

  public class snmptrapHandler extends SimpleChannelHandler {
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent mEvent) {
      try {
        byte[] message;
        Map <String, String> headers;
        Event event;

        ChannelBuffer in = (ChannelBuffer) mEvent.getMessage();

        message = new byte[1];

        while (in.readable()) {
            message[0] = in.readByte();
        }

        event = new SimpleEvent();
        headers = new HashMap<String, String>();
        headers.put("timestamp", String.valueOf(System.currentTimeMillis()));
        logger.info("Message: {}", message);
        event.setBody(message);
        event.setHeaders(headers);

        if (event == null) {
          return;
        }
        getChannelProcessor().processEvent(event);
        counterGroup.incrementAndGet("events.success");
      } catch (ChannelException ex) {
        counterGroup.incrementAndGet("events.dropped");
        logger.error("Error writting to channel", ex);
        return;
      }
    }
  }

  @Override
  public void start() {
    // setup Netty server
    ConnectionlessBootstrap serverBootstrap = new ConnectionlessBootstrap
        (new OioDatagramChannelFactory(Executors.newCachedThreadPool()));

    final snmptrapHandler handler = new snmptrapHandler();
    
    serverBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() {
       return Channels.pipeline(handler);
      }
     });

    nettyChannel = serverBootstrap.bind(new InetSocketAddress(bindAddress, bindPort));

    super.start();
  }

  @Override
  public void stop() {
    logger.info("SNMPTrap Source stopping...");
    logger.info("Metrics:{}", counterGroup);
    if (nettyChannel != null) {
      nettyChannel.close();
      try {
        nettyChannel.getCloseFuture().await(60, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        logger.warn("netty server stop interrupted", e);
      } finally {
        nettyChannel = null;
      }
    }

    super.stop();
  }

  @Override
  public void configure(Context context) {
        /*
         * Default is to listen on UDP port 162 on all IPv4 interfaces. 
         * Since 162 is a privileged port, snmptrapd must typically be run as root. 
         * Or change to non-privileged port > 1024.
         */
        bindAddress = context.getString("bind", DEFAULT_BIND);
        bindPort = context.getInteger("port", DEFAULT_PORT);
  }

}
