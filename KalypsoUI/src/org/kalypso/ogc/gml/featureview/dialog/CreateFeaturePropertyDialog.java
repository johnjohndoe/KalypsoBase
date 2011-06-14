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
package org.kalypso.ogc.gml.featureview.dialog;

import java.util.Collection;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.jface.viewers.ArrayTreeContentProvider;
import org.kalypso.contribs.eclipse.ui.dialogs.TreeSingleSelectionDialog;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.filterdialog.model.FeatureTypeLabelProvider;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * A dialog which creates a feature property (i.e. it creates the inline property or allows the user to choose an
 * existing feature).
 * 
 * @author Gernot Belger
 */
public class CreateFeaturePropertyDialog implements IFeatureDialog
{
  private FeatureChange m_change = null;

  private final IFeatureChangeListener m_listener;

  private final Feature m_feature;

  private final IRelationType m_relationType;

  public CreateFeaturePropertyDialog( final IFeatureChangeListener listener, final Feature feature, final IRelationType relationType )
  {
    m_listener = listener;
    m_feature = feature;
    m_relationType = relationType;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.dialog.IFeatureDialog#open(org.eclipse.swt.widgets.Shell)
   */
  @Override
  public int open( final Shell shell )
  {
    final GMLWorkspace workspace = m_feature.getWorkspace();

    if( m_relationType.isInlineAble() && !m_relationType.isLinkAble() )
    {
      final IFeatureType targetFeatureType = m_relationType.getTargetFeatureType();
      final IFeatureType[] substituts = GMLSchemaUtilities.getSubstituts( targetFeatureType, workspace.getGMLSchema(), false, true );


      final IFeatureType newFeatureType = chooseFeatureType( shell, substituts );
      if( newFeatureType == null )
        return Window.CANCEL;

      final Feature newFeature = workspace.createFeature( m_feature, m_relationType, newFeatureType );
      m_change = new FeatureChange( m_feature, m_relationType, newFeature );

      m_listener.openFeatureRequested( null, null ); // in order to force total refresh of feature view
      m_listener.openFeatureRequested( m_feature, m_relationType );

      return Window.OK;
    }

    // TODO: the other cases (choosing a feature) are not supported yet
    final String userMessage = Messages.getString( "org.kalypso.ogc.gml.featureview.dialog.NotImplementedFeatureDialog.implemented" ); //$NON-NLS-1$
    MessageDialog.openInformation( shell, Messages.getString( "org.kalypso.ogc.gml.featureview.dialog.NotImplementedFeatureDialog.edit" ), userMessage ); //$NON-NLS-1$

    return Window.CANCEL;
  }

  private IFeatureType chooseFeatureType( final Shell shell, final IFeatureType[] substituts )
  {
    if( substituts.length == 0 )
    {
      // May only happen if the type is abstract
      final String message = String.format( "No implementation(s) of type '%s' available.", getNewLabel() );
      MessageDialog.openWarning( shell, "New Feature", message );
      return null;
    }

    if( substituts.length == 1 )
      return substituts[0];

    /* Let user choose */
    final String message = "Please choose the type of the new feature:";

    final ILabelProvider labelProvider = new FeatureTypeLabelProvider( IAnnotation.ANNO_NAME );
    final TreeSingleSelectionDialog dialog = new TreeSingleSelectionDialog( shell, substituts, new ArrayTreeContentProvider(), labelProvider, message );
// ListSelectionDialog dialog = new ListSelectionDialog( shell, substituts, new ArrayContentProvider(), labelProvider,
// message );

    if( dialog.open() == Window.CANCEL )
      return null;

    return (IFeatureType) dialog.getResult()[0];
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.dialog.IFeatureDialog#collectChanges(java.util.Collection)
   */
  @Override
  public void collectChanges( final Collection<FeatureChange> c )
  {
    if( c != null && m_change != null )
      c.add( m_change );
  }

  @Override
  public String getLabel( )
  {
    final String label = getNewLabel();

    if( m_relationType.isInlineAble() && m_relationType.isLinkAble() )
      return Messages.getString( "org.kalypso.ogc.gml.featureview.dialog.CreateFeaturePropertyDialog.3", label ); //$NON-NLS-1$

    if( m_relationType.isInlineAble() )
      return Messages.getString( "org.kalypso.ogc.gml.featureview.dialog.CreateFeaturePropertyDialog.2", label ); //$NON-NLS-1$

    if( m_relationType.isLinkAble() )
      return Messages.getString( "org.kalypso.ogc.gml.featureview.dialog.CreateFeaturePropertyDialog.4", label ); //$NON-NLS-1$

    return label;
  }

  protected String getNewLabel( )
  {
    final String label = m_relationType.getTargetFeatureType().getAnnotation().getValue( IAnnotation.ANNO_NAME );
    return label;
  }

}
