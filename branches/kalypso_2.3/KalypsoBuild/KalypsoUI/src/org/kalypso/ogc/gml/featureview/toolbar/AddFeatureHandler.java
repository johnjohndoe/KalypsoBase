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
package org.kalypso.ogc.gml.featureview.toolbar;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.featureview.control.TableFeatureControl;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ui.editor.gmleditor.util.command.AddFeatureCommand;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Dirk Kuch
 */
public class AddFeatureHandler extends AbstractTableFeatureControlHandler
{
  public static final String ID = "org.kalypso.ogc.gml.featureview.toolbar.AddFeatureHandler";

  /**
   * This function checks, if more features can be added.
   * 
   * @return True, if so.
   */
  private boolean checkMaxCount( final Feature parentFeature, final IRelationType parentRelation )
  {
    int maxOccurs = -1;
    int size = -1;

    maxOccurs = parentRelation.getMaxOccurs();
    if( parentFeature instanceof List< ? > )
    {
      size = ((List< ? >) parentFeature).size();
      if( maxOccurs == IPropertyType.UNBOUND_OCCURENCY )
        return true;
      else if( maxOccurs < size )
        return false;
    }

    return true;
  }

  /**
   * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final TableFeatureControl control = getFeatureControl( event );

    final Feature parentFeature = control.getFeature();
    final CommandableWorkspace workspace = control.getWorkspace();
    final IRelationType parentRelation = control.getFeatureTypeProperty();

    /* Get the needed properties. */
    if( !checkMaxCount( parentFeature, parentRelation ) )
    {
      final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
      MessageDialog.openInformation( shell, Messages.getString( "org.kalypso.ogc.gml.featureview.control.TableFeatureContol.2" ), Messages.getString( "org.kalypso.ogc.gml.featureview.control.TableFeatureContol.3" ) ); //$NON-NLS-1$ //$NON-NLS-2$

      return Status.CANCEL_STATUS;
    }

    final AddFeatureCommand command = new AddFeatureCommand( workspace, parentRelation.getTargetFeatureType(), parentFeature, parentRelation, -1, null, null, 0 );
    control.execute( command );

    return Status.OK_STATUS;
  }

}
