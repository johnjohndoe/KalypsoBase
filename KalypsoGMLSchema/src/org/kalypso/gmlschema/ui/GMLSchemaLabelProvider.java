/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 * 
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 * 
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 * 
 * and
 * 
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact:
 * 
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 * 
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.gmlschema.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.feature.IFeatureContentType;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.property.restriction.EnumerationRestriction;
import org.kalypso.gmlschema.property.restriction.MaxExclusiveRestriction;
import org.kalypso.gmlschema.property.restriction.MaxInclusiveRestriction;
import org.kalypso.gmlschema.property.restriction.MaxLengthRestriction;
import org.kalypso.gmlschema.property.restriction.MinExclusiveRestriction;
import org.kalypso.gmlschema.property.restriction.MinInclusiveRestriction;
import org.kalypso.gmlschema.property.restriction.MinLengthRestriction;

/**
 * TODO: insert type comment here
 * 
 * @author doemming
 */
public class GMLSchemaLabelProvider implements ILabelProvider
{

  /**
   * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
   */
  @Override
  public Image getImage( final Object element )
  {
    // no image
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
   */
  @Override
  public String getText( final Object element )
  {
    if( element == null )
      return "null"; //$NON-NLS-1$
    if( element instanceof LabelAndChildsProvider )
      return ((LabelAndChildsProvider) element).getText();
    if( element instanceof String )
      return (String) element;
    if( element instanceof GMLSchema )
      return "GMLSchema: " + ((IGMLSchema) element).getTargetNamespace(); //$NON-NLS-1$
    if( element instanceof IFeatureType )
      return "Feature: " + ((IFeatureType) element).getQName(); //$NON-NLS-1$

    if( element instanceof IFeatureContentType )
      return "ContentType: " + ((IFeatureContentType) element).getQName(); //$NON-NLS-1$
    final StringBuffer result = new StringBuffer();

    // IRestrictions
    if( element instanceof EnumerationRestriction )
    {
      final EnumerationRestriction restriction = (EnumerationRestriction) element;
      result.append( "enumeration:\n" ); //$NON-NLS-1$
      final Object[] enumeration = restriction.getEnumeration();
      for( final Object element2 : enumeration )
        result.append( "  " + element2 + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
      return result.toString();
    }
    if( element instanceof MinLengthRestriction )
    {
      final MinLengthRestriction restriction = (MinLengthRestriction) element;
      result.append( "  length >=" + restriction.getMinLength() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
      return result.toString();
    }
    if( element instanceof MaxLengthRestriction )
    {
      final MaxLengthRestriction restriction = (MaxLengthRestriction) element;
      result.append( "  length <=" + restriction.getMaxLength() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
      return result.toString();
    }
    if( element instanceof MinInclusiveRestriction )
    {
      final MinInclusiveRestriction restriction = (MinInclusiveRestriction) element;
      result.append( "  value >=" + restriction.getMinInclusive() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
      return result.toString();
    }
    if( element instanceof MinExclusiveRestriction )
    {
      final MinExclusiveRestriction restriction = (MinExclusiveRestriction) element;
      result.append( "  value >" + restriction.getMinExclusive() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
      return result.toString();
    }
    if( element instanceof MaxInclusiveRestriction )
    {
      final MaxInclusiveRestriction restriction = (MaxInclusiveRestriction) element;
      result.append( "  value <=" + restriction.getMaxInclusive() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
      return result.toString();
    }
    if( element instanceof MaxExclusiveRestriction )
    {
      final MaxExclusiveRestriction restriction = (MaxExclusiveRestriction) element;
      result.append( "  value <" + restriction.getMaxExclusive() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
      return result.toString();
    }

    if( element instanceof IPropertyType )
    {
      final IPropertyType prop = (IPropertyType) element;
      result.append( "[" + prop.getMinOccurs() + "," ); //$NON-NLS-1$ //$NON-NLS-2$
      final int maxOccurs = prop.getMaxOccurs();
      if( maxOccurs == IPropertyType.UNBOUND_OCCURENCY )
        result.append( "oo" ); //$NON-NLS-1$
      else
        result.append( maxOccurs );
      result.append( "]" ); //$NON-NLS-1$

    }

    if( element instanceof IRelationType )
    {
      final IRelationType relationType = (IRelationType) element;
      result.append( "  Relation: " + relationType.getQName() ); //$NON-NLS-1$

      final List<String> strings = new ArrayList<String>();

      if( relationType.isInlineAble() )
        strings.add( "inlinable" ); //$NON-NLS-1$
      if( relationType.isLinkAble() )
        strings.add( "linkable" ); //$NON-NLS-1$
      if( relationType.isNillable() )
        strings.add( "nillable" ); //$NON-NLS-1$

      if( strings.size() > 0 )
        result.append( " (" ); //$NON-NLS-1$
      for( final Iterator<String> iter = strings.iterator(); iter.hasNext(); )
      {
        result.append( iter.next() );
        if( iter.hasNext() )
          result.append( ',' );
      }
      if( strings.size() > 0 )
        result.append( ") " ); //$NON-NLS-1$
    }
    else if( element instanceof IValuePropertyType )
    {
      final IValuePropertyType vpt = (IValuePropertyType) element;
      result.append( "Property: " + vpt.getQName() ); //$NON-NLS-1$
      if( vpt.isGeometry() )
        result.append( "    [X] GEOMETRY" ); //$NON-NLS-1$
      result.append( "     Value: " + vpt.getValueQName() ); //$NON-NLS-1$
      result.append( "     Class: " + vpt.getValueClass() ); //$NON-NLS-1$
      if( vpt.hasDefault() )
        result.append( "     default: " + vpt.getDefault() ); //$NON-NLS-1$
      if( vpt.isFixed() )
        result.append( "     fixed: " + vpt.getFixed() ); //$NON-NLS-1$
    }
    else
      return "unknown"; //$NON-NLS-1$
    return result.toString();
  }

  /**
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
   */
  @Override
  public void addListener( final ILabelProviderListener listener )
  {
    // TODO Auto-generated method stub
  }

  /**
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
   */
  @Override
  public boolean isLabelProperty( final Object element, final String property )
  {
    return false;
  }

  /**
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
   */
  @Override
  public void removeListener( final ILabelProviderListener listener )
  {
    // TODO Auto-generated method stub
  }

}
