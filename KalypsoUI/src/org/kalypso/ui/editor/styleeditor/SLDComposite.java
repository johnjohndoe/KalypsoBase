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
package org.kalypso.ui.editor.styleeditor;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.contribs.eclipse.swt.widgets.ControlUtils;
import org.kalypso.contribs.eclipse.ui.forms.ToolkitUtils;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypso.ogc.gml.IKalypsoStyleListener;
import org.kalypso.ui.editor.styleeditor.style.FeatureTypeStyleComposite;
import org.kalypso.ui.editor.styleeditor.style.FeatureTypeStyleInput;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * @author F.Lindemann
 */
public class SLDComposite extends Composite
{
  final Runnable m_styleChangedRunnable = new Runnable()
  {
    @Override
    public void run( )
    {
      updateControl();
    }
  };

  private final ResetUserStyleAction m_resetAction = new ResetUserStyleAction( this );

  private final SaveStyleAction m_saveAction = new SaveStyleAction( this );

  private final IKalypsoStyleListener m_styleListener = new IKalypsoStyleListener()
  {
    @Override
    public void styleChanged( )
    {
      ControlUtils.exec( SLDComposite.this, m_styleChangedRunnable );
    }
  };

  private FeatureTypeStyleInput m_input;

  private final Form m_form;

  private final FormToolkit m_toolkit;

  private FeatureTypeStyleComposite m_styleComposite;

// private IKalypsoStyle m_style;

  public SLDComposite( final Composite parent )
  {
    super( parent, SWT.NONE );

    setLayout( new FillLayout() );

    m_toolkit = ToolkitUtils.createToolkit( this );

    m_form = m_toolkit.createForm( this );
    m_form.setText( Messages.getString( "org.kalypso.ui.editor.styleeditor.SLDEditorGuiBuilder.1" ) ); //$NON-NLS-1$

    final Composite body = m_form.getBody();
    body.setLayout( new FillLayout() );

    final IToolBarManager toolBarManager = m_form.getToolBarManager();
    toolBarManager.add( m_resetAction );
    toolBarManager.add( m_saveAction );
    toolBarManager.update( true );

    setInput( null );
  }

  public IFeatureType getFeatureType( )
  {
    if( m_input == null )
      return null;

    return m_input.getFeatureType();
  }

  public int getSelectedStyle( )
  {
    if( m_styleComposite == null )
      return -1;

    return m_styleComposite.getSelectedStyle();
  }

  // final FeatureTypeStyleInput input = createInput( style, featureType, styleToSelect, config );

  public void setInput( final FeatureTypeStyleInput input )
  {
    if( ObjectUtils.equals( input, m_input ) )
    {
      if( !m_form.isDisposed() )
        updateControl();
      return;
    }

    if( m_input != null )
    {
      final IKalypsoStyle oldStyle = m_input.getStyle();
      if( oldStyle != null )
        oldStyle.removeStyleListener( m_styleListener );
    }

    m_input = input;

    if( m_input != null )
    {
      final IKalypsoStyle newStyle = input.getStyle();
      if( newStyle != null )
        newStyle.addStyleListener( m_styleListener );
    }

    /* Update form */
    if( m_form.isDisposed() )
      return;

    m_form.getMessageManager().setAutoUpdate( false );

    final Composite body = m_form.getBody();
    ControlUtils.disposeChildren( body );

    if( m_input != null )
      m_styleComposite = new FeatureTypeStyleComposite( m_toolkit, body, m_input );

    updateControl();

    m_form.getMessageManager().setAutoUpdate( true );
  }

  protected void updateControl( )
  {
    updateActions();

    final IKalypsoStyle style = m_input == null ? null : m_input.getStyle();

    if( style == null )
      m_form.setText( MessageBundle.STYLE_EDITOR_NO_STYLE_FOR_EDITOR );
    else
    {
      final String formTitle = style.getTitle();
      if( formTitle == null )
        m_form.setText( Messages.getString( "SLDComposite.0" ) ); //$NON-NLS-1$
      else
        m_form.setText( formTitle );
    }

    if( m_styleComposite != null )
      m_styleComposite.updateControl();
  }

  @Override
  public void dispose( )
  {
    setInput( null );
  }

  public IKalypsoStyle getKalypsoStyle( )
  {
    if( m_input == null )
      return null;

    return m_input.getStyle();
  }

  public void updateActions( )
  {
    m_saveAction.update();
    m_resetAction.update();
  }
}