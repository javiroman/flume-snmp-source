flume-snmp-source
=================

Apache Flume Source plugin for SNMP Query and Trap consumption.

SNMP Trap Source usage example - Listen for generic SNMP Trap:

	# Name the components on this agent
	agent.sources = source1
	agent.sinks = sink1
	agent.channels = channel1

	# The source
	agent.sources.source1.type = org.apache.flume.source.SNMPTrapSource
	agent.sources.source1.bind = 127.0.0.1
	agent.sources.source1.port = 4444

	# The channel
	agent.channels.channel1.type = memory

	# The sink
	agent.sinks.sink1.type = logger

	# Bind the source and sink to the channel
	agent.sources.source1.channels = channel1
	agent.sinks.sink1.channel = channel1


SNMP Query Source usage example - SNMP Query for PDU data, the plugin is using
SNMP GETBULK for performance.

    # Name the components on this agent
    agent.sources = source1 
    agent.sinks = sink1
    agent.channels = channel1

    # The source
    agent.sources.source1.type = org.apache.flume.source.SNMPQuerySource
    agent.sources.source1.host = 23.23.52.11
    agent.sources.source1.port = 161
    agent.sources.source1.delay = 30
    # Power Distribution Unit - PDU name
    agent.sources.source1.oid1 = 1.3.6.1.4.1.2000.1.2.5.1.3
    # Amperes
    agent.sources.source1.oid2 = 1.3.6.1.4.1.2000.1.2.5.1.7
    agent.sources.source1.oid3 = 1.3.6.1.4.1.2000.1.2.5.1.9
    agent.sources.source1.oid4 = 1.3.6.1.4.1.2000.1.2.5.1.10
    agent.sources.source1.oid5 = 1.3.6.1.4.1.2000.1.2.5.1.12
    agent.sources.source1.oid6 = 1.3.6.1.4.1.2000.1.2.5.1.13
    agent.sources.source1.oid7 = 1.3.6.1.4.1.2000.1.2.5.1.15
    # Volts
    agent.sources.source1.oid8 = 1.3.6.1.4.1.2000.1.2.5.1.16
    agent.sources.source1.oid9 = 1.3.6.1.4.1.2000.1.2.5.1.17
    agent.sources.source1.oid10 = 1.3.6.1.4.1.2000.1.2.5.1.18
    # Temp
    agent.sources.source1.oid11 = 1.3.6.1.4.1.2000.1.3.1.1.7
    agent.sources.source1.oid12 = 1.3.6.1.4.1.2000.1.3.1.1.9
    # Humidity
    agent.sources.source1.oid13 = 1.3.6.1.4.1.2000.1.3.1.1.10
    agent.sources.source1.oid14 = 1.3.6.1.4.1.2000.1.3.1.1.12
    # Energy
    agent.sources.source1.oid15 = 1.3.6.1.4.1.2000.1.2.5.1.19

    # The channel
    agent.channels.channel1.type = memory

    # The sink
    agent.sinks.sink1.type = logger

    # Bind the source and sink to the channel
    agent.sources.source1.channels = channel1
    agent.sinks.sink1.channel = channel1


The message passed to the Channel is build with the format:

    date,oid1,oid2,oid3, ....,oid15




