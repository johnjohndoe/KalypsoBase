package org.kalypso.ui.editor.gmleditor.ui;

import javax.xml.namespace.QName;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;
import org.eclipse.core.expressions.PropertyTester;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.Feature;

public class GmlEditorPropertyTester extends PropertyTester
{
  private static final String PROPERTY_QNAME = "qname"; //$NON-NLS-1$

  private static final String ARG_PROPERTY = "property"; //$NON-NLS-1$

  private static final String ARG_FEATURE = "feature"; //$NON-NLS-1$

  @Override
  public boolean test( final Object receiver, final String property, final Object[] args, final Object expectedValue )
  {
    if( PROPERTY_QNAME.equals( property ) )
    {
      final IFeatureType featureType = findFeatureType( receiver, args );
      if( featureType == null )
        return false;

      final String excpectedStr = expectedValue.toString().replaceAll( "\"", "" ); //$NON-NLS-1$ //$NON-NLS-2$
      final QName expectedQName = QName.valueOf( excpectedStr );
      return GMLSchemaUtilities.substitutes( featureType, expectedQName );
    }

    final String msg = String.format( "Unknown test property %s", property );
    throw new NotImplementedException( msg );
  }

  private IFeatureType findFeatureType( final Object receiver, final Object[] args )
  {
    final boolean allowProperties = checkArguments( args, ARG_PROPERTY );
    final boolean allowFeatures = checkArguments( args, ARG_FEATURE );

    if( allowProperties && receiver instanceof FeatureAssociationTypeElement )
    {
      final FeatureAssociationTypeElement fate = (FeatureAssociationTypeElement) receiver;
      final IRelationType fateRT = fate.getAssociationTypeProperty();
      return fateRT.getTargetFeatureType();
    }

    if( allowFeatures && receiver instanceof Feature )
      return ((Feature) receiver).getFeatureType();

    return null;
  }

  private boolean checkArguments( final Object[] args, final String argument )
  {
    /* Empty arguments always count for 'all' */
    if( args == null || args.length == 0 )
      return true;

    return ArrayUtils.contains( args, argument );
  }

}
