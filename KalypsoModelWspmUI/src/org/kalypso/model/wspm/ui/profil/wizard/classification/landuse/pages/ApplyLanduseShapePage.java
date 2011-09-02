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
package org.kalypso.model.wspm.ui.profil.wizard.classification.landuse.pages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.kalypso.commons.databinding.DataBinder;
import org.kalypso.commons.databinding.jface.wizard.DatabindingWizardPage;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.ui.profil.wizard.classification.landuse.model.ALSSelectedPropertyChanged;
import org.kalypso.model.wspm.ui.profil.wizard.classification.landuse.model.ALSSelectedShapeFileChangedListener;
import org.kalypso.model.wspm.ui.profil.wizard.classification.landuse.model.ApplyLanduseShapeModel;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.utils.LanduseShapeLabelProvider;
import org.kalypso.shape.ShapeFile;

/**
 * @author Dirk Kuch
 */
public class ApplyLanduseShapePage extends WizardPage
{
  protected final ApplyLanduseShapeModel m_model;

  private DatabindingWizardPage m_binding;

  public ApplyLanduseShapePage( final IProject project )
  {
    super( "ApplyLanduseShapePage" ); //$NON-NLS-1$

    m_model = new ApplyLanduseShapeModel( project );
    m_model.addPropertyChangeListener( ApplyLanduseShapeModel.PROPERTY_LANDUSE_SHAPE, new ALSSelectedShapeFileChangedListener() );
    m_model.addPropertyChangeListener( ApplyLanduseShapeModel.PROPERTY_SHAPE_FILE_PROPERTY, new ALSSelectedPropertyChanged() );
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( final Composite parent )
  {
    m_binding = new DatabindingWizardPage( this, null );

    final Composite body = new Composite( parent, SWT.NULL );
    body.setLayout( new GridLayout() );

    /** select landuse shape file */
    new Label( body, SWT.NULL ).setText( "Shape File" ); //$NON-NLS-1$
    final ComboViewer type = getViewer( body, ApplyLanduseShapeModel.PROPERTY_TYPE );
    type.setInput( new String[] { IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS, IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS } );

    /** select landuse shape file */
    new Label( body, SWT.NULL ).setText( "Shape File" ); //$NON-NLS-1$
    final ComboViewer landuseShapeFile = getViewer( body, ApplyLanduseShapeModel.PROPERTY_LANDUSE_SHAPE );
    landuseShapeFile.setInput( m_model.getLanduseShapeFiles() );

    /** select shape file property */
    new Label( body, SWT.NULL ).setText( "Shape File Property" ); //$NON-NLS-1$
    final ComboViewer shapeFileProperty = getViewer( body, ApplyLanduseShapeModel.PROPERTY_SHAPE_FILE_PROPERTY );

    m_model.addPropertyChangeListener( ApplyLanduseShapeModel.PROPERTY_LANDUSE_SHAPE, new PropertyChangeListener()
    {
      @Override
      public void propertyChange( final PropertyChangeEvent evt )
      {
        final ShapeFile shapeFile = m_model.getShapeFile();
        if( Objects.isNotNull( shapeFile ) )
          shapeFileProperty.setInput( shapeFile.getFields() );
      }
    } );

    // TODO refacor table
// final LanduseMappingTable table = new LanduseMappingTable( body, this );
// table.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

// m_model.addPropertyChangeListener( ApplyLanduseShapeModel.PROPERTY_MAPPING, new PropertyChangeListener()
// {
// @Override
// public void propertyChange( final PropertyChangeEvent evt )
// {
// table.refresh();
// }
// } );

    setControl( body );
  }

  private ComboViewer getViewer( final Composite body, final String property )
  {
    final ComboViewer viewer = new ComboViewer( body, SWT.BORDER | SWT.READ_ONLY );
    viewer.getCombo().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    viewer.setLabelProvider( new LanduseShapeLabelProvider() );
    viewer.setContentProvider( new ArrayContentProvider() );

    final IObservableValue viewerSelection = ViewersObservables.observeSingleSelection( viewer );
    final IObservableValue modelValue = BeansObservables.observeValue( m_model, property );

    m_binding.bindValue( new DataBinder( viewerSelection, modelValue ) );

    return viewer;
  }

}
