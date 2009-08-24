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
package org.kalypso.repository.proxy.preferences.page;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.repository.KalypsoRepositoryPlugin;
import org.kalypso.repository.proxy.preferences.IKalypsoRepositoryPreferences;

/**
 * @author Dirk Kuch
 */
public class KalypsoRepositoryPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{

  private SelectRepositoryComposite m_selectRepositoryComposite;

  /**
   * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createContents( final Composite parent )
  {
    /* Get the preference store. */
    final IPreferenceStore store = KalypsoRepositoryPlugin.getDefault().getPreferenceStore();

    /* Create the main container. */
    final Composite main = new Composite( parent, SWT.NONE );
    main.setLayout( new GridLayout() );

    try
    {
      m_selectRepositoryComposite = new SelectRepositoryComposite( store, main );
      m_selectRepositoryComposite.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
    }
    catch( final CoreException e )
    {
      new Label( main, SWT.MULTI ).setText( e.getMessage() );

      KalypsoRepositoryPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    return main;
  }

  /**
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  @Override
  public void init( final IWorkbench workbench )
  {

  }

  /**
   * @see org.eclipse.jface.preference.PreferencePage#performApply()
   */
  @Override
  protected void performApply( )
  {
    /* Get the preference store. */
    final IPreferenceStore store = KalypsoRepositoryPlugin.getDefault().getPreferenceStore();

    /* Set the new values. */
    final String connector = m_selectRepositoryComposite.getConnector();
    store.setValue( IKalypsoRepositoryPreferences.CONNECTOR, connector );

    /* Save the plugin preferences. */
    KalypsoRepositoryPlugin.getDefault().savePluginPreferences();
  }
}
