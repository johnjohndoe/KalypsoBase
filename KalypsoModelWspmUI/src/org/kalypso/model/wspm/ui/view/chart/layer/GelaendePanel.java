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
package org.kalypso.model.wspm.ui.view.chart.layer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.changes.ProfileChangeHint;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.AbstractProfilView;
import org.kalypso.model.wspm.ui.view.chart.IProfilChartLayer;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;

/**
 * @author gernot
 * @author kimwerner
 */
public class GelaendePanel extends AbstractProfilView
{
  protected StyledText m_comment;

  private final IChartLayer m_layer;

  public GelaendePanel( final IProfile profile, final IChartLayer layer )
  {
    super( profile );
    m_layer = layer;
  }

  @Override
  protected Control doCreateControl( final Composite parent, final FormToolkit toolkit )
  {
    final Composite panel = toolkit.createComposite( parent );
    panel.setLayout( new GridLayout() );

    final Group cg = new Group( panel, SWT.None );
    cg.setText( Messages.getString( "org.kalypso.model.wspm.tuhh.ui.panel.GelaendePanel.3" ) ); //$NON-NLS-1$
    final GridData cgData = new GridData( SWT.FILL, SWT.FILL, true, true );

    cg.setLayoutData( cgData );
    cg.setLayout( new GridLayout() );

    toolkit.adapt( cg );

    createComment( cg, toolkit );

    return panel;
  }

  private void createComment( final Group cg, final FormToolkit toolkit )
  {
    final HyperlinkStyledText hyperlinkStyledText = new HyperlinkStyledText( getProfile().getComment() );
    m_comment = hyperlinkStyledText.createControl( cg, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL );
    m_comment.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    toolkit.adapt( m_comment, true, true );

    m_comment.addFocusListener( new FocusAdapter()
    {
      @Override
      public void focusLost( final FocusEvent e )
      {
        final String comment = m_comment.getText();
        if( comment != null && !comment.equals( getProfile().getComment() ) )
        {
          /*
           * we need both methods to stay synchronized with featureView
           */
          getProfile().setComment( m_comment.getText() );

          // FIXME: check -> cannot work any more...
          //final ProfilOperation operation = new ProfilOperation( "", getProfile(), new ProfilPropertyEdit( getProfile(), IWspmConstants.PROFIL_PROPERTY_COMMENT, m_comment.getText() ), true ); //$NON-NLS-1$
          // new ProfilOperationJob( operation ).schedule();
        }
      }
    } );
  }

  protected final void setLayerData( final boolean horz, final boolean vert )
  {
    Integer data = 0;
    if( horz )
    {
      data = data + 1;
    }
    if( vert )
    {
      data = data + 2;
    }
    int old = 0;

    try
    {
      final Object o = m_layer.getData( IProfilChartLayer.VIEW_DATA_KEY );
      old = o == null ? 0 : Integer.valueOf( o.toString() );
    }
    catch( final NumberFormatException e )
    {
      old = 0;
    }

    if( old != data )
    {
      m_layer.setData( IProfilChartLayer.VIEW_DATA_KEY, data.toString() );
    }
  }

  @Override
  public void onProfilChanged( final ProfileChangeHint hint )
  {
    if( hint.isProfilPropertyChanged() )
    {
      final Control control = getControl();
      if( control != null && !control.isDisposed() )
      {
        control.getDisplay().asyncExec( new Runnable()
        {
          @Override
          public void run( )
          {
            m_comment.setText( getProfile().getComment() );
          }
        } );
      }
    }
  }
}
