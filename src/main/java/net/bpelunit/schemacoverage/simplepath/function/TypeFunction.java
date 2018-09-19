package net.bpelunit.schemacoverage.simplepath.function;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.bpelunit.schemacoverage.simplepath.selector.INodeSelector;
import net.bpelunit.schemacoverage.util.StringUtil;
import net.bpelunit.schemacoverage.xml.QNameUtil;
import net.bpelunit.schemacoverage.xml.XMLUtil;

public class TypeFunction extends AbstractNodeFunction {

	private String defaultType;

	public TypeFunction(INodeSelector selector, String defaultType) {
		super(selector);
		this.defaultType = defaultType;
	}

	@Override
	protected List<String> evaluateInternal(List<Node> nodes) {
		List<String> result = new ArrayList<>();
		for(Node n : nodes) {
			if(n.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element)n;
				String typeAttributeValue = e.getAttributeNS(XMLUtil.NAMESPACE_SCHEMAINSTANCE, "type");
				if(!StringUtil.isEmpty(typeAttributeValue)) {
					QName qName = QNameUtil.resolveQNameFromCName(e, typeAttributeValue);
					result.add(QNameUtil.format(qName));
				} else {
					result.add(defaultType);
				}
			}
		}
		return result;
	}

	@Override
	protected String getFunctionName() {
		if(defaultType != null) {
			return "xsi:type[default:" + defaultType + "]";
		} else {
			return "xsi:type";
		}
	}

}
