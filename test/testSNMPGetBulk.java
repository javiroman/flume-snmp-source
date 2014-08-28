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
 *
 * http://www.webnms.com/snmp/help/snmpapi/snmpv3/snmp_operations/snmp_getbulk.html
 *
 ****************************************************************/

import java.io.IOException;
import java.util.Vector;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;


public class testSNMPGetBulk
{
	public static void main(String[] args) throws IOException, InterruptedException {
	    Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
	    snmp.listen();

	    CommunityTarget target = new CommunityTarget();
	    target.setCommunity(new OctetString("public"));
	    target.setVersion(SnmpConstants.version2c);
	    target.setAddress(new UdpAddress("22.79.52.119/161"));
	    target.setTimeout(3000);    //3s
	    target.setRetries(1);

	    PDU pdu = new PDU();
	    pdu.setType(PDU.GETBULK);
	    pdu.setMaxRepetitions(1); 
	    pdu.setNonRepeaters(0);
	    VariableBinding[] array = {new VariableBinding(new OID("1.3.6.1.4.1.2000.1.2.5.1.3")),
	                               new VariableBinding(new OID("1.3.6.1.4.1.2000.1.3.1.1.7")),
	                               new VariableBinding(new OID("1.3.6.1.4.1.2000.1.3.1.1.10")),
	                               new VariableBinding(new OID("1.3.6.1.4.1.2000.1.2.5.1.19"))};
	    pdu.addAll(array);

	    //pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.2000.1.2.5.1.3"))); 

	    ResponseEvent responseEvent = snmp.send(pdu, target);
	    PDU response = responseEvent.getResponse();

	    if (response == null) {
		    System.out.println("TimeOut...");
	    } else {
		    if (response.getErrorStatus() == PDU.noError) {
                Vector<? extends VariableBinding> vbs = response.getVariableBindings();
                for (VariableBinding vb : vbs) {
                    System.out.println(vb.getVariable().toString());
		        }
		    } else {
		        System.out.println("Error:" + response.getErrorStatusText());
		    }
	    }
	}
}
