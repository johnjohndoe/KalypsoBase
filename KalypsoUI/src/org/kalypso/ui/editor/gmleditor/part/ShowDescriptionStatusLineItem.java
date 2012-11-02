/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.ui.editor.gmleditor.part;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IEditorPart;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.ogc.gml.selection.FeatureSelectionHelper;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * A status line item, which shows the description of the currently selected feature.
 * 
 * @author Gernot Belger
 */
public class ShowDescriptionStatusLineItem extends StatusLineContributionItem
{
  private final GMLLabelProvider m_labelProvider = new GMLLabelProvider();

  private final ISelectionChangedListener m_listener = new ISelectionChangedListener()
  {
    @Override
    public void selectionChanged( final SelectionChangedEvent event )
    {
      final ISelection selection = event.getSelection();
      handleSelectionChanged( selection );
    }
  };

  private IEditorPart m_targetEditor;

  public ShowDescriptionStatusLineItem( final String id )
  {
    super( id );
  }

  public ShowDescriptionStatusLineItem( final String id, final int charWidth )
  {
    super( id, charWidth );
  }

  @Override
  public void dispose( )
  {
    unhookEditor();

    m_labelProvider.dispose();

    super.dispose();
  }

  public void setActiveEditor( final IEditorPart targetEditor )
  {
    unhookEditor();

    m_targetEditor = targetEditor;

    hookEditor();
  }

  private void hookEditor( )
  {
    if( m_targetEditor != null )
    {
      final ISelectionProvider selectionProvider = m_targetEditor.getSite().getSelectionProvider();
      if( selectionProvider != null )
        selectionProvider.addSelectionChangedListener( m_listener );
    }
  }

  private void unhookEditor( )
  {
    if( m_targetEditor != null )
    {
      final ISelectionProvider selectionProvider = m_targetEditor.getSite().getSelectionProvider();
      if( selectionProvider != null )
        selectionProvider.removeSelectionChangedListener( m_listener );
    }
  }

  protected void handleSelectionChanged( final ISelection selection )
  {
    final String statusMessage = getStatusMessage( selection );

    setText( statusMessage );
  }

  private String getStatusMessage( final ISelection selection )
  {
    if( selection instanceof IStructuredSelection )
    {
      final int size = ((IStructuredSelection)selection).size();
      if( size > 1 )
        return String.format( Messages.getString( "ShowDescriptionStatusLineItem_0" ), size ); //$NON-NLS-1$
    }

    if( selection instanceof IFeatureSelection )
    {
      final Feature feature = FeatureSelectionHelper.getFirstFeature( (IFeatureSelection)selection );
      if( feature != null )
      {
        final String description = FeatureHelper.getAnnotationValue( feature, IAnnotation.ANNO_DESCRIPTION );
        if( !StringUtils.isBlank( description ) )
          return description;
      }
    }

    if( selection instanceof IStructuredSelection )
    {
      final Object firstElement = ((IStructuredSelection)selection).getFirstElement();
      if( firstElement != null )
        return m_labelProvider.getText( firstElement );
    }

    return StringUtils.EMPTY;
  }
}