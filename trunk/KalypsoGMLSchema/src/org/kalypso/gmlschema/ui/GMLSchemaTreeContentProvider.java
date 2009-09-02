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
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.feature.IDetailedFeatureType;
import org.kalypso.gmlschema.feature.IFeatureContentType;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;

/**
 * TODO: insert type comment here
 * 
 * @author doemming
 */
public class GMLSchemaTreeContentProvider implements ITreeContentProvider
{
  private boolean m_detailed;

  private IGMLSchema m_context;

  // key=schema, value=parent of schown schemas
  private final Hashtable<GMLSchema, GMLSchema> m_importMap = new Hashtable<GMLSchema, GMLSchema>();

  public GMLSchemaTreeContentProvider( final IGMLSchema context, final boolean detailed )
  {
    m_context = context;
    m_detailed = detailed;
  }

  public void setDetailed( final boolean detailed )
  {
    m_detailed = detailed;
  }

  public void setContext( final IGMLSchema context )
  {
    m_context = context;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  public Object[] getChildren( final Object parent )
  {
    if( parent == null )
      return new Object[0];
    if( parent instanceof LabelAndChildsProvider )
      return ((LabelAndChildsProvider) parent).getChilds();
    if( parent instanceof GMLSchema )
    {
      final GMLSchema schema = (GMLSchema) parent;

      final List<GMLSchema> schemasToShow = new ArrayList<GMLSchema>();
      schemasToShow.addAll( getSchemaChildren( schema, schema.getImports() ) );
      schemasToShow.addAll( getSchemaChildren( schema, schema.getAdditionalSchemas() ) );

      final IFeatureType[] featureTypes = schema.getAllFeatureTypes();
      final Object[] children = new Object[schemasToShow.size() + featureTypes.length];
      System.arraycopy( schemasToShow.toArray(), 0, children, 0, schemasToShow.size() );
      System.arraycopy( featureTypes, 0, children, schemasToShow.size(), featureTypes.length );
      return children;
    }

    if( !m_detailed )
    {
      if( parent instanceof IFeatureType )
        return ((IFeatureType) parent).getProperties();
    }
    else
    {
      if( parent instanceof IDetailedFeatureType )
      {
        final IFeatureContentType featureContentType = ((IDetailedFeatureType) parent).getFeatureContentType();
        return new Object[] { featureContentType };
      }
      if( parent instanceof IFeatureContentType )
      {
        final IFeatureContentType fct = (IFeatureContentType) parent;
        final IPropertyType[] properties = fct.getDirectProperties();
        final String label;
        switch( fct.getDerivationType() )
        {
          case IFeatureContentType.DERIVATION_NONE:
            return properties;
          case IFeatureContentType.DERIVATION_BY_EXTENSION:
            label = "extended by"; //$NON-NLS-1$
            break;
          case IFeatureContentType.DERIVATION_BY_RESTRICTION:
            label = "restricted by"; //$NON-NLS-1$
            break;
          default:
            throw new UnsupportedOperationException();
        }
        final LabelAndChildsProvider provider = new LabelAndChildsProvider( label, new Object[] { fct.getBase() } );
        final Object[] result = new Object[properties.length + 1];
        result[0] = provider;
        for( int i = 0; i < properties.length; i++ )
          result[i + 1] = properties[i];
        return result;
      }
    }

    if( parent instanceof IRelationType )
    {
      final IRelationType relationType = (IRelationType) parent;
      final IFeatureType targetFeatureType = relationType.getTargetFeatureType();
      final IFeatureType[] targetFeatureTypes = GMLSchemaUtilities.getSubstituts( targetFeatureType, m_context, true, true );
      final TreeSet<IFeatureType> sortedFTs = new TreeSet<IFeatureType>( new Comparator<IFeatureType>()
      {
        public int compare( IFeatureType ft1, IFeatureType ft2 )
        {
          String ns1 = ft1.getQName().getNamespaceURI();
          String ns2 = ft2.getQName().getNamespaceURI();
          if( ns1 == null )
            ns1 = ""; //$NON-NLS-1$
          if( ns2 == null )
            ns2 = ""; //$NON-NLS-1$
          if( ns1.equals( ns2 ) )
          {
            final String lp1 = ft1.getQName().getLocalPart();
            final String lp2 = ft2.getQName().getLocalPart();
            return lp1.compareTo( lp2 );
          }
          return ns1.compareTo( ns2 );
        }
      } );
      for( final IFeatureType ft : targetFeatureTypes )
      {
        sortedFTs.add( ft );
      }

      final List<String> result = new ArrayList<String>();
      for( final IFeatureType ft : sortedFTs )
      {
        final String isAbstract;
        if( ft.isAbstract() )
          isAbstract = "\t  [x] abstract"; //$NON-NLS-1$
        else
          isAbstract = ""; //$NON-NLS-1$
        result.add( " -> " + ft.getQName() + isAbstract ); //$NON-NLS-1$
      }
      return result.toArray();
    }
    if( parent instanceof IValuePropertyType )
    {
      final IValuePropertyType vpt = (IValuePropertyType) parent;
      if( vpt.hasRestriction() )
        return vpt.getRestriction();
    }
    return new Object[0];
  }

  private List<GMLSchema> getSchemaChildren( final GMLSchema schema, final GMLSchema[] importedSchemas )
  {
    final List<GMLSchema> schemasToShow = new ArrayList<GMLSchema>();
    for( final GMLSchema _import : importedSchemas )
    {
      if( !m_importMap.containsKey( _import ) )
      {
        schemasToShow.add( _import );
        m_importMap.put( _import, schema );
      }
      else
      {
        // if called for the same position (e.g. update for a view)
        if( m_importMap.get( _import ) == schema )
          schemasToShow.add( _import );
      }
    }
    return schemasToShow;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  public Object getParent( final Object element )
  {
    // is not supported
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  public boolean hasChildren( final Object element )
  {
    return getChildren( element ).length > 0;
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements( final Object inputElement )
  {
    return getChildren( inputElement );
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public void dispose( )
  {
    // nothing to dispose
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
   *      java.lang.Object)
   */
  public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
  {
    m_importMap.clear();
    setContext( (IGMLSchema) newInput );
  }

  public void accept( final Object element, final ITreeContentProviderVisitor visitor, int indent )
  {
    if( visitor.visit( element, indent ) )
    {
      indent++;

      final Object[] children = getChildren( element );
      for( final Object child : children )
        accept( child, visitor, indent );
    }
  }
}
