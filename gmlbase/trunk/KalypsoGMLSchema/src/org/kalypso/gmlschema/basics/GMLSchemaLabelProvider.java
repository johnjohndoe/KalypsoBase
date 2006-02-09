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
package org.kalypso.gmlschema.basics;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.feature.IFeatureContentType;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.property.restriction.EnumerationResitriction;
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
  public Image getImage( Object element )
  {
    // no image
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
   */
  public String getText( Object element )
  {
    if( element == null )
      return "null";
    if( element instanceof LabelAndChildsProvider )
      return ((LabelAndChildsProvider) element).getText();
    if( element instanceof String )
      return (String) element;
    if( element instanceof GMLSchema )
      return "GMLSchema: " + ((GMLSchema) element).getTargetNamespace();
    if( element instanceof IFeatureType )
      return "Feature: " + ((IFeatureType) element).getQName();

    if( element instanceof IFeatureContentType )
      return "ContentType: " + ((IFeatureContentType) element).getQName();
    final StringBuffer result = new StringBuffer();

    // IRestrictions
    if( element instanceof EnumerationResitriction )
    {
      final EnumerationResitriction restriction = (EnumerationResitriction) element;
      result.append( "enumeration:\n" );
      final String[] enumeration = restriction.getEnumeration();
      for( int i = 0; i < enumeration.length; i++ )
        result.append( "  " + enumeration[i] + "\n" );
      return result.toString();
    }
    if( element instanceof MinLengthRestriction )
    {
      MinLengthRestriction restriction = (MinLengthRestriction) element;
      result.append( "  length >=" + restriction.getMinLength() + "\n" );
      return result.toString();
    }
    if( element instanceof MaxLengthRestriction )
    {
      MaxLengthRestriction restriction = (MaxLengthRestriction) element;
      result.append( "  length <=" + restriction.getMaxLength() + "\n" );
      return result.toString();
    }
    if( element instanceof MinInclusiveRestriction )
    {
      MinInclusiveRestriction restriction = (MinInclusiveRestriction) element;
      result.append( "  value >=" + restriction.getMinInclusive() + "\n" );
      return result.toString();
    }
    if( element instanceof MinExclusiveRestriction )
    {
      MinExclusiveRestriction restriction = (MinExclusiveRestriction) element;
      result.append( "  value >" + restriction.getMinExclusive() + "\n" );
      return result.toString();
    }
    if( element instanceof MaxInclusiveRestriction )
    {
      MaxInclusiveRestriction restriction = (MaxInclusiveRestriction) element;
      result.append( "  value <=" + restriction.getMaxInclusive() + "\n" );
      return result.toString();
    }
    if( element instanceof MaxExclusiveRestriction )
    {
      MaxExclusiveRestriction restriction = (MaxExclusiveRestriction) element;
      result.append( "  value <" + restriction.getMaxExclusive() + "\n" );
      return result.toString();
    }

    if( element instanceof IPropertyType )
    {
      final IPropertyType prop = (IPropertyType) element;
      result.append( "[" + prop.getMinOccurs() + "," );
      int maxOccurs = prop.getMaxOccurs();
      if( maxOccurs == IPropertyType.UNBOUND_OCCURENCY )
        result.append( "oo" );
      else
        result.append( maxOccurs );
      result.append( "]" );

    }

    if( element instanceof IRelationType )
      result.append( "  Relation: " + ((IRelationType) element).getQName() );
    else if( element instanceof IValuePropertyType )
    {
      final IValuePropertyType vpt = (IValuePropertyType) element;
      result.append( "Property: " + vpt.getQName() );
      if( vpt.isGeometry() )
        result.append( "    [X] GEOMETRY" );
      result.append( "\n     Value: " + vpt.getValueQName() );
      result.append( "\n     Class: " + vpt.getValueClass() );
      if( vpt.hasDefault() )
        result.append( "\n     default: " + vpt.getDefault() );
      if( vpt.isFixed() )
        result.append( "\n     fixed: " + vpt.getFixed() );
    }
    else
      return "unknown";
    return result.toString();
  }

  /**
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
   */
  public void addListener( ILabelProviderListener listener )
  {
    // TODO Auto-generated method stub
  }

  /**
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
   */
  public void dispose( )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
   */
  public boolean isLabelProperty( Object element, String property )
  {
    return false;
  }

  /**
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
   */
  public void removeListener( ILabelProviderListener listener )
  {
    // TODO Auto-generated method stub
  }

}
