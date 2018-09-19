package net.bpelunit.schemacoverage.messagesource;

import java.util.List;

import org.w3c.dom.Element;

public interface IMessageSource {

	List<Element> getInboundMessageInstances();
	List<Element> getOutboundMessageInstances();
	
}
