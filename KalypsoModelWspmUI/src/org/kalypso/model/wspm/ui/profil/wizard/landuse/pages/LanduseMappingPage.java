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

import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.kalypso.commons.databinding.DataBinder;
import org.kalypso.commons.databinding.jface.wizard.DatabindingWizardPage;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.viewers.IRefreshable;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.gml.IWspmProject;
import org.kalypso.model.wspm.core.gml.classifications.IClassificationClass;
import org.kalypso.model.wspm.core.gml.classifications.IWspmClassification;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.utils.ILanduseShapeDataProvider;
import org.kalypso.shape.ShapeFile;
import org.kalypso.shape.dbf.FieldType;
import org.kalypso.shape.dbf.IDBFField;

/**
 * @author Dirk Kuch
 */
public class LanduseMappingPage extends WizardPage implements IRefreshable, ILanduseMapping
{
  protected final ILanduseShapeDataProvider m_provider;

  protected final ImportLanduseDataModel m_model = new ImportLanduseDataModel();

  private DatabindingWizardPage m_binding;

  private ComboViewer m_column;

  private final String m_type;

  private LanduseMappingTable m_table;

  private ShapeFile m_shapeFile;

  public LanduseMappingPage( final ILanduseShapeDataProvider provider, final String type )
  {
    super( "LanduseMappingPage" ); //$NON-NLS-1$

    m_provider = provider;
    m_type = type;

    if( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS.equals( type ) )
    {
      setTitle( Messages.getString("LanduseMappingPage.0") ); //$NON-NLS-1$
    }
    else if( IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS.equals( type ) )
    {
      setTitle( Messages.getString("LanduseMappingPage.1") ); //$NON-NLS-1$
    }

    setDescription( Messages.getString("LanduseMappingPage.2") ); //$NON-NLS-1$
  }

  @Override
  public void createControl( final Composite parent )
  {
    m_binding = new DatabindingWizardPage( this, null );

    final Composite body = new Composite( parent, SWT.NULL );
    body.setLayout( new GridLayout() );

    new Label( body, SWT.NULL ).setText( Messages.getString("LanduseMappingPage.3") ); //$NON-NLS-1$
    m_column = getViewer( body );

    m_column.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @SuppressWarnings("synthetic-access")
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        try
        {
          final LanduseTableMappingHandler handler = new LanduseTableMappingHandler( m_provider, m_model );
          getContainer().run( false, false, handler );

          m_table.refresh();
        }
        catch( final Exception e )
        {
          e.printStackTrace();
        }
      }
    } );

    new Label( body, SWT.NULL ).setText( " " );// spacer //$NON-NLS-1$

    new Label( body, SWT.NULL ).setText( Messages.getString("LanduseMappingPage.4") ); //$NON-NLS-1$
    m_table = new LanduseMappingTable( body, this );
    m_table.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    setControl( body );
  }

  private ComboViewer getViewer( final Composite body )
  {
    final ComboViewer viewer = new ComboViewer( body, SWT.BORDER );
    viewer.getCombo().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    viewer.setLabelProvider( new LabelProvider()
    {
      @Override
      public String getText( final Object element )
      {
        if( element instanceof IDBFField )
        {
          final IDBFField field = (IDBFField) element;

          return field.getName();
        }

        return super.getText( element );
      }
    } );
    viewer.setContentProvider( new ArrayContentProvider() );

    final FieldType[] filter = new FieldType[] { FieldType.D, FieldType.F, FieldType.L, FieldType.M, FieldType.N };
    viewer.addFilter( new ViewerFilter()
    {

      @Override
      public boolean select( final Viewer v, final Object parentElement, final Object element )
      {
        if( element instanceof IDBFField )
        {
          final IDBFField field = (IDBFField) element;
          final FieldType type = field.getType();
          if( ArrayUtils.contains( filter, type ) )
            return false;
        }

        return true;
      }
    } );

    final IObservableValue viewerSelection = ViewersObservables.observeSingleSelection( viewer );
    final IObservableValue modelValue = BeansObservables.observeValue( m_model, ImportLanduseDataModel.PROPERTY_SHAPE_COLUMN );

    m_binding.bindValue( new DataBinder( viewerSelection, modelValue ) );

    return viewer;
  }

  @Override
  public void refresh( )
  {
    try
    {
      final ShapeFile shape = m_provider.getShapeFile();
      if( Objects.notEqual( m_shapeFile, shape ) )
      {
        m_column.setInput( shape.getFields() );
        m_shapeFile = shape;
      }
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();
    }
  }

  /**
   * @see org.kalypso.model.wspm.ui.profil.wizard.landuse.pages.ILanduseMapping#getProperties()
   */
  @Override
  public Properties getProperties( )
  {
    return m_model.getMapping();
  }

  @Override
  public IClassificationClass[] getClasses( )
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

  @Override
  public IDBFField getSelectedColumn( )
  {
    return m_model.getShapeColumn();
  }
}
