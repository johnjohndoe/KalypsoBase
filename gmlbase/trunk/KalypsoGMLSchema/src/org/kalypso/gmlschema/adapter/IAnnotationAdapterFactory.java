package org.kalypso.gmlschema.adapter;

import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.AnnotationDocument.Annotation;
import org.apache.xmlbeans.impl.xb.xsdschema.DocumentationDocument.Documentation;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.kalypso.gmlschema.basics.QualifiedElement;
import org.kalypso.gmlschema.property.IPropertyType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO: rename: this is not an interface
public class IAnnotationAdapterFactory implements IAdapterFactory
{
  public Object getAdapter( final Object adaptableObject, final Class adapterType )
  {
    final String platformLang = getPlatformLang();

    if( adapterType == IAnnotation.class && adaptableObject instanceof QualifiedElement )
    {
      final Element xsdElement = ((QualifiedElement) adaptableObject).getElement();
      return annotationForElement( platformLang, xsdElement.getAnnotation(), xsdElement.getName() );
    }

    if( adapterType == IAnnotation.class && adaptableObject instanceof IPropertyType )
    {
      IPropertyType pt = (IPropertyType) adaptableObject;
      return createDefaultAnnotation( platformLang, pt.getQName().getLocalPart() );
    }

    return null;
  }

  public static String getPlatformLang( )
  {
    final String nl = Platform.getNL();

    if( "de_de".equalsIgnoreCase( nl ) )
      return "de";

    if( "en_en".equalsIgnoreCase( nl ) )
      return "en";

    return nl;
  }

  public static IAnnotation annotationForElement( final String platformLang, final Annotation xsdAnnotation, final String defaultName )
  {
    if( xsdAnnotation == null )
      return createDefaultAnnotation( platformLang, defaultName );
    final Documentation[] documentationArray = xsdAnnotation.getDocumentationArray();
    if( documentationArray == null )
      return createDefaultAnnotation( platformLang, defaultName );

    for( int i = 0; i < documentationArray.length; i++ )
    {
      final Documentation documentation = documentationArray[i];
      final String lang = documentation.getLang();
      if( platformLang.equals( lang ) )
      {
        final Node domNode = documentation.getDomNode();
        final org.w3c.dom.Element ele = (org.w3c.dom.Element) domNode;
        // TODO: get namespace constants from somewhere else!
        final String xmlSchemaNamespace = "http://www.w3.org/2001/XMLSchema";
        final String tooltip = getStringFromChildElement( ele, xmlSchemaNamespace, "tooltip" );
        final String label = getStringFromChildElement( ele, xmlSchemaNamespace, "label" );
        final String description = getStringFromChildElement( ele, xmlSchemaNamespace, "description" );
        final String defaultValue = defaultName;
        return new IAnnotation()
        {
          public String getLang( )
          {
            return lang;
          }

          public String getTooltip( )
          {
            return tooltip == null ? defaultValue : tooltip;
          }

          public String getLabel( )
          {
            return label == null ? defaultValue : label;
          }

          public String getDescription( )
          {
            return description == null ? defaultValue : description;
          }
        };
      }
    }
    String elementName = defaultName;
    if( elementName != null )
      return createDefaultAnnotation( platformLang, elementName );
    else
      return createDefaultAnnotation( platformLang, "?" );
  }

  private static IAnnotation createDefaultAnnotation( final String lang, final String defaultName )
  {
    return new IAnnotation()
    {

      public String getLang( )
      {
        return lang;
      }

      public String getTooltip( )
      {
        return defaultName;
      }

      public String getLabel( )
      {
        return defaultName;
      }

      public String getDescription( )
      {
        return defaultName;
      }

    };

  }

  public Class[] getAdapterList( )
  {
    return new Class[] { IAnnotation.class };
  }

  private static String getStringFromChildElement( final org.w3c.dom.Element elt, final String namespace, final String eltName )
  {
    final NodeList nlL = elt.getElementsByTagNameNS( namespace, eltName );
    if( nlL.getLength() > 0 )
    {
      final org.w3c.dom.Element innerElt = (org.w3c.dom.Element) nlL.item( 0 );
      return getStringValue( innerElt );
    }

    return null;
  }

  /**
   * Returns the text contained in the specified element. The returned value is trimmed by calling the trim() method of
   * java.lang.String
   * <p>
   * 
   * @param node
   *          current element
   * @return the textual contents of the element or null, if it is missing
   */
  private static String getStringValue( Node node )
  {
    NodeList children = node.getChildNodes();
    StringBuffer sb = new StringBuffer( children.getLength() * 500 );

    for( int i = 0; i < children.getLength(); i++ )
    {
      if( children.item( i ).getNodeType() == Node.TEXT_NODE || children.item( i ).getNodeType() == Node.CDATA_SECTION_NODE )
      {
        sb.append( children.item( i ).getNodeValue() );
      }
    }

    return sb.toString().trim();
  }

}
