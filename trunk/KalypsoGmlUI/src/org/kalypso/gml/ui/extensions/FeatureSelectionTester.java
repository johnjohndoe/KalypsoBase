package org.kalypso.gml.ui.extensions;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.ISelection;
import org.kalypso.gml.ui.util.GenericFeatureSelection;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.ogc.gml.selection.EasyFeatureWrapper;
import org.kalypso.ogc.gml.selection.IFeatureSelection;

public class FeatureSelectionTester extends PropertyTester
{
  private static final String PROPERTY_IS_NOT_EMPTY = "isNotEmpty"; //$NON-NLS-1$

  private static final String PROPERTY_HAS_GEOMETRY_OF_TYPE = "hasGeometryOfType"; //$NON-NLS-1$

  /**
   * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[],
   *      java.lang.Object)
   */
  @Override
  public boolean test( final Object receiver, final String property, final Object[] args, final Object expectedValue )
  {
    final IFeatureSelection featureSelection = getFeatureSelection( receiver );

    if( PROPERTY_IS_NOT_EMPTY.equals( property ) )
      return testIsNotEmpty( featureSelection );

    if( PROPERTY_HAS_GEOMETRY_OF_TYPE.equals( property ) )
      return testHasGeometryOfType( featureSelection, expectedValue );

    return false;
  }

  private IFeatureSelection getFeatureSelection( final Object receiver )
  {
    if( receiver instanceof ISelection )
      return GenericFeatureSelection.create( (ISelection) receiver, null );

    return null;
  }

  private boolean testHasGeometryOfType( final IFeatureSelection featureSelection, final Object expectedValue )
  {
    if( !testIsNotEmpty( featureSelection ) )
      return false;

    try
    {
      final IFeatureType featureType = findLeastCommonType( featureSelection.getAllFeatures() );
      final Class< ? > geometryClass = getClass().getClassLoader().loadClass( expectedValue.toString() );
      final IValuePropertyType[] types = findGeometryTypes( featureType, geometryClass );
      return types.length > 0;
    }
    catch( final ClassNotFoundException e )
    {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * TODO: combine with GenericShapeDataFactory
   */
  private IFeatureType findLeastCommonType( final EasyFeatureWrapper[] allFeatures )
  {
    return allFeatures[0].getFeature().getFeatureType();
  }

  public static IValuePropertyType[] findGeometryTypes( final IFeatureType featureType, final Class< ? > geomType )
  {
    final Collection<IValuePropertyType> geometries = new ArrayList<IValuePropertyType>();
    final IPropertyType[] geometryProperties = featureType.getProperties();
    for( final IPropertyType propertyType : geometryProperties )
    {
      if( propertyType instanceof IValuePropertyType )
      {
        final IValuePropertyType vpt = (IValuePropertyType) propertyType;
        if( vpt.isGeometry() && !vpt.isList() )
        {
          // TODO: list of geometries not yet supported
          final Class< ? > valueClass = vpt.getValueClass();
          if( geomType.isAssignableFrom( valueClass ) )
            geometries.add( vpt );
        }
      }
    }

    return geometries.toArray( new IValuePropertyType[geometries.size()] );
  }

  private boolean testIsNotEmpty( final IFeatureSelection selection )
  {
    if( selection == null )
      return false;

    return !selection.isEmpty();
  }

}
