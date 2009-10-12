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
package org.kalypso.ui.editor.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.ogc.gml.command.ChangeFeaturesCommand;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.ogc.gml.command.RelativeFeatureChange;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.FeatureSelectionHelper;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypsodeegree.model.feature.Feature;

/**
 * Action delegate class for feature batch editing
 * 
 * @author Stefan Kurzbach
 */
public class FeatureBatchEditActionDelegate implements IActionDelegate
{
  private IPropertyType m_focusedProperty;

  private Feature[] m_selectedFeatures;

  private CommandableWorkspace m_workspace;

  private double m_focusedValue;

  /**
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run( final IAction action )
  {
    final Shell shell = Display.getCurrent().getActiveShell();
    final BatchEditParametersInputDialog dialog = new BatchEditParametersInputDialog( shell, action.getText(), m_focusedValue );
    if( dialog.open() == Window.CANCEL )
      return;

    final String op = dialog.getOperator();
    final FeatureChange[] changeArray = new FeatureChange[m_selectedFeatures.length];
    for( int i = 0; i < m_selectedFeatures.length; i++ )
      changeArray[i] = new RelativeFeatureChange( m_selectedFeatures[i], (IValuePropertyType) m_focusedProperty, op, dialog.getAmount() );

    final ChangeFeaturesCommand changeFeaturesCommand = new ChangeFeaturesCommand( m_workspace, changeArray );

    try
    {
      m_workspace.postCommand( changeFeaturesCommand );
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, "", e ); //$NON-NLS-1$
      ErrorDialog.openError( shell, action.getText(), changeFeaturesCommand.getDescription(), status );
    }

  }

  /**
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   *      org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged( final IAction action, final ISelection selection )
  {
    m_focusedValue = Double.NaN;

    if( selection instanceof IFeatureSelection )
    {
      final IFeatureSelection featureSelection = (IFeatureSelection) selection;
      m_focusedProperty = featureSelection.getFocusedProperty();
      addFocusDescription( action, m_focusedProperty );
      if( RelativeFeatureChange.isNumeric( m_focusedProperty ) )
      {
        action.setEnabled( true );
        m_selectedFeatures = FeatureSelectionHelper.getFeatures( featureSelection );
        final Feature focusedFeature = featureSelection.getFocusedFeature();
        m_workspace = featureSelection.getWorkspace( focusedFeature );

        if( m_focusedProperty != null && focusedFeature != null )
        {
          Object value =  focusedFeature.getProperty( m_focusedProperty );
          if( value instanceof Number )
            m_focusedValue = ((Number)value).doubleValue();
        }
      }
      else
      {
        action.setEnabled( false );
      }
    }
  }

  private void addFocusDescription( final IAction action, final IPropertyType focusedProperty )
  {
    final String text = action.getText();
    if( text != null )
    {
      final String newText;
      if( focusedProperty == null )
        newText = text;
      else
        newText = text.replaceAll( " \\(.*\\)", "" ) + " (" + focusedProperty.getAnnotation().getLabel() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      action.setText( newText );
    }
  }

}
