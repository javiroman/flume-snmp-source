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
 ***************************************************************/
import java.io.IOException;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.MessageException;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.StateReference;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.TransportIpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.tools.console.SnmpRequest;
import org.snmp4j.transport.AbstractTransportMapping;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

public class testSNMPTrap implements CommandResponder
{
  public testSNMPTrap()
  {
  }

  public static void main(String[] args)
  {
    testSNMPTrap snmp4jTrapReceiver = new testSNMPTrap();
    try
    {
      snmp4jTrapReceiver.listen(new UdpAddress("localhost/163"));
    }
    catch (IOException e)
    {
      System.err.println("Error in Listening for Trap");
      System.err.println("Exception Message = " + e.getMessage());
    }
  }

  /**
   * This method will listen for traps and response pdu's from SNMP agent.
   */
  public synchronized void listen(TransportIpAddress address) throws IOException
  {
    AbstractTransportMapping transport;
    if (address instanceof TcpAddress)
    {
      transport = new DefaultTcpTransportMapping((TcpAddress) address);
    }
    else
    {
      transport = new DefaultUdpTransportMapping((UdpAddress) address);
    }

    ThreadPool threadPool = ThreadPool.create("DispatcherPool", 10);
    MessageDispatcher mtDispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());

    // add message processing models
    mtDispatcher.addMessageProcessingModel(new MPv1());
    mtDispatcher.addMessageProcessingModel(new MPv2c());

    // add all security protocols
    SecurityProtocols.getInstance().addDefaultProtocols();
    SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());

    //Create Target
    CommunityTarget target = new CommunityTarget();
    target.setCommunity( new OctetString("public"));
    
    Snmp snmp = new Snmp(mtDispatcher, transport);
    snmp.addCommandResponder(this);
    
    transport.listen();
    System.out.println("Listening on " + address);

    try
    {
      this.wait();
    }
    catch (InterruptedException ex)
    {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * This method will be called whenever a pdu is received on the given port specified in the listen() method
   */
  public synchronized void processPdu(CommandResponderEvent cmdRespEvent)
  {
    System.out.println("Received PDU...");
    PDU pdu = cmdRespEvent.getPDU();
    if (pdu != null)
    {

      System.out.println("Trap Type = " + pdu.getType());
      System.out.println("Variable Bindings = " + pdu.getVariableBindings());
      int pduType = pdu.getType();
      if ((pduType != PDU.TRAP) && (pduType != PDU.V1TRAP) && (pduType != PDU.REPORT)
      && (pduType != PDU.RESPONSE))
      {
        pdu.setErrorIndex(0);
        pdu.setErrorStatus(0);
        pdu.setType(PDU.RESPONSE);
        StatusInformation statusInformation = new StatusInformation();
        StateReference ref = cmdRespEvent.getStateReference();
        try
        {
          System.out.println(cmdRespEvent.getPDU());
          cmdRespEvent.getMessageDispatcher().returnResponsePdu(cmdRespEvent.getMessageProcessingModel(),
          cmdRespEvent.getSecurityModel(), cmdRespEvent.getSecurityName(), cmdRespEvent.getSecurityLevel(),
          pdu, cmdRespEvent.getMaxSizeResponsePDU(), ref, statusInformation);
        }
        catch (MessageException ex)
        {
          System.err.println("Error while sending response: " + ex.getMessage());
          LogFactory.getLogger(SnmpRequest.class).error(ex);
        }
      }
    }
  }
}
