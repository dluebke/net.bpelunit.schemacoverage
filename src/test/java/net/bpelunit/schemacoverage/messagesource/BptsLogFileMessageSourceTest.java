package net.bpelunit.schemacoverage.messagesource;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Element;

public class BptsLogFileMessageSourceTest {

	@Test
	public void testReadBptsLog() throws Exception {
		BptsLogFileMessageSource bptsLogMessageSource = BptsLogFileMessageSource.readBptsLogFiles("src/test/resources/net/bpelunit/schemacoverage/messagesource/bpts.1.log.xml");
		
		List<Element> inboundMessageInstances = bptsLogMessageSource.getInboundMessageInstances();
		List<Element> outboundMessageInstances = bptsLogMessageSource.getOutboundMessageInstances();
		assertEquals(2, inboundMessageInstances.size());
		assertEquals(2, outboundMessageInstances.size());
		
		assertEquals("a", outboundMessageInstances.get(0).getLocalName());
		assertEquals("d", outboundMessageInstances.get(1).getLocalName());
		
		assertEquals("b", inboundMessageInstances.get(0).getLocalName());
		assertEquals("c", inboundMessageInstances.get(1).getLocalName());
	}
}
