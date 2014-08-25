flume-snmp-source
=================

Apache Flume Source plugin for SNMP Query and Trap consumption.

SNMP Trap Source usage example:

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


SNMP Query Source usage example:

... pending.
