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

import org.apache.flume.source.snmp.SNMPTrap;

import java.io.IOException;

import org.apache.flume.ChannelException;
import org.apache.flume.Context;
import org.apache.flume.CounterGroup;
import org.apache.flume.Event;
import org.apache.flume.event.SimpleEvent;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.conf.Configurables;
import org.apache.flume.source.SyslogUtils;

import org.snmp4j.smi.UdpAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SNMPTrapSource extends AbstractSource
      implements EventDrivenSource, Configurable {

  private String bindAddress;
  private int bindPort;
  private static final int DEFAULT_PORT = 5140;
  private static final String DEFAULT_BIND = "127.0.0.1";

  private static final Logger logger = LoggerFactory
      .getLogger(SNMPTrapSource.class);

  private CounterGroup counterGroup = new CounterGroup();

  @Override
  public void start() {
    // setup snmp4j trap server
    SNMPTrap snmp4jTrapReceiver = new SNMPTrap();
    try {
        snmp4jTrapReceiver.listen(new UdpAddress(bindAddress + "/" + bindPort));
    }
    catch (IOException e) {
        logger.info("Error in Listening for Trap");
        logger.info("Exception Message = ", e.getMessage());
    }

    super.start();
  }

  @Override
  public void stop() {
    logger.info("SNMPTrap Source stopping...");
    logger.info("Metrics:{}", counterGroup);

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
