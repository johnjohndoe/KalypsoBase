/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.simulation.ui.startscreen;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.swt.graphics.FontUtilities;
import org.kalypso.simulation.ui.KalypsoSimulationUIPlugin;

/**
 * @author belger
 */
public class PrognoseView extends ViewPart
{
  private PrognosePanel m_panel = null;

  private final FontUtilities m_fontUtils = new FontUtilities();

  private static final String CONFIG_MODELLIST = "kalypso.hwv.modellist.url";

  @Override
  public void dispose( )
  {
    if( m_panel != null )
      m_panel.dispose();
    m_fontUtils.dispose();
  }

  /**
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl( final Composite parent )
  {
    final String modellistProp = System.getProperty( CONFIG_MODELLIST, null );
    if( modellistProp == null )
    {
      // TODO: show in a status-component
      final IStatus status = StatusUtilities.createWarningStatus( "URL der Modellliste nicht konfiguriert. Setzen Sie die Eigenschaft '" + CONFIG_MODELLIST
          + "' in der config.ini, um die Modellliste zu konfigurieren." );
      KalypsoSimulationUIPlugin.getDefault().getLog().log( status );
    }
    else
    {
      try
      {
        final URL configUrl = Platform.getConfigurationLocation().getURL();
        final URL modellistUrl = new URL( configUrl, modellistProp );
        m_panel = new PrognosePanel( modellistUrl );
      }
      catch( final MalformedURLException e )
      {
        final IStatus errorStatus = StatusUtilities.statusFromThrowable( e );
        KalypsoSimulationUIPlugin.getDefault().getLog().log( errorStatus );
      }
    }

    final Display display = parent.getDisplay();
    final FormToolkit toolkit = new FormToolkit( display );
    final Form form = toolkit.createForm( parent );
    form.setBackground( display.getSystemColor( SWT.COLOR_WHITE ) );

    final GridLayout gridLayout = new GridLayout( 1, false );
    // gridLayout.horizontalSpacing = 20;
    // gridLayout.verticalSpacing = 20;
    // gridLayout.marginHeight = 20;
    // gridLayout.marginWidth = 20;
    form.getBody().setLayout( gridLayout );
    form.getBody().setBackground( display.getSystemColor( SWT.COLOR_WHITE ) );

    final GridData formGridData = new GridData( GridData.FILL_BOTH );
    formGridData.horizontalAlignment = GridData.CENTER;
    form.setLayoutData( formGridData );

    if( m_panel == null )
    {
      // TODO: show in status-component
      final Label label = toolkit.createLabel( form.getBody(), "Es konnte kein Kontakt zum Server hergestellt werden.\n" + "Hochwasser-Vorhersage nicht möglich.\n" );

      label.setBackground( display.getSystemColor( SWT.COLOR_WHITE ) );

      final GridData labelData = new GridData( GridData.FILL_BOTH );
      labelData.horizontalAlignment = GridData.CENTER;
      label.setLayoutData( labelData );

      final Font headingFont = m_fontUtils.createChangedFontData( label.getFont().getFontData(), 8, SWT.NONE, label.getDisplay() );
      label.setFont( headingFont );
    }
    else
    {
      final Composite panelControl = m_panel.createControl( form.getBody(), getSite().getWorkbenchWindow() );
      final GridData gridData = new GridData( GridData.FILL_BOTH );
      panelControl.setLayoutData( gridData );

      panelControl.setBackground( display.getSystemColor( SWT.COLOR_WHITE ) );
    }

    form.layout();
  }

  /**
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  @Override
  public void setFocus( )
  {
    // mir doch egal
  }
}