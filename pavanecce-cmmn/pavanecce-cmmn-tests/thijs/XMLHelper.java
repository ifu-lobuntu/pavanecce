package org.cafienne.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Helper class for various XML functions.
 * 
 */
public class XMLHelper
{
	public static String printXMLNode(Node node)
	{
		try
		{
			TransformerFactory tf = TransformerFactory.newInstance();
			tf.setAttribute("indent-number", 2);
			Transformer transformer = tf.newTransformer();
			String omitDeclaration = node instanceof Document || node==node.getOwnerDocument().getDocumentElement() ? "no" : "yes";
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitDeclaration);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(node), new StreamResult(writer));
			String output = writer.getBuffer().toString();
			output = removeEmptyLines(output); // .replaceAll("\n|\r", "");
			return output;
		}
		catch (TransformerException te)
		{
			throw new RuntimeException(te);
		}
	}

	public static void persist(Node node, File file) throws IOException, TransformerException
	{
		String xmlString = printXMLNode(node);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(xmlString.getBytes());
		fos.flush();
		fos.close();
	}

	private static String removeEmptyLines(String text)
	{
		StringBuffer buffer = new StringBuffer();
		String[] lines = text.split("\n");
		for (String string : lines)
		{
			if (string.trim().isEmpty())
			{
				continue;
			}
			buffer.append(string + "\n");
		}
		return buffer.toString();
	}

	/**
	 * Parses the file with the specified name into an XMLDocument
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static Document getXMLDocument(String fileName) throws IOException, ParserConfigurationException, SAXException
	{
		InputStream inputStream = XMLHelper.class.getClassLoader().getResourceAsStream(fileName);
		if (inputStream == null)
		{
			File file = new File(fileName);
			inputStream = new FileInputStream(file);
		}
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(inputStream);

		// optional, but recommended
		// read this -
		// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();

		return doc;
	}

	/**
	 * Parses a string into an XMLDocument
	 * 
	 * @param xmlString
	 * @return
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static Document loadXML(String xmlString) throws IOException, ParserConfigurationException, SAXException
	{
		InputStream inputStream = new ByteArrayInputStream(xmlString.getBytes());
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(inputStream);

		// optional, but recommended
		// read this -
		// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();

		return doc;
	}

	/**
	 * Returns all elements under the element with the specified tagname.
	 * 
	 * @param element
	 * @param localName
	 * @return
	 */
	public static Collection<Element> getElementsByTagName(Element element, String localName)
	{
		return getDirectChildren(element, localName);
	}

	/**
	 * Get a child element with the specified localName. Assumption is that it
	 * is in the same namespace as the specified element.
	 * 
	 * @param element
	 * @param localName
	 * @return
	 */
	public static Element getElement(Element element, String localName)
	{
		Collection<Element> elements = getElementsByTagName(element, localName);
		if (elements.isEmpty())
		{
			return null;
		}
		else
		{
			return elements.iterator().next();
		}
	}

	/**
	 * Searches for the element with the specified name in the whole subtree of
	 * element.
	 * 
	 * @param element
	 * @param localName
	 * @return
	 */
	public static Element findElement(Element element, String localName)
	{
		NodeList nList = element.getElementsByTagName(localName);
		if (nList.getLength() == 0)
		{
			return null;
		}
		return (Element) nList.item(0);
	}

	/**
	 * Simple logging
	 * 
	 * @param message
	 */
	static void debugLog(String message)
	{
		// System.out.println(message);
	}

	/**
	 * Returns the text content of the child element with the specified tag
	 * name, and the default value if no such element exists or the element does
	 * not have any text content.
	 * 
	 * @param element
	 * @param localName
	 * @return
	 */
	public static String getContent(Element element, String localName, String defaultValue)
	{
		Element child = getElement(element, localName);
		if (child != null)
		{
			String value = child.getTextContent();
			if (value != null)
			{
				return value;
			}
		}
		return defaultValue;
	}

	/**
	 * Checks whether the expected XML element is contained in the actual
	 * element. I.e., every attribute of the expected element must occur in the
	 * actual element, with the same value, and all child nodes in the expected
	 * element must occur in the actual element; if multiple child nodes with
	 * the same name are found in the expected element, the first found
	 * identifying attribute in the expected child element is used to identify
	 * the child in the actual element.
	 * 
	 * @param expected
	 * @param actual
	 * @return
	 */
	public static void assertContains(Element expected, Element actual, String messagePrefix, String... identifyingAttributes)
	{
		if (!expected.getTagName().equals(actual.getTagName()))
		{
			throw new AssertionError(messagePrefix + "\nExpected tagname differs from actual tagname (expected '" + printNodeOnly(expected) + "', found " + printNodeOnly(actual) + ")");
		}

		// Compare attributes
		NamedNodeMap expectedAttributes = expected.getAttributes();
		for (int i = expectedAttributes.getLength() - 1; i >= 0; i--)
		{
			String expectedAttributeName = expectedAttributes.item(i).getNodeName();
			String expectedAttributeValue = expectedAttributes.item(i).getNodeValue();

			String actualValue = actual.getAttribute(expectedAttributeName);
			if (actualValue == null || actualValue.isEmpty())
			{
				throw new AssertionError(messagePrefix + "\nThe attribute '" + expectedAttributeName + "' with value '" + expectedAttributeValue + "' was not found in the result " + printNodeOnly(actual));
			}
			if (!expectedAttributeValue.equals(actualValue))
			{
				throw new AssertionError(messagePrefix + "\nThe attribute '" + expectedAttributeName + "' has value '" + actualValue + "' instead of '" + expectedAttributeValue
						+ "' in the result " + printNodeOnly(actual));
			}
		}

		// Compare child elements
		Node child = expected.getFirstChild();
		while (child != null)
		{
			if (child instanceof Element)
			{
				assertContainsChildElement((Element) child, actual, messagePrefix, identifyingAttributes);
			}
			child = child.getNextSibling();
		}
	}

	private static List<Element> getDirectChildren(Element element, String tagName)
	{
		NodeList nList = element.getElementsByTagName(tagName);
		List<Element> list = new ArrayList<Element>();
		int listLength = nList.getLength();
		for (int i = 0; i < listLength; i++)
		{
			Element listElement = (Element) nList.item(i);
			if (listElement.getParentNode() == element)
			{
				list.add(listElement);
			}
		}
		return list;
	}
	
	private static String printNodeOnly(Node node)
	{
		return printXMLNode(node.cloneNode(false));
	}

	private static void assertContainsChildElement(Element expectedChild, Element actual, String messagePrefix, String... identifyingAttributes)
	{
		List<Element> childrenWithThatName = getDirectChildren(actual, expectedChild.getTagName());
		if (childrenWithThatName.isEmpty())
		{
			throw new AssertionError(messagePrefix + "\nThe child element " + printNodeOnly(expectedChild) + " was not found in the result " + printNodeOnly(actual));
		}
		if (childrenWithThatName.size() == 1)
		{
			assertContains(expectedChild, childrenWithThatName.get(0), messagePrefix, identifyingAttributes);
		}
		else
		{
			String[] identifyingValues = getAttributeValues(expectedChild, identifyingAttributes);
			int expectedLocationInParent = getLocationInParent(expectedChild, identifyingAttributes, identifyingValues);
			Element actualChild = getNthElement(childrenWithThatName, identifyingAttributes, identifyingValues, expectedLocationInParent);
			if (actualChild==null)
			{
				throw new AssertionError(messagePrefix + "\nThe child element " + printNodeOnly(expectedChild) + " was not found in the result " + printNodeOnly(actual));
			}
			else
			{
				assertContains(expectedChild, actualChild, messagePrefix, identifyingAttributes);
			}
		}
	}
	
	private static Element getNthElement(List<Element> elements, String[] identifyingAttributes, String[] identifyingValues, int expectedLocationInParent)
	{
		int count = -1;
		for (Element element : elements)
		{
			if (hasTheseAttributeValues(element, identifyingAttributes, identifyingValues))
			{
				count ++;
			}
			if (count == expectedLocationInParent)
			{
				return element;
			}
		}
		return null;
	}

	private static int getLocationInParent(Element expectedChild, String[] identifyingAttributes, String[] identifyingValues)
	{
		Element parent = (Element) expectedChild.getParentNode();
		List<Element> allSiblingsWithMyName = getDirectChildren(parent, expectedChild.getTagName());
		int location = -1;
		for (Element sibling : allSiblingsWithMyName)
		{
			if (hasTheseAttributeValues(sibling, identifyingAttributes, identifyingValues))
			{
				location ++;
			}
			if (sibling == expectedChild)
			{
				return location;
			}
		}
		return location;
	}
	
	/**
	 * Returns true if the element has all the specified attributes with all the specified values
	 * @param element
	 * @param identifyingAttributes
	 * @param identifyingValues
	 * @return
	 */
	private static boolean hasTheseAttributeValues(Element element, String[] identifyingAttributes, String[] identifyingValues)
	{
		for (int i=0; i<identifyingAttributes.length; i++)
		{
			if (!element.getAttribute(identifyingAttributes[i]).equals(identifyingValues[i]))
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns a set with all attribute values for the specified attributes within the element.
	 * @param expectedChild
	 * @param identifyingAttributes
	 * @return
	 */
	private static String[] getAttributeValues(Element element, String[] identifyingAttributes)
	{
		ArrayList<String> values = new ArrayList<String>();
		for (int i=0; i<identifyingAttributes.length; i++)
		{
			values.add(element.getAttribute(identifyingAttributes[i]));
		}
		return values.toArray(new String[values.size()]);
	}

}
