/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.zml.ui.table.view;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.services.IServiceLocator;
import org.kalypso.contribs.eclipse.jface.wizard.IUpdateable;
import org.kalypso.contribs.eclipse.swt.layout.Layouts;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.event.IObservationListener;
import org.kalypso.ogc.sensor.event.ObservationChangeType;
import org.kalypso.zml.core.base.IMultipleZmlSourceElement;
import org.kalypso.zml.core.table.ZmlTableConfigurationLoader;
import org.kalypso.zml.core.table.model.IZmlColumnModelListener;
import org.kalypso.zml.core.table.model.ZmlModel;
import org.kalypso.zml.core.table.model.event.ZmlModelColumnChangeType;
import org.kalypso.zml.core.table.schema.ZmlTableType;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableComposite;
import org.kalypso.zml.ui.table.ZmlTableComposite;
import org.kalypso.zml.ui.table.update.ZmlTableUpdater;

/**
 * @author Dirk Kuch
 */
public class TableComposite extends Composite implements IUpdateable, IObservationListener
{
  private final IServiceLocator m_context;

  private ZmlModel m_model;

  protected ZmlTableComposite m_table;

  public TableComposite( final Composite parent, final FormToolkit toolkit, final IServiceLocator context )
  {
    super( parent, SWT.BORDER );

    m_context = context;

    final GridLayout layout = Layouts.createGridLayout();
    layout.verticalSpacing = 0;
    setLayout( layout );

    init();

    draw( toolkit );
  }

  private void init( )
  {
    try
    {

      final URL template = getClass().getResource( "templates/base.kot" );
      final ZmlTableConfigurationLoader loader = new ZmlTableConfigurationLoader( template );
      final ZmlTableType tableType = loader.getTableType();

      m_model = new ZmlModel( tableType, template );
    }
    catch( final Throwable t )
    {
      t.printStackTrace();
    }

  }

  private void draw( final FormToolkit toolkit )
  {

    m_table = new ZmlTableComposite( this, toolkit );
    m_table.doInitialize( m_model );

    m_table.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
    final IZmlColumnModelListener listener = new IZmlColumnModelListener()
    {

      @Override
      public void modelChanged( final ZmlModelColumnChangeType type )
      {
        m_table.getTable().refresh( type );
      }
    };

    m_model.addListener( listener );

  }

  public void setSelection( final IMultipleZmlSourceElement[] selection )
  {
    final ZmlTableUpdater updater = new ZmlTableUpdater( m_table.getTable(), selection );
    updater.setSingleSelectionMode( true );
    updater.run();
  }

  @Override
  public final void dispose( )
  {
    m_table.dispose();
    m_model.dispose();

    super.dispose();
  }

  @Override
  public void observationChanged( final IObservation obs, final Object source, final ObservationChangeType type )
  {
    // TODO
// final UIJob job = new UIJob( "Observation changed - invalidating diagram" )
// {
// @Override
// public IStatus runInUIThread( final IProgressMonitor monitor )
// {
// m_chartComposite.invalidate();
//
// return Status.OK_STATUS;
// }
// };
//
// job.setUser( false );
// job.setSystem( true );
//
// job.schedule();
  }

  public IZmlTable getTable( )
  {
    return m_table.getTable();
  }

  public IZmlTableComposite getTableComposite( )
  {
    return m_table;
  }
}
