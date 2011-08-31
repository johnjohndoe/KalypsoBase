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
package org.kalypso.model.wspm.ui.profil.wizard.landuse.pages;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.contribs.eclipse.jface.viewers.ArrayTreeContentProvider;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.gml.IWspmProject;
import org.kalypso.model.wspm.core.gml.classifications.IClassificationClass;
import org.kalypso.model.wspm.core.gml.classifications.IWspmClassification;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.utils.ILanduseShapeDataProvider;

/**
 * @author Dirk Kuch
 */
public class LanduseMappingTable extends Composite
{

  private TableViewer m_viewer;

  private final ImportLanduseDataModel m_model;

  TableColumnLayout m_layout = new TableColumnLayout();

  private final ILanduseShapeDataProvider m_provider;

  private final String m_type;

  public LanduseMappingTable( final Composite parent, final ImportLanduseDataModel model, final ILanduseShapeDataProvider provider, final String type )
  {
    super( parent, SWT.NULL );
    m_model = model;
    m_provider = provider;
    m_type = type;
    setLayout( m_layout );

    init();
  }

  private void init( )
  {
    final IClassificationClass[] clazzes = getClasses();

    m_viewer = new TableViewer( this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION );
    m_viewer.getTable().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    m_viewer.setContentProvider( new ArrayTreeContentProvider()
    {
      /**
       * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
       */
      @Override
      public Object[] getElements( final Object inputElement )
      {

        if( inputElement instanceof Properties )
        {
          final Properties properties = (Properties) inputElement;
          final Set<Entry<Object, Object>> entries = properties.entrySet();

          return entries.toArray();
        }

        return super.getElements( inputElement );
      }
    } );

    m_viewer.getTable().setLinesVisible( true );
    m_viewer.getTable().setHeaderVisible( true );

    final TableViewerColumn shapeColumn = new TableViewerColumn( m_viewer, SWT.NONE );
    shapeColumn.getColumn().setText( "Property" );
    m_layout.setColumnData( shapeColumn.getColumn(), new ColumnWeightData( 50 ) );
    shapeColumn.setLabelProvider( new LanduseMappingLabelProvider( clazzes, 0 ) );

    final TableViewerColumn clazzColumn = new TableViewerColumn( m_viewer, SWT.NONE );
    clazzColumn.getColumn().setText( "Classifiation" );
    m_layout.setColumnData( clazzColumn.getColumn(), new ColumnWeightData( 50 ) );

    clazzColumn.setLabelProvider( new LanduseMappingLabelProvider( clazzes, 1 ) );
    clazzColumn.setEditingSupport( new LandUseMappingEditingSupport( m_viewer, clazzes ) );
  }

  private IClassificationClass[] getClasses( )
  {
    try
    {
      final IWspmProject project = m_provider.getWspmModel();
      final IWspmClassification classification = project.getClassificationMember();

      if( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS.equals( m_type ) )
        return classification.getVegetationClasses();
      else if( IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS.equals( m_type ) )
        return classification.getRoughnessClasses();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    throw new UnsupportedOperationException();
  }

  public void refresh( )
  {
    m_viewer.setInput( m_model.getMapping() );
  }

}
