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
package org.kalypso.ogc.sensor.view.wizard;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.ui.KalypsoGisPlugin;

/**
 * Wizard for exporting a Repository ZML as a file in the local filesystem
 * 
 * @author schlienger
 */
public class ExportAsFileWizard extends Wizard
{
  private DateRangeInputWizardPage m_page1;

  private FileSelectWizardPage m_page2;

  private final IObservation m_obs;

  public ExportAsFileWizard( final IObservation obs )
  {
    m_obs = obs;

    final IDialogSettings section = DialogSettingsUtils.getDialogSettings( KalypsoGisPlugin.getDefault(), "ExportAsFileWizard" ); //$NON-NLS-1$ 
    setDialogSettings( section );

    setWindowTitle( Messages.getString( "org.kalypso.ogc.sensor.view.wizard.ExportAsFileWizard.5" ) ); //$NON-NLS-1$
    setNeedsProgressMonitor( true );
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#addPages()
   */
  @Override
  public void addPages()
  {
    super.addPages();

    final String lastDirPath = getDialogSettings().get( "lastDir" );
    File file;
    if( lastDirPath == null )
      file = new File( m_obs.getName() );
    else
      file = new File( lastDirPath, m_obs.getName() );

    final String fileName = file.getAbsolutePath();

    m_page1 = new DateRangeInputWizardPage();
    m_page2 = new FileSelectWizardPage( "fileselect", fileName, new String[] //$NON-NLS-1$
                                                                           { "*.zml", "*.xml" } ); //$NON-NLS-1$ //$NON-NLS-2$

    addPage( m_page1 );
    addPage( m_page2 );
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish()
  {
    final DateRange dateRange = m_page1.getDateRange();
    final String filePath = m_page2.getFilePath();

    try
    {
      final File file = new File( filePath );

      getDialogSettings().put( "lastDir", file.getParent() );

      final ObservationRequest request = new ObservationRequest( dateRange );
      ZmlFactory.writeToFile( m_obs, file, request );
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      MessageDialog.openError( getShell(), Messages.getString("org.kalypso.ogc.sensor.view.wizard.ExportAsFileWizard.6"), e.getLocalizedMessage() ); //$NON-NLS-1$

      return false;
    }

    return true;
  }
}
