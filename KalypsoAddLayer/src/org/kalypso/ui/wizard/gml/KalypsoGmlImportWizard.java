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
package org.kalypso.ui.wizard.gml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.kalypso.commons.command.ICommand;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoAddLayerPlugin;
import org.kalypso.ui.action.AddThemeCommand;
import org.kalypso.ui.addlayer.dnd.MapDropData;
import org.kalypso.ui.addlayer.internal.util.AddLayerUtils;
import org.kalypso.ui.editor.actions.FeatureActionUtilities;
import org.kalypso.ui.editor.gmleditor.part.FeatureAssociationTypeElement;
import org.kalypso.ui.i18n.Messages;
import org.kalypso.ui.wizard.AbstractDataImportWizard;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.gml.binding.commons.NamedFeatureHelper;
import org.kalypsodeegree_impl.model.feature.FeaturePath;

/**
 * @author Kuepferle
 */
public class KalypsoGmlImportWizard extends AbstractDataImportWizard
{
  private GmlFileImportPage m_page;

  private final GmlFileImportData m_data = new GmlFileImportData();

  public KalypsoGmlImportWizard( )
  {
    setWindowTitle( Messages.getString( "org.kalypso.ui.wizard.gml.KalypsoGmlImportWizard.2" ) ); //$NON-NLS-1$
  }

  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    super.init( workbench, selection );

    m_data.init( getDialogSettings() );
  }

  @Override
  public void addPages( )
  {
    final IProject project = ResourceUtilities.findProjectFromURL( getMapModel().getContext() );
    m_data.setProjectSelection( project );

    m_page = new GmlFileImportPage( "GML:importPage", Messages.getString( "org.kalypso.ui.wizard.gml.KalypsoGmlImportWizard.0" ), m_data ); //$NON-NLS-1$ //$NON-NLS-2$
    m_page.setImageDescriptor( ImageProvider.IMAGE_UTIL_UPLOAD_WIZ );


    addPage( m_page );
  }

  @Override
  public void initFromDrop( final MapDropData data )
  {
    // data./

    throw new UnsupportedOperationException();
  }

  @Override
  public boolean performCancel( )
  {
    m_data.storeSettings( getDialogSettings() );

    return super.performCancel();
  }

  @Override
  public boolean performFinish( )
  {
    m_data.storeSettings( getDialogSettings() );

    try
    {
      final ICommand[] commands = getCommands();

      for( final ICommand command : commands )
      {
        postCommand( command, null );
      }
    }
    catch( final Throwable e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      KalypsoAddLayerPlugin.getDefault().getLog().log( status );
      ErrorDialog.openError( getShell(), getWindowTitle(), Messages.getString( "org.kalypso.ui.wizard.gml.KalypsoGmlImportWizard.1" ), status ); //$NON-NLS-1$
      return false;
    }

    return true;
  }

  private ICommand[] getCommands( )
  {
    final IKalypsoLayerModell model = getMapModel();

    final IKalypsoLayerModell mapModell = getMapModel();
    final IPath mapPath = AddLayerUtils.getPathForMap( mapModell );
    final String sourcePath = m_data.getSourcePath( mapPath );

    final Object selectedElement = m_data.getSelectedElement();

    final List<String> pathList = new ArrayList<>();
    final List<String> titleList = new ArrayList<>();
    if( selectedElement instanceof Feature )
    {
      // create featurepath for element
      final Feature feature = (Feature) selectedElement;
      final FeaturePath featurepath = new FeaturePath( feature );
      final IFeatureType ft = feature.getFeatureType();
      // find title
      String title = NamedFeatureHelper.getName( feature );
      if( title == null || title.length() < 1 )
        title = ft.getAnnotation().getLabel();
      pathList.add( featurepath.toString() );
      titleList.add( title );
    }
    else if( selectedElement instanceof FeatureAssociationTypeElement )
    {
      // create featurepath for association
      final FeatureAssociationTypeElement link = (FeatureAssociationTypeElement) selectedElement;
      final Feature parent = link.getOwner();
      final FeaturePath parentFeaturePath = new FeaturePath( parent );
      final IRelationType ftp = link.getPropertyType();

      final IFeatureType associationFeatureType = ftp.getTargetFeatureType();
      final IFeatureType[] associationFeatureTypes = GMLSchemaUtilities.getSubstituts( associationFeatureType, null, false, true );

      final IAnnotation annotation = ftp.getAnnotation();
      final String linkLabel = annotation == null ? "" : annotation.getLabel(); //$NON-NLS-1$

      // TODO: this is very often not what is wanted...
      // We should show a dialog to the user and ask what we really want to add here...
      for( final IFeatureType ft : associationFeatureTypes )
      {
        final String title;
        if( ft.equals( associationFeatureType ) )
          title = linkLabel;
        else
        {
          final String ftLabel = FeatureActionUtilities.newFeatureActionLabel( ft );
          title = String.format( "%s [%s]", linkLabel, ftLabel ); //$NON-NLS-1$
        }

        final String ftpName = ftp.getQName().getLocalPart();
        final String ftName = ft.getQName().getLocalPart();
        final FeaturePath path = new FeaturePath( parentFeaturePath, ftpName + "[" + ftName + "]" ); //$NON-NLS-1$ //$NON-NLS-2$
        pathList.add( path.toString() );
        titleList.add( title );
      }
    }

    final ICommand[] result = new ICommand[pathList.size()];
    final Iterator<String> titleIterator = titleList.iterator();
    int pos = 0;
    for( final Iterator<String> pathIterator = pathList.iterator(); pathIterator.hasNext(); pos++ )
    {
      final String title = titleIterator.next();
      final String featurePath = pathIterator.next();
      result[pos] = new AddThemeCommand( model, title, "gml", featurePath, sourcePath ); //$NON-NLS-1$
    }
    return result;
  }
}