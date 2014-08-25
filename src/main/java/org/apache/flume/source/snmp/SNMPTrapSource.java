/***************************************************************
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
 ****************************************************************/
package org.apache.flume.source.snmp;

import java.io.IOException;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.SimpleEvent;
import org.apache.flume.source.AbstractSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.snmp4j.smi.UdpAddress;

public class SNMPTrapSource extends AbstractSource implements 
        Configurable, PollableSource {

    private static final Logger LOG = LoggerFactory.getLogger(SNMPTrapSource.class);

    private String bindAddress;
    private String bindPort;

    private static final String DEFAULT_PORT = "162";
    private static final String DEFAULT_BIND = "127.0.0.1";

    @Override
	public void configure(Context context) {
        /*
         * Default is to listen on UDP port 162 on all IPv4 interfaces. 
         * Since 162 is a privileged port, snmptrapd must typically be run as root. 
         * Or change to non-privileged port > 1024.
         */
        bindAddress = context.getString("bind", DEFAULT_BIND);
        bindPort = context.getString("port", DEFAULT_PORT);
	}

    private class Startup extends Thread {
        public void run() {
              try {
                  SNMPTrap snmp4jTrapReceiver = new SNMPTrap();
                    try {
                        snmp4jTrapReceiver.listen(new UdpAddress(bindAddress + "/" + bindPort));
                    }
                    catch (IOException e) {
                        LOG.warn("Error in Listening for Trap");
                        LOG.warn("Exception Message = ", e.getMessage());
                    }
                } catch (Exception e) {
                        LOG.warn("Scribe failed", e);
                }
        }
    }

    @Override
    public void start() {
        // Initialize the connection to the external client
         Startup startupThread = new Startup();
         startupThread.start();

        super.start();
    }

    @Override
    public void stop() {
        // Disconnect from external client and do any additional cleanup
        super.stop();
    }

    /*
     * The runner of a PollableSource invokes the Source‘s process() method. 
     * The process() method should check for new data and store it into the 
     * Channel as Flume Events.
     */
    @Override
	public Status process() throws EventDeliveryException {

		return Status.READY;
	}
}

