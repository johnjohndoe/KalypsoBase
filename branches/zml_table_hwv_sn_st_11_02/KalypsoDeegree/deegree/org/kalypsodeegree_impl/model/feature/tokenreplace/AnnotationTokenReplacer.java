package org.kalypsodeegree_impl.model.feature.tokenreplace;

import javax.xml.namespace.NamespaceContext;

import org.kalypso.commons.tokenreplace.ITokenReplacer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathException;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;

/**
 * Tokens of kind ${annotation:-propertyPath-;-annotation-type-}, replaced by the annotation of the denoted property
 * (value).
 * 
 * @author Gernot Belger
 */
public final class AnnotationTokenReplacer implements ITokenReplacer
{
  @Override
  public String replaceToken( final Object value, final String argument )
  {
    final Feature feature = (Feature) value;

    final String[] split = argument.split( ";", 2 );
    if( split.length != 2 )
      return "Wrong argument for annotation. Must be propertyPath;annotation-type";

    // TODO: where from?
    final NamespaceContext namespaceContext = null;
    final GMLXPath propertyPath = new GMLXPath( split[0], namespaceContext );
    final String annotationType = split[1];

    try
    {
      final Object referencedElement = GMLXPathUtilities.query( propertyPath, feature );

      if( referencedElement instanceof Feature )
        return FeatureHelper.getAnnotationValue( (Feature) referencedElement, annotationType );

      if( referencedElement instanceof FeatureList )
      {
        // ??
      }
    }
    catch( final GMLXPathException e )
    {
      e.printStackTrace();
    }

    return "-";
  }

  @Override
  public String getToken( )
  {
    return "annotation";
  }
}