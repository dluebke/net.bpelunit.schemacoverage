package net.bpelunit.schemacoverage.model.measurement;

import static net.bpelunit.schemacoverage.model.xsd.XMLSchemaHelper.collectAttributes;
import static net.bpelunit.schemacoverage.model.xsd.XMLSchemaHelper.collectElements;
import static net.bpelunit.schemacoverage.model.xsd.XMLSchemaHelper.getAnonymousType;
import static net.bpelunit.schemacoverage.model.xsd.XMLSchemaHelper.getMaxOccurs;
import static net.bpelunit.schemacoverage.model.xsd.XMLSchemaHelper.getMinOccurs;
import static net.bpelunit.schemacoverage.model.xsd.XMLSchemaHelper.isAbstract;
import static net.bpelunit.schemacoverage.model.xsd.XMLSchemaHelper.resolveElementRefIfNecessary;
import static net.bpelunit.schemacoverage.model.xsd.XMLSchemaHelper.resolveQNameForAttribute;
import static net.bpelunit.schemacoverage.model.xsd.XMLSchemaHelper.resolveQNameForElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import net.bpelunit.schemacoverage.model.project.IProject;
import net.bpelunit.schemacoverage.simplepath.filter.XSITypeFilter;
import net.bpelunit.schemacoverage.simplepath.function.CountFunction;
import net.bpelunit.schemacoverage.simplepath.function.INodeFunction;
import net.bpelunit.schemacoverage.simplepath.function.TextValueFunction;
import net.bpelunit.schemacoverage.simplepath.function.TypeFunction;
import net.bpelunit.schemacoverage.simplepath.selector.AttributeSelector;
import net.bpelunit.schemacoverage.simplepath.selector.ElementSelector;
import net.bpelunit.schemacoverage.simplepath.selector.PathTooLongException;
import net.bpelunit.schemacoverage.simplepath.selector.RootSelector;
import net.bpelunit.schemacoverage.util.StringUtil;
import net.bpelunit.schemacoverage.xml.QNameUtil;
import net.bpelunit.schemacoverage.xml.XMLUtil;

public class MeasurementBuilder {

	private static final int MAX_DEPTH = 7;
	private static final boolean USE_BASE_ELEMENT_IN_SUBSTITUTION_HIERARCHY = true;
	private boolean resolveTypeHierarchyForSubstitutionGroups = true;

	private Set<String> rulesTruncatedAt = new HashSet<>();
	
	public MeasurementBuilder() {
		this(true);
	}
	
	public Set<String> getRulesTruncatedAt() {
		return rulesTruncatedAt;
	}
	
	public MeasurementBuilder(boolean resolveTypeHierarchyForSubstitutionGroups ) {
		this.resolveTypeHierarchyForSubstitutionGroups = resolveTypeHierarchyForSubstitutionGroups; 
	}
	
	public void buildMeasurements(Context<Element> ctx, IProject project) {
		Element elementForSchemaElement = ctx.getSchemaReference();
		buildMeasurementPointsForElement(
			ctx, 
			elementForSchemaElement, 
			project, 
			new RootSelector()
		);
	}
		
	private void buildMeasurementPointsForElement(Context<Element> context, Element element, IProject project,
			RootSelector parentElementPath) {
		
		if("element".equals(element.getLocalName())) {
			element = resolveElementRefIfNecessary(element, project);
			int minOccurs = getMinOccurs(element);
			int maxOccurs = getMaxOccurs(element);
			
			QName elementQName = resolveQNameForElement(element);
			
			if(parentElementPath.length() <= MAX_DEPTH) {
				Set<String> substitutions = project.getAllSubstitutionsForElement(QNameUtil.format(elementQName));
				boolean hasSubstitutions = substitutions.size() > 0;

				List<QName> elementsToProcess = new ArrayList<>();
				if(USE_BASE_ELEMENT_IN_SUBSTITUTION_HIERARCHY) {
					elementsToProcess.add(elementQName);
				}
				for(String formatedQName : substitutions) {
					elementsToProcess.add(QNameUtil.resolveQName(formatedQName));
				}
				
				for(QName elementToProcess : elementsToProcess) {
					try {
						RootSelector thisElementPath = parentElementPath.clone();
						thisElementPath.appendSelector(new ElementSelector(elementToProcess), MAX_DEPTH);
						INodeFunction countFunctionElementPath = new CountFunction(thisElementPath.clone());
						if(minOccurs == 0 && maxOccurs == 1) {
							context.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.OptionalNodeNotSet, countFunctionElementPath, "0"));
							context.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.OptionalNodeSet, countFunctionElementPath, "1"));
						} else if(minOccurs == maxOccurs) {
							context.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.MandatoryNodeUsed, countFunctionElementPath, Integer.toString(minOccurs)));
						} else {
							context.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.ListLowerBound, countFunctionElementPath, Integer.toString(minOccurs)));
							if(maxOccurs > 0) {
								context.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.ListUpperBound, countFunctionElementPath, Integer.toString(maxOccurs)));
							} else {
								context.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.DifferentlySizedLists, countFunctionElementPath, null));
							}
						}
						Element currentElement = project.getSchemaElementByQName(QNameUtil.format(elementToProcess));
						if(currentElement == null) {
							currentElement = element;
						}
						String typeAsCName = currentElement.getAttribute("type");
						buildCoveragePointsForType(context, thisElementPath, typeAsCName, currentElement, project, resolveTypeHierarchyForSubstitutionGroups  || !hasSubstitutions);
					} catch(PathTooLongException e) {
						rulesTruncatedAt.add(e.getParentSelector().toString());
						// ignore and proceed. Rules will be truncated here, which is what we want
					}
				}
			}
		}
	}

	private void buildCoveragePointsForAttribute(Context<Element> context, Element attributeElement, IProject project, RootSelector parentElementPath) {
		try {
			if("attribute".equals(attributeElement.getLocalName())) {
				RootSelector thisElementPath = parentElementPath.clone();
				boolean required = "required".equals(attributeElement.getAttribute("use"));
				
				QName elementQName = resolveQNameForAttribute(attributeElement);
				thisElementPath.appendSelector(new AttributeSelector(elementQName), MAX_DEPTH);
				INodeFunction countFunctionElementPath = new CountFunction(thisElementPath);
				if(required) {
					context.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.MandatoryNodeUsed, countFunctionElementPath, "1"));
				} else {
					context.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.OptionalNodeNotSet, countFunctionElementPath, "0"));
					context.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.OptionalNodeSet, countFunctionElementPath, "1"));
				}
				
				String typeAsCName = attributeElement.getAttribute("type");
				buildCoveragePointsForType(context, thisElementPath, typeAsCName, attributeElement, project, false);
			}
		} catch(PathTooLongException e) {
			rulesTruncatedAt.add(e.getParentSelector().toString());
			// ignore and proceed. Rules will be truncated here, which is what we want
		}
	}
	
	private void buildCoveragePointsForType(Context<Element> context, RootSelector thisElementPath, String typeAsCName, Element element, IProject project, boolean followTypeHierarchy) {
		QName typeQName = QNameUtil.resolveQNameFromCName(element, typeAsCName);
		String formattedQName = QNameUtil.format(typeQName);
		Element type = StringUtil.isEmpty(typeAsCName) ? getAnonymousType(element) : project.getSchemaTypeByQName(formattedQName);
		
		INodeFunction textFunctionElementPath = new TextValueFunction(thisElementPath.clone());
		if(type == null) {
			// type == null iff XSD internal type
			if(XMLUtil.NAMESPACE_XMLSCHEMA.equals(typeQName.getNamespaceURI())) {
				if("boolean".equals(typeQName.getLocalPart())) {
					context.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.BooleanTrue, textFunctionElementPath, null));
					context.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.BooleanFalse, textFunctionElementPath, null));
				} else {
					context.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.MultipleValues, textFunctionElementPath, null));
				}
			} else {
				throw new RuntimeException("Cannot resolve type: " + typeAsCName);
			}
		} else if("simpleType".equals(type.getLocalName())) {
			NodeList enumerationElements = type.getElementsByTagNameNS(XMLUtil.NAMESPACE_XMLSCHEMA, "enumeration");
			if(enumerationElements.getLength() > 0) {
				for(int i = 0; i < enumerationElements.getLength(); i++) {
					context.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.EnumLiteralUsed, textFunctionElementPath, ((Element)enumerationElements.item(i)).getAttribute("value")));
				}
			} else {
				context.addMeasurementPoint(new MeasurementPoint(textFunctionElementPath));
			}
		} else {
			// only follow complex types
			if(followTypeHierarchy) {
				INodeFunction typeFunctionElementPath = new TypeFunction(thisElementPath.clone(), formattedQName);
				Set<String> allSubTypes = project.getAllSubtypesForType(formattedQName);
				if(allSubTypes != null && allSubTypes.size() > 0) {
					thisElementPath = thisElementPath.clone();
					thisElementPath.getLastSelector().setFilter(new XSITypeFilter(formattedQName, true));
					if(!isAbstract(formattedQName, project)) {
						context.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.DeclaredTypeInHierarchy, typeFunctionElementPath, formattedQName));
					}
					for(String subType : allSubTypes) {
						context.addMeasurementPoint(new MeasurementPoint(MeasurementPointType.TypeInHierarchy, typeFunctionElementPath, subType));
						
						Element subTypeElement = project.getSchemaTypeByQName(subType);
						
						RootSelector subTypeElementPath = thisElementPath.clone();
						subTypeElementPath.getLastSelector().setFilter(new XSITypeFilter(subType, false));
						List<Element> elements = collectElements(subTypeElement, project.getXMLSchemaContents());
						for(Element e : elements) {
							buildMeasurementPointsForElement(context, e, project, subTypeElementPath);
						}
						
						List<Element> attributes = collectAttributes(subTypeElement);
						for(Element a : attributes) {
							buildCoveragePointsForAttribute(context, a, project, subTypeElementPath);
						}
					}
				}
			}
		}
		
		List<Element> elements = collectElements(type, project.getXMLSchemaContents());
		for(Element e : elements) {
			buildMeasurementPointsForElement(context, e, project, thisElementPath);
		}
		
		List<Element> attributes = collectAttributes(type);
		for(Element a : attributes) {
			buildCoveragePointsForAttribute(context, a, project, thisElementPath);
		}
	}
}

