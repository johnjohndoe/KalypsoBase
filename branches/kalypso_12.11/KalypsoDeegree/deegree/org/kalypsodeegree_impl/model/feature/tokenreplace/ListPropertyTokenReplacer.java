package org.kalypsodeegree_impl.model.feature.tokenreplace;

import java.util.List;

import javax.xml.namespace.NamespaceContext;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.commons.tokenreplace.ITokenReplacer;
import org.kalypso.contribs.javax.xml.namespace.QNameUtilities;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathException;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;

/**
 * Tokens of kind ${listPoperty:-property path-;-index-;-null Value-}
 * 
 * @author Gernot Belger
 */
public final class ListPropertyTokenReplacer implements ITokenReplacer
{
  @Override
  public String replaceToken( final Object value, final String argument )
  {
    final Feature feature = (Feature) value;

    final String[] strings = argument.split( ";" );
    if( strings.length < 2 )
      return "Wrong argument for listProperty. Must be _qname_;listindex;[null-Value]";

    final String propertyPath = strings[0];
    final GMLXPath gmlxPath = parsePropertyPath( feature, propertyPath );

    try
    {
      final Object listObject = GMLXPathUtilities.query( gmlxPath, feature );
      final int listindex = Integer.parseInt( strings[1] );
      final String nullValue = strings.length > 2 ? strings[2] : StringUtils.EMPTY;

      if( listObject == null )
        return nullValue;

      if( !(listObject instanceof List) )
        return String.format( "Value object is not a list: %s", listObject );

      final List< ? > list = (List< ? >) listObject;

      if( listindex >= list.size() )
        return "" + nullValue;

      final Object propertyValue = list.get( listindex );
      if( propertyValue == null )
        return "" + nullValue;

      return "" + propertyValue;
    }
    catch( final GMLXPathException e )
    {
      return String.format( "Illegal XPath: %s", propertyPath );
    }
  }

  static GMLXPath parsePropertyPath( final Feature feature, final String propertyPath )
  {
    // REMARK: for backwards compatibility, we still parse paths of the form 'namespace#localPart' (for single qname's
    // only). This should not be used any more.
    if( propertyPath.contains( "#" ) ) //$NON-NLS-1$
      return new GMLXPath( QNameUtilities.createQName( propertyPath ) );

    // FIXME: get namespace context from outside, it is not always the feature's context
    final NamespaceContext namespaceContext = feature.getWorkspace().getNamespaceContext();
    return new GMLXPath( propertyPath, namespaceContext );
  }

  @Override
  public String getToken( )
  {
    return "listProperty";
  }
}