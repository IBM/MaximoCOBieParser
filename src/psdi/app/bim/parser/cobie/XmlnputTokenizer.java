/**
* Copyright IBM Corporation 2009-2017
*
* Licensed under the Eclipse Public License - v 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.eclipse.org/legal/epl-v10.html
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* 
* @Author Doug Wood
**/
package psdi.app.bim.parser.cobie;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import javax.xml.xpath.*;


/**
 * A class that contains a set of XML-handling utility methods
 * All methods of this class are static
 */
public class XmlnputTokenizer {

	/**
	 * Parses the XML file contained in the given input stream and reads
	 * the main Document node which is returned from this method
	 * 
	 * @param in the reader to read the XML data from
	 * @return the Document object which represents the root Node of the XML
	 * @throws Exception if an error occurs during parsing of the xml
	 */
	public static XmlnputTokenizer parse(Reader in) throws Exception {
		return parse(new InputSource(in));
	}
	
	/**
	 * Parses the XML file contained in the given input stream and reads
	 * the main Document node which is returned from this method
	 * 
	 * @param in the input stream to read the XML data from
	 * @return the Document object which represents the root Node of the XML
	 * @throws Exception if an error occurs during parsing of the xml
	 */
	public static XmlnputTokenizer parse(InputStream in) throws Exception {
		return parse(new InputSource(in));
	}
	
	/**
	 * Parses the XML file contained in the given input source and reads
	 * the main Document node which is returned from this method
	 * 
	 * @param in the input source to read the XML data from
	 * @return the Document object which represents the root Node of the XML
	 * @throws Exception if an error occurs during parsing of the xml
	 */
	public static XmlnputTokenizer parse(InputSource in) throws Exception {
		
		// Get DOM Parser Factory
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder builder =  builderFactory.newDocumentBuilder();
		Document xmlDocument = builder.parse(in);	
		
		return new XmlnputTokenizer(xmlDocument.getDocumentElement());
	}
	
	private Element fElement;
	
	private XmlnputTokenizer(Element element) {
		fElement = element;
	}
	
    /**
     * Reads the child node which has the given name from the given parent node
     * This method assumes that there is a single child node with
     * the given name under the given parent node, otherwise the first child
     * found is returned
     * 
     * @param node the parent node to read the child from
     * @param name the name of the child node to read
     * @return the child node
     */
    public XmlnputTokenizer getChild(String name) {
    	if (name == null) return null;
    	NodeList nodeList = fElement.getElementsByTagName(name);
    	return (nodeList.getLength() > 0)? new XmlnputTokenizer((Element) nodeList.item(0)) : null;       
    }
   
    
    /**
     * Returns the list of child nodes that have the given name from
     * the given parent node
     * 
     * @param node the parent node to read the child nodes from
     * @param name the name of the child nodes to read
     * @return the list of child nodes
     */
    public List<XmlnputTokenizer> getChildren(String name) {
        List<XmlnputTokenizer> childNodes = new ArrayList<XmlnputTokenizer>(); 
        if (name == null) return childNodes;
        
        NodeList nodeList = fElement.getElementsByTagName(name);
        int count = nodeList.getLength();
        for (int i = 0; i < count; i++) {
            Element e = (Element) nodeList.item(i);
            if (e.getParentNode() == fElement) {
                childNodes.add(new XmlnputTokenizer(e));
            }
        }
        return childNodes;
    }

    public List<XmlnputTokenizer> find(String query) throws XPathException {
    	List<XmlnputTokenizer> childNodes = new ArrayList<XmlnputTokenizer>();
    	
    	XPath xPath = XPathFactory.newInstance().newXPath();
    	
    	NodeList nodeList = (NodeList) xPath.evaluate(query, fElement, XPathConstants.NODESET);
    	int count = nodeList.getLength();
    	for (int i = 0; i < count; i++) {
    		childNodes.add(new XmlnputTokenizer((Element) nodeList.item(i)));
    	}
    	return childNodes;
    }

    public XmlnputTokenizer findElement(String query) throws XPathException {
    	XPath xPath = XPathFactory.newInstance().newXPath();
    	
    	Node result = (Node) xPath.evaluate(query, fElement, XPathConstants.NODE);
    	return (result != null)? new XmlnputTokenizer((Element) result) : null;
    }

    /**
     * Returns the value of the xml attribute with the given name that
     * is contained in the given node
     * 
     * @param node the node to read the attribute from
     * @param attrName the name of the attribute whose value is to be retrieved
     * @return the value corresponding to the given attribute
     */
    public String getAttribute(String attrName) {
    	String value = fElement.getAttribute(attrName);
    	return (value != null && value.length() == 0) ? null : value;
    }
    
    /**
     * Reads the text value contained in the given child node
     * 
     * @param node the parent node to read the child node from
     * @param childName the name of the child node whose text value
     * is to be retrieved
     * @return the text value of the child node
     */
    public String getChildValue(String childName) {
    	XmlnputTokenizer childNode = getChild(childName);
    	return (childNode != null)? childNode.fElement.getTextContent() : null;
    }
    
    public String getName() {
        return fElement.getTagName();
    }
    
    public String getXML() throws TransformerException, TransformerConfigurationException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		
		//initialize StreamResult with File object to save to file
		StringWriter result = new StringWriter();
		transformer.transform(new DOMSource(fElement), new StreamResult(result));
    	return result.toString();
    }
}


