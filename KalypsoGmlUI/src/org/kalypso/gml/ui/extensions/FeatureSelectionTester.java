package org.kalypso.gml.ui.extensions;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.ISelection;
import org.kalypso.gml.ui.util.GenericFeatureSelection;
import org.kalypso.ogc.gml.selection.IFeatureSelection;

public class FeatureSelectionTester extends PropertyTester
{
  private static final String PROPERTY_HAS_FEATURE_SELECTION = "hasFeatureSelection"; //$NON-NLS-1$

  /**
   * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[],
   *      java.lang.Object)
   */
  @Override
  public boolean test( final Object receiver, final String property, final Object[] args, final Object expectedValue )
  {
    if( PROPERTY_HAS_FEATURE_SELECTION.equals( property ) )
      return testHasFeatureSelection( receiver );

    return false;
  }

  private boolean testHasFeatureSelection( final Object receiver )
  {
    if( receiver instanceof ISelection )
    {
      final IFeatureSelection selection = GenericFeatureSelection.create( (ISelection) receiver, null );
      return !selection.isEmpty();
    }

    return false;
  }

}
