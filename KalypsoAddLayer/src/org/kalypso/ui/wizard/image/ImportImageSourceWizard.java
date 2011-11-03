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
package org.kalypso.ui.wizard.image;

import java.net.URL;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.action.AddThemeCommand;
import org.kalypso.ui.i18n.Messages;
import org.kalypso.ui.wizard.IKalypsoDataImportWizard;

/**
 * ImportImageSourceWizard
 * <p>
 * created by
 * 
 * @author kuepfer (21.05.2005)
 */
public class ImportImageSourceWizard extends Wizard implements IKalypsoDataImportWizard
{
  public static final String ID = "org.kalypso.ui.wizard.image"; //$NON-NLS-1$

  private ICommandTarget m_outlineviewer;

  private IKalypsoLayerModell m_mapModel;

  private ImportImageWizardPage m_page;

  /**
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    if( m_mapModel != null )
    {
      final URL context = m_mapModel.getContext();

      final String name = m_page.getSourcePath().removeFileExtension().lastSegment();
      final String type = m_page.getFileType();
      final String source = m_page.getSource( context );
      final AddThemeCommand command = new AddThemeCommand( m_mapModel, name, type, null, source
          + "#" + m_page.getCSName() ); //$NON-NLS-1$
      m_outlineviewer.postCommand( command, null );
    }
    return true;
  }

  /**
   * @see org.kalypso.ui.wizard.data.IKalypsoDataImportWizard#setOutlineViewer(org.kalypso.ogc.gml.outline.GisMapOutlineViewer)
   */
  @Override
  public void setCommandTarget( final ICommandTarget commandTarget )
  {
    m_outlineviewer = commandTarget;
  }

  /**
   * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
   *      org.eclipse.jface.viewers.IStructuredSelection)
   */
  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
  }

  @Override
  public void addPages( )
  {
    m_page = new ImportImageWizardPage( "imageimport", Messages.getString( "org.kalypso.ui.wizard.image.ImportImageSourceWizard.3" ), ImageProvider.IMAGE_KALYPSO_ICON_BIG ); //$NON-NLS-1$ //$NON-NLS-2$
    if( m_outlineviewer != null )
    {
      m_page.setProjectSelection( m_mapModel.getProject() );
    }
    addPage( m_page );
  }

  /**
   * @see org.kalypso.ui.wizard.IKalypsoDataImportWizard#setMapModel(org.kalypso.ogc.gml.mapmodel.IMapModell)
   */
  @Override
  public void setMapModel( final IKalypsoLayerModell modell )
  {
    m_mapModel = modell;
  }
}