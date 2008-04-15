package org.kalypso.gmlschema.annotation;

import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.AnnotationDocument.Annotation;
import org.apache.xmlbeans.impl.xb.xsdschema.DocumentationDocument.Documentation;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.KalypsoGmlSchemaExtensions;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.ReferencedRelationType;
import org.kalypso.gmlschema.xml.QualifiedElement;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This adapter factory creates {@link org.kalypso.gmlschema.adapter.IAnnotation}s for adaptable objects.
 * 
 * @author vdoemming
 */
public class AnnotationAdapterFactory implements IAdapterFactory
{
  @SuppressWarnings("unchecked")
  public Object getAdapter( final Object adaptableObject, final Class adapterType )
  {
    final String platformLang = getPlatformLang();
    
    if( adapterType == IAnnotation.class )
    {
      /* First, we try to get an annotation from an external annotation provider */
      final IAnnotationProvider[] annotationProviders = KalypsoGmlSchemaExtensions.getAnnotationProviders();
      for( final IAnnotationProvider provider : annotationProviders )
      {
        final IAnnotation annotation = provider.getAnnotation( platformLang, adaptableObject );
        if( annotation != null )
          return annotation;
      }

      // BUGFIX: if it is a referenced element, first try to get the annotation from the
      // referencing element. Only if we get nothing there, return the annotation of the referenced element.
      if( adaptableObject instanceof ReferencedRelationType )
      {
        final ReferencedRelationType relType = (ReferencedRelationType) adaptableObject;
        final Element xsdElement = relType.getReferencingElement();
        final IAnnotation annotation = annotationForElement( platformLang, xsdElement.getAnnotation(), xsdElement.getName(), false );
        if( annotation != null )
          return annotation;

        // else fall through, try to get the annotation from the referenced element
      }

      if( adaptableObject instanceof QualifiedElement )
      {
        final Element xsdElement = ((QualifiedElement) adaptableObject).getElement();
        return annotationForElement( platformLang, xsdElement.getAnnotation(), xsdElement.getName() );
      }

      if( adaptableObject instanceof IPropertyType )
      {
        final IPropertyType pt = (IPropertyType) adaptableObject;
        return createDefaultAnnotation( platformLang, pt.getQName().getLocalPart(), true );
      }
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

  /**
   * Same as {@link #annotationForElement(String, Annotation, String, true)}.
   */
  public static IAnnotation annotationForElement( final String platformLang, final Annotation xsdAnnotation, final String defaultName )
  {
    return annotationForElement( platformLang, xsdAnnotation, defaultName, true );
  }

  /**
   * Create an IAnnotation from an xsd-annotation element.
   * <p>
   * All inner elements from the dcoument element are regarded as annotations.
   * </p>
   * TODO: instead of restricting to one language here, we should parse into an IAnnotationProvider and return it.
   * 
   * @param createDefaultAnnotation
   *            If true, even if no annotation data is found, return a default annotation based on the defaultName; if
   *            false it returns null in this case.
   */
  public static IAnnotation annotationForElement( final String platformLang, final Annotation xsdAnnotation, final String defaultName, final boolean createDefaultAnnotation )
  {
    if( xsdAnnotation == null )
      return createDefaultAnnotation( platformLang, defaultName, createDefaultAnnotation );
    final Documentation[] documentationArray = xsdAnnotation.getDocumentationArray();
    if( documentationArray == null )
      return createDefaultAnnotation( platformLang, defaultName, createDefaultAnnotation );

    for( final Documentation documentation : documentationArray )
    {
      final String lang = documentation.getLang();
      if( platformLang.equals( lang ) )
      {
        final DefaultAnnotation annotation = new DefaultAnnotation( lang, defaultName );

        // Put all inner elements into the annotation-hash
        final Node domNode = documentation.getDomNode();
        final NodeList childNodes = domNode.getChildNodes();
        for( int n = 0; n < childNodes.getLength(); n++ )
        {
          final Node node = childNodes.item( n );
          if( NS.XSD_SCHEMA.equals( node.getNamespaceURI() ) )
          {
            final String name = node.getLocalName();
            final String value = getStringValue( node );
            annotation.putValue( name, value );
          }
        }

        return annotation;
      }
    }
    final String elementName = defaultName;
    if( elementName != null )
      return createDefaultAnnotation( platformLang, elementName, createDefaultAnnotation );
    else
      return createDefaultAnnotation( platformLang, "?", createDefaultAnnotation );
  }

  /**
   * @param createDefaultAnnotation
   *            If false, return null.
   */
  private static IAnnotation createDefaultAnnotation( final String lang, final String defaultName, final boolean createDefaultAnnotation )
  {
    if( !createDefaultAnnotation )
      return null;

    return new DefaultAnnotation( lang, defaultName );
  }

  public Class< ? >[] getAdapterList( )
  {
    return new Class[] { IAnnotation.class };
  }

  /**
   * Returns the text contained in the specified element. The returned value is trimmed by calling the trim() method of
   * java.lang.String
   * <p>
   * 
   * @param node
   *            current element
   * @return the textual contents of the element or null, if it is missing
   */
  private static String getStringValue( final Node node )
  {
    final NodeList children = node.getChildNodes();

    final StringBuffer sb = new StringBuffer( children.getLength() * 500 );
    for( int i = 0; i < children.getLength(); i++ )
    {
      if( children.item( i ).getNodeType() == Node.TEXT_NODE || children.item( i ).getNodeType() == Node.CDATA_SECTION_NODE )
        sb.append( children.item( i ).getNodeValue() );
    }

    return sb.toString().trim();
  }

}
