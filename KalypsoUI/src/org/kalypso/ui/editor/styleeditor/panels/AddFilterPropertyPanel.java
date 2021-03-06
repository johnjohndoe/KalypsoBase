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
/*
 * Created on 15.07.2004
 *  
 */
package org.kalypso.ui.editor.styleeditor.panels;

import javax.swing.event.EventListenerList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.editor.styleeditor.MessageBundle;

/**
 * @author F.Lindemann
 */
public class AddFilterPropertyPanel
{
  private Composite composite = null;

  private Combo geometryCombo = null;

  private int selectionIndex = 0;

  private final EventListenerList listenerList = new EventListenerList();

  private String label = null;

  private String[] listCombi = null;

  public AddFilterPropertyPanel( final Composite parent, final String m_label, final String[] list )
  {
    setLabel( m_label );
    setListCombi( list );
    composite = new Composite( parent, SWT.NULL );
    final FormLayout compositeLayout = new FormLayout();
    final GridData compositeData = new GridData();
    compositeData.widthHint = 230;
    composite.setLayoutData( compositeData );
    composite.setLayout( compositeLayout );
    compositeLayout.marginWidth = 0;
    compositeLayout.marginHeight = 0;
    compositeLayout.spacing = 0;
    composite.layout();
    init();
  }

  public void addPanelListener( final PanelListener pl )
  {
    listenerList.add( PanelListener.class, pl );
  }

  private void init( )
  {
    // Geometry-Selection Combo
    geometryCombo = new Combo( composite, SWT.NULL );
    final FormData geometryComboData = new FormData();
    geometryComboData.height = 21;
    geometryComboData.width = 90;
    geometryComboData.left = new FormAttachment( 295, 1000, 0 );
    geometryComboData.top = new FormAttachment( 100, 1000, 0 );
    geometryCombo.setLayoutData( geometryComboData );

    if( getListCombi() != null && getListCombi().length > 0 )
    {
      geometryCombo.setItems( getListCombi() );
      geometryCombo.select( 0 );
    }

    // Symbolizer Add-Button
    final Label symbolizerAddButton = new Label( composite, SWT.PUSH | SWT.CENTER );
    symbolizerAddButton.setImage( ImageProvider.IMAGE_STYLEEDITOR_GET_SCALE.createImage() );
    final FormData symbolizerAddButtonData = new FormData();
    symbolizerAddButtonData.height = 20;
    symbolizerAddButtonData.width = 30;
    symbolizerAddButtonData.left = new FormAttachment( 860, 1000, 0 );
    symbolizerAddButtonData.top = new FormAttachment( 100, 1000, 0 );
    symbolizerAddButton.setLayoutData( symbolizerAddButtonData );
    symbolizerAddButton.setToolTipText( MessageBundle.STYLE_EDITOR_SET );
    symbolizerAddButton.addMouseListener( new MouseListener()
    {
      @Override
      public void mouseDoubleClick( final MouseEvent e )
      {
        setSelection( getGeometryCombo().getSelectionIndex() );
        fire();
      }

      @Override
      public void mouseDown( final MouseEvent e )
      {
        mouseDoubleClick( e );
      }

      @Override
      public void mouseUp( final MouseEvent e )
      {
        // nothing
      }

    } );

    // ***** Label
    final Label symbolizerLabel = new Label( composite, SWT.NULL );
    final FormData symbolizerLabelData = new FormData();
    symbolizerLabelData.height = 15;
    symbolizerLabelData.width = 242;
    symbolizerLabelData.left = new FormAttachment( 0, 1000, 0 );
    symbolizerLabelData.top = new FormAttachment( 100, 1000, 0 );
    symbolizerLabel.setLayoutData( symbolizerLabelData );
    symbolizerLabel.setText( label );
  }

  public String getSelection( )
  {
    return getListCombi()[selectionIndex];
  }

  public void setSelection( final int index )
  {
    selectionIndex = index;
  }

  public void setSelection( final String selectionString )
  {
    if( getListCombi() == null )
      return;
    for( int i = 0; i < getListCombi().length; i++ )
    {
      if( getListCombi()[i].equals( selectionString ) )
      {
        setSelection( i );
        geometryCombo.select( i );
      }
    }
  }

  protected void fire( )
  {
    final Object[] listeners = listenerList.getListenerList();
    for( int i = listeners.length - 2; i >= 0; i -= 2 )
    {
      if( listeners[i] == PanelListener.class )
      {
        final PanelEvent event = new PanelEvent( this );
        ((PanelListener)listeners[i + 1]).valueChanged( event );
      }
    }
  }

  public Combo getGeometryCombo( )
  {
    return geometryCombo;
  }

  public void setGeometryCombo( final Combo m_geometryCombo )
  {
    geometryCombo = m_geometryCombo;
  }

  public String getLabel( )
  {
    return label;
  }

  public void setLabel( final String m_label )
  {
    label = m_label;
  }

  public int getSelectionIndex( )
  {
    return selectionIndex;
  }

  public void setSelectionIndex( final int m_selectionIndex )
  {
    selectionIndex = m_selectionIndex;
  }

  public String[] getListCombi( )
  {
    return listCombi;
  }

  public void setListCombi( final String[] m_listCombi )
  {
    listCombi = m_listCombi;
  }
}