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

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
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
import org.kalypso.contribs.eclipse.jface.viewers.IRefreshable;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.model.ILanduseModel;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.model.LanduseModel;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.utils.ILanduseShapeDataProvider;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.utils.LanduseMappingTable;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.utils.LandusePropertyFilter;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.utils.LanduseShapeLabelProvider;

/**
 * @author Dirk Kuch
 */
public class LanduseMappingPage extends WizardPage implements IRefreshable
{
  protected final ILanduseShapeDataProvider m_provider;

  protected final LanduseModel m_model;

  private DatabindingWizardPage m_binding;

  private ComboViewer m_column;

  private String m_lnkShapeFile;

  public LanduseMappingPage( final ILanduseShapeDataProvider provider, final String type )
  {
    super( "LanduseMappingPage" ); //$NON-NLS-1$

    m_provider = provider;
    m_model = new LanduseModel( provider.getProject(), type );

    if( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS.equals( type ) )
    {
      setTitle( Messages.getString( "LanduseMappingPage.0" ) ); //$NON-NLS-1$
    }
    else if( IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS.equals( type ) )
    {
      setTitle( Messages.getString( "LanduseMappingPage.1" ) ); //$NON-NLS-1$
    }

    setDescription( Messages.getString( "LanduseMappingPage.2" ) ); //$NON-NLS-1$
  }

  @Override
  public void createControl( final Composite parent )
  {
    m_binding = new DatabindingWizardPage( this, null );

    final Composite body = new Composite( parent, SWT.NULL );
    body.setLayout( new GridLayout() );

    // shape column
    new Label( body, SWT.NULL ).setText( Messages.getString( "LanduseMappingPage.3" ) ); //$NON-NLS-1$
    m_column = getViewer( body );

    new Label( body, SWT.NULL ).setText( "" );// spacer //$NON-NLS-1$

    new Label( body, SWT.NULL ).setText( Messages.getString( "LanduseMappingPage.4" ) ); //$NON-NLS-1$
    final LanduseMappingTable table = new LanduseMappingTable( body, m_model );
    table.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    setControl( body );
  }

  private ComboViewer getViewer( final Composite body )
  {
    final ComboViewer viewer = new ComboViewer( body, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE );
    viewer.getCombo().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    viewer.setLabelProvider( new LanduseShapeLabelProvider() );
    viewer.setContentProvider( new ArrayContentProvider() );
    viewer.addFilter( new LandusePropertyFilter() );

    final IObservableValue viewerSelection = ViewersObservables.observeSingleSelection( viewer );
    final IObservableValue modelValue = BeansObservables.observeValue( m_model, ILanduseModel.PROPERTY_SHAPE_COLUMN );

    m_binding.bindValue( new DataBinder( viewerSelection, modelValue ) );

    return viewer;
  }

  @Override
  public void refresh( )
  {
    try
    {
      final String lnkShapeFile = m_provider.getLnkShapeFile();
      if( Objects.notEqual( m_lnkShapeFile, lnkShapeFile ) )
      {
        m_model.updateShapeFile( lnkShapeFile );
        m_column.setInput( m_model.getShapeFile().getFields() );

        m_lnkShapeFile = lnkShapeFile;
      }
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();
    }
  }

  public ILanduseModel getModel( )
  {
    return m_model;
  }
}
