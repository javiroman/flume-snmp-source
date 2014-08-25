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
import java.util.Date;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class sendSNMPTrap 
{
  public static final String  community  = "public";

  //  Sending Trap for sysLocation of RFC1213
  public static final String  trapOid          = ".1.3.6.1.2.1.1.6";                         

  public static final String  ipAddress      = "127.0.0.1";
  
  //public static final int     port      = 163;
  public static final int     port      = 5140;
  
  public sendSNMPTrap()
  {
  }

  public static void main(String[] args)
  {
    sendSNMPTrap snmp4JTrap = new sendSNMPTrap();

    /* Sending V1 Trap */
    snmp4JTrap.sendSnmpV1Trap();

    /* Sending V2 Trap */
    snmp4JTrap.sendSnmpV2Trap();
  }

  /**
   * This methods sends the V1 trap to the Localhost in port 163
   */
  public void sendSnmpV1Trap()
  {
    try
    {
      //Create Transport Mapping
      TransportMapping transport = new DefaultUdpTransportMapping();
      transport.listen();

      //Create Target 
      CommunityTarget comtarget = new CommunityTarget();
      comtarget.setCommunity(new OctetString(community));
      comtarget.setVersion(SnmpConstants.version1);
      comtarget.setAddress(new UdpAddress(ipAddress + "/" + port));
      comtarget.setRetries(2);
      comtarget.setTimeout(5000);

      //Create PDU for V1
      PDUv1 pdu = new PDUv1();
      pdu.setType(PDU.V1TRAP);
      pdu.setEnterprise(new OID(trapOid));
      pdu.setGenericTrap(PDUv1.ENTERPRISE_SPECIFIC);
      pdu.setSpecificTrap(1);
      pdu.setAgentAddress(new IpAddress(ipAddress));

      //Send the PDU
      Snmp snmp = new Snmp(transport);
      System.out.println("Sending V1 Trap to " + ipAddress + " on Port " + port);
      snmp.send(pdu, comtarget);
      snmp.close();
    }
    catch (Exception e)
    {
      System.err.println("Error in Sending V1 Trap to " + ipAddress + " on Port " + port);
      System.err.println("Exception Message = " + e.getMessage());
    }
  }

  
  /**
   * This methods sends the V2 trap to the Localhost in port 163
   */
  public void sendSnmpV2Trap()
  {
    try
    {
      //Create Transport Mapping
      TransportMapping transport = new DefaultUdpTransportMapping();
      transport.listen();

      //Create Target 
      CommunityTarget comtarget = new CommunityTarget();
      comtarget.setCommunity(new OctetString(community));
      comtarget.setVersion(SnmpConstants.version2c);
      comtarget.setAddress(new UdpAddress(ipAddress + "/" + port));
      comtarget.setRetries(2);
      comtarget.setTimeout(5000);

      //Create PDU for V2
      PDU pdu = new PDU();
      
      // need to specify the system up time
      pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new OctetString(new Date().toString())));
      pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(trapOid)));
      pdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress, new IpAddress(ipAddress)));

      // variable binding for Enterprise Specific objects, Severity (should be defined in MIB file)
      pdu.add(new VariableBinding(new OID(trapOid), new OctetString("Major"))); 
      pdu.setType(PDU.NOTIFICATION);
      
      //Send the PDU
      Snmp snmp = new Snmp(transport);
      System.out.println("Sending V2 Trap to " + ipAddress + " on Port " + port);
      snmp.send(pdu, comtarget);
      snmp.close();
    }
    catch (Exception e)
    {
      System.err.println("Error in Sending V2 Trap to " + ipAddress + " on Port " + port);
      System.err.println("Exception Message = " + e.getMessage());
    }
  }
}

