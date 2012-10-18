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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.kalypso.commons.databinding.DataBinder;
import org.kalypso.commons.databinding.jface.wizard.DatabindingWizardPage;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.profil.filter.ProfilePointFilterComposite;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.profil.wizard.classification.landuse.model.ApplyLanduseShapeModel;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.model.ILanduseModel;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.utils.LanduseMappingTable;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.utils.LanduseShapeLabelProvider;

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

    setTitle( Messages.getString( "ApplyLanduseShapePage_0" ) ); //$NON-NLS-1$
    setMessage( Messages.getString( "ApplyLanduseShapePage_1" ) ); //$NON-NLS-1$
  }

  @Override
  public void createControl( final Composite parent )
  {
    m_binding = new DatabindingWizardPage( this, null );

    final Composite body = new Composite( parent, SWT.NULL );
    body.setLayout( new GridLayout() );

    /** select landuse shape file */
    new Label( body, SWT.NULL ).setText( Messages.getString( "ApplyLanduseShapePage_2" ) ); //$NON-NLS-1$
    final ComboViewer type = getViewer( body, ILanduseModel.PROPERTY_TYPE );
    type.setInput( new String[] { IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS, IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS } );
    type.setLabelProvider( new LabelProvider()
    {
      @Override
      public String getText( final Object element )
      {
        if( StringUtils.equals( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS, (String)element ) )
        {
          return Messages.getString( "ApplyLanduseShapePage.0" ); //$NON-NLS-1$
        }
        else if( StringUtils.equals( IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS, (String)element ) )
        {
          return Messages.getString( "ApplyLanduseShapePage.1" ); //$NON-NLS-1$
        }
        return super.getText( element );
      }
    } );

    /** select landuse shape file */
    new Label( body, SWT.NULL ).setText( Messages.getString( "ApplyLanduseShapePage_3" ) ); //$NON-NLS-1$
    final ComboViewer landuseShapeFile = getViewer( body, ILanduseModel.PROPERTY_LANDUSE_SHAPE );
    landuseShapeFile.setInput( m_model.getLanduseShapeFiles() );

    /** select shape file property */
    // TODO: makes no sense here, changing the property does not work
    //new Label( body, SWT.NULL ).setText( Messages.getString( "ApplyLanduseShapePage_4" ) ); //$NON-NLS-1$
    // final ComboViewer properties = getViewer( body, ILanduseModel.PROPERTY_SHAPE_COLUMN );
    // properties.addFilter( new LandusePropertyFilter() );

    // m_model.addPropertyChangeListener( ILanduseModel.PROPERTY_LANDUSE_SHAPE, new ALSShapeFilePropertyFiller( properties ) );

    final LanduseMappingTable table = new LanduseMappingTable( body, m_model );
    table.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /** profile point filter */
    final Group group = new Group( body, SWT.NONE );
    group.setLayout( new FillLayout() );
    group.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    group.setText( ProfilePointFilterComposite.STR_GROUP_TEXT );

    m_model.getFilter().createControl( group, SWT.BORDER );

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

  public ILanduseModel getModel( )
  {
    return m_model;
  }
}