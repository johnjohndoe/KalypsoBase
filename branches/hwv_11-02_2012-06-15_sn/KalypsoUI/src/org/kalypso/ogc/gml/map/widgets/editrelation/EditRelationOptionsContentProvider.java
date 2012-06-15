/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.map.widgets.editrelation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author doemming
 */
public class EditRelationOptionsContentProvider implements ITreeContentProvider
{
  private final Hashtable<Object, Object[]> m_childCache = new Hashtable<Object, Object[]>();

  private final Hashtable<Object, Object> m_parentCache = new Hashtable<Object, Object>();

  private final HashSet<Object> m_checkedElements = new HashSet<Object>();

  public EditRelationOptionsContentProvider( )
  {
    // nothing
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  @Override
  public Object[] getChildren( final Object parentElement )
  {
    if( m_childCache.containsKey( parentElement ) )
      return m_childCache.get( parentElement );
    final List<Object> result = new ArrayList<Object>();
    if( parentElement == null )
      return new Object[0];

    if( parentElement instanceof IKalypsoFeatureTheme )
    {
      final IKalypsoFeatureTheme featureTheme = (IKalypsoFeatureTheme) parentElement;
      final CommandableWorkspace workspace = featureTheme.getWorkspace();
      if( workspace != null )
        result.add( workspace );
    }
    if( parentElement instanceof GMLWorkspace )
    {
      final IFeatureType[] featureTypes = ((GMLWorkspace) parentElement).getGMLSchema().getAllFeatureTypes();

      for( final IFeatureType ft : featureTypes )
      {
        if( /*!ft.isAbstract() && */ft.getDefaultGeometryProperty() != null )
          result.add( ft );
      }
    }

    if( parentElement instanceof HeavyRelationType )
    {
      final HeavyRelationType relation = (HeavyRelationType) parentElement;
      final IFeatureType destFT = relation.getLink2().getTargetFeatureType();

      final IFeatureType destFT2 = relation.getDestFT(); // where is the
      // difference ?
      final IFeatureType associationFeatureType = relation.getLink2().getTargetFeatureType();
      final IFeatureType[] associationFeatureTypes = GMLSchemaUtilities.getSubstituts( associationFeatureType, null, false, true );

      if( destFT == destFT2 )
        for( final IFeatureType associationFeatureType2 : associationFeatureTypes )
        {
          final IFeatureType ft = associationFeatureType2;
          if( /* !ft.isAbstract() && */!ft.equals( destFT ) )
            result.add( new HeavyRelationType( relation.getSrcFT(), relation.getLink1(), relation.getBodyFT(), relation.getLink2(), ft ) );
        }
    }
    else if( parentElement instanceof RelationType )
    {
      final RelationType relation = (RelationType) parentElement;
      final IFeatureType destFT = relation.getLink().getTargetFeatureType();
      final IFeatureType destFT2 = relation.getDestFT();
      final IFeatureType associationFeatureType = relation.getLink().getTargetFeatureType();

      final IFeatureType[] associationFeatureTypes = GMLSchemaUtilities.getSubstituts( associationFeatureType, null, false, true );
      if( destFT == destFT2 )
        for( final IFeatureType associationFeatureType2 : associationFeatureTypes )
        {
          final IFeatureType ft = associationFeatureType2;
          if( /* !ft.isAbstract() && */!ft.equals( destFT ) )
            result.add( new RelationType( relation.getSrcFT(), relation.getLink(), ft ) );
        }
    }
    if( parentElement instanceof IFeatureType )
    {
      final IFeatureType ft1 = (IFeatureType) parentElement;
// if( !ft1.isAbstract() )
      {
        final IPropertyType[] properties = ft1.getProperties();
        for( final IPropertyType property : properties )
        {
          if( property instanceof IRelationType )
          {
            final IRelationType linkFTP1 = (IRelationType) property;
            final IFeatureType ft2 = linkFTP1.getTargetFeatureType();
            // leight: FT,Prop,FT
            // heavy: FT,Prop,FT,PropFT
            // leight relationship ?
//            if( ft2.getDefaultGeometryProperty() != null /* &&!ft2.isAbstract() */)
//              result.add( new RelationType( ft1, linkFTP1, ft2 ) );
//            else
//            {
              // heavy relationship ?
              final IFeatureType ft2a = linkFTP1.getTargetFeatureType();
              final IFeatureType[] ft2s = GMLSchemaUtilities.getSubstituts( ft2a, null, false, true );
              for( final IFeatureType ft22 : ft2s )
              {
                final IPropertyType[] properties2 = ft22.getProperties();
                for( final IPropertyType property2 : properties2 )
                {
                  if( property2 instanceof IRelationType )
                  {
                    final IRelationType linkFTP2 = (IRelationType) property2;
                    final IFeatureType ft3 = linkFTP2.getTargetFeatureType();
                    if( /* !ft3.isAbstract() && */ft3.getDefaultGeometryProperty() != null )
                    {
                      // it is a heavy relationship;
                      result.add( new HeavyRelationType( ft1, linkFTP1, ft22, linkFTP2, ft3 ) );
                    }
                  }
                }
              }
//            }
          }
        }
      }
    }
    final Object[] array = result.toArray();
    if( array.length > 0 )
    {
      m_childCache.put( parentElement, array );
      for( final Object element : array )
        m_parentCache.put( element, parentElement );
    }
    if( m_childCache.containsKey( parentElement ) )
      return m_childCache.get( parentElement );
    return new Object[0];
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  @Override
  public Object getParent( final Object element )
  {
    if( m_parentCache.containsKey( element ) )
    {
      return m_parentCache.get( element );
    }
    // can not compute parent
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  @Override
  public boolean hasChildren( final Object element )
  {
    if( element == null )
      return false;
    return getChildren( element ).length > 0;
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  @Override
  public Object[] getElements( final Object inputElement )
  {
    if( inputElement != null && inputElement instanceof IKalypsoFeatureTheme )
    {
      final IKalypsoFeatureTheme featureTheme = (IKalypsoFeatureTheme) inputElement;
      return new GMLWorkspace[] { featureTheme.getWorkspace() };
    }
    return new Object[0];
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    // nothing to do
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
   *      java.lang.Object)
   */
  @Override
  public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
  {
    // final CheckboxTreeViewer treeviewer = (CheckboxTreeViewer)viewer;
    // {
    // treeviewer.getControl().getDisplay().asyncExec( new Runnable()
    // {
    // public void run()
    // {
    // if( treeviewer != null && !treeviewer.getControl().isDisposed() )
    // {
    // treeviewer.expandAll();
    // treeviewer.setCheckedElements( getCheckedElements() );
    // }
    // }
    // } );
    // }
  }

  public boolean isChecked( final Object element )
  {
    return m_checkedElements.contains( element );
  }

  public void setChecked( final Object element, final boolean checked )
  {
    if( checked )
      m_checkedElements.add( element );
    else
      m_checkedElements.remove( element );
  }

  Object[] getCheckedElements( )
  {
    return m_checkedElements.toArray();
  }

  public org.kalypso.ogc.gml.map.widgets.editrelation.IRelationType[] getCheckedRelations( )
  {
    final List<org.kalypso.ogc.gml.map.widgets.editrelation.IRelationType> result = new ArrayList<org.kalypso.ogc.gml.map.widgets.editrelation.IRelationType>();
    for( final Object element : m_checkedElements )
    {
      if( element instanceof org.kalypso.ogc.gml.map.widgets.editrelation.IRelationType )
        result.add( (org.kalypso.ogc.gml.map.widgets.editrelation.IRelationType) element );
    }
    return result.toArray( new org.kalypso.ogc.gml.map.widgets.editrelation.IRelationType[result.size()] );
  }
}