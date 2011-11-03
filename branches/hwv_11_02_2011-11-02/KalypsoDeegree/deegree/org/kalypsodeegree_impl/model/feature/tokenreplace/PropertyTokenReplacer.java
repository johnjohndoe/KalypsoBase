package org.kalypsodeegree_impl.model.feature.tokenreplace;

import org.kalypso.commons.tokenreplace.ITokenReplacer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathException;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;

/**
 * Tokens of kind ${property:-property path-;-null Value-}
 * 
 * @author Gernot Belger
 */
public final class PropertyTokenReplacer implements ITokenReplacer
{
  @Override
  public String replaceToken( final Object value, final String argument )
  {
    final Feature feature = (Feature) value;

    final String[] strings = argument.split( ";" );
    if( strings.length == 0 )
      return "No argument for property. Must be _qname_;[null-value];[format-string]";

    final String propertyPath = strings[0];

    final GMLXPath gmlxPath = ListPropertyTokenReplacer.parsePropertyPath( feature, propertyPath );

    final String nullValue = strings.length > 1 ? strings[1] : null;
    final String formatString = strings.length > 2 ? strings[2] : null;

    try
    {
      final Object property = GMLXPathUtilities.query( gmlxPath, feature );

      if( property == null )
        return "" + nullValue;

      if( formatString != null )
        return String.format( formatString, property );

      return "" + property;
    }
    catch( final GMLXPathException e )
    {
      return String.format( "Illegal XPath: %s", propertyPath );
    }
  }

  @Override
  public String getToken( )
  {
    return "property";
  }
}