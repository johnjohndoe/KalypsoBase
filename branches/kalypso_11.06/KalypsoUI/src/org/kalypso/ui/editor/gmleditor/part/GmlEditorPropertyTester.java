package org.kalypso.ui.editor.gmleditor.part;

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

  private static final String PROPERTY_ROOT_QNAME = "rootQName"; //$NON-NLS-1$

  private static final String ARG_PROPERTY = "property"; //$NON-NLS-1$

  private static final String ARG_FEATURE = "feature"; //$NON-NLS-1$

  @Override
  public boolean test( final Object receiver, final String property, final Object[] args, final Object expectedValue )
  {
    if( PROPERTY_QNAME.equals( property ) )
      return testQName( receiver, args, expectedValue );

    if( PROPERTY_ROOT_QNAME.equals( property ) )
      return testRootQName( receiver, expectedValue );

    final String msg = String.format( "Unknown test property %s", property ); //$NON-NLS-1$
    throw new NotImplementedException( msg );
  }

  private boolean testRootQName( final Object receiver, final Object expectedValue )
  {
    final Feature rootFeature = findRootFeature( receiver );
    if( rootFeature == null )
      return false;

    final QName expectedQName = asQName( expectedValue );

    final IFeatureType featureType = rootFeature.getFeatureType();

    return GMLSchemaUtilities.substitutes( featureType, expectedQName );
  }

  protected boolean testQName( final Object receiver, final Object[] args, final Object expectedValue )
  {
    final IFeatureType featureType = findFeatureType( receiver, args );
    if( featureType == null )
      return false;

    final QName expectedQName = asQName( expectedValue );
    return GMLSchemaUtilities.substitutes( featureType, expectedQName );
  }

  protected QName asQName( final Object expectedValue )
  {
    final String excpectedStr = expectedValue.toString().replaceAll( "\"", "" ); //$NON-NLS-1$ //$NON-NLS-2$
    return QName.valueOf( excpectedStr );
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

  private Feature findRootFeature( final Object receiver )
  {
    if( receiver instanceof FeatureAssociationTypeElement )
    {
      final FeatureAssociationTypeElement fate = (FeatureAssociationTypeElement) receiver;
      return findRootFeature( fate.getParentFeature() );
    }

    if( receiver instanceof Feature )
      return ((Feature) receiver).getWorkspace().getRootFeature();

    return null;
  }
}