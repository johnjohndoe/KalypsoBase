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

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.contribs.eclipse.swt.widgets.ControlUtils;
import org.kalypso.contribs.eclipse.ui.forms.ToolkitUtils;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypso.ogc.gml.IKalypsoStyleListener;
import org.kalypso.ui.editor.styleeditor.style.FeatureTypeStyleComposite;
import org.kalypso.ui.editor.styleeditor.style.FeatureTypeStyleInput;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.UserStyle;

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

  private IKalypsoStyle m_style;

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
    m_saveAction.setEnabled( false );
    toolBarManager.add( m_saveAction );
    toolBarManager.update( true );

    setKalypsoStyle( null, null, -1 );
  }

// public IKalypsoStyle getKalypsoStyle( )
// {
// if( m_input == null )
// return null;
//
// final IStyleContext context = m_input.getContext();
// return context.getKalypsoStyle();
// }

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

  public void setKalypsoStyle( final IKalypsoStyle style, final IFeatureType featureType )
  {
    setKalypsoStyle( style, featureType, -1 );
  }

  public void setKalypsoStyle( final IKalypsoStyle style, final IFeatureType featureType, final int styleToSelect )
  {
    final FeatureTypeStyleInput input = createInput( style, featureType, styleToSelect );
    if( ObjectUtils.equals( input, m_input ) )
    {
      if( !m_form.isDisposed() )
        updateControl();
      return;
    }

    if( m_style != null )
      m_style.removeStyleListener( m_styleListener );

    m_input = input;
    m_style = style;

    if( style != null )
      m_style.addStyleListener( m_styleListener );

    /* Update form */
    if( m_form.isDisposed() )
      return;

    final Composite body = m_form.getBody();
    ControlUtils.disposeChildren( body );

    if( m_input != null )
      m_styleComposite = new FeatureTypeStyleComposite( m_toolkit, body, m_input );

    updateControl();
  }

  private FeatureTypeStyleInput createInput( final IKalypsoStyle style, final IFeatureType featureType, final int styleToSelect )
  {
    if( style == null )
      return null;

    /* Use config with default values */
    final IStyleEditorConfig config = new StyleEditorConfig();

    final FeatureTypeStyle fts = findFeatureTypeStyle( style, styleToSelect );
    return new FeatureTypeStyleInput( fts, style, styleToSelect, featureType, config );
  }

  private FeatureTypeStyle findFeatureTypeStyle( final IKalypsoStyle style, final int styleToSelect )
  {
    if( style instanceof FeatureTypeStyle )
      return (FeatureTypeStyle) style;

    if( style instanceof UserStyle )
    {
      final FeatureTypeStyle[] styles = ((UserStyle) style).getFeatureTypeStyles();
      if( styles.length == 0 )
        return null;

      if( styleToSelect == -1 )
        return styles[0];
      else
        return styles[styleToSelect];
    }

    return null;
  }

  protected void updateControl( )
  {
    m_saveAction.update();

    final String formTitle = m_style == null ? MessageBundle.STYLE_EDITOR_NO_STYLE_FOR_EDITOR : m_style.getTitle();
    m_form.setText( formTitle );

    if( m_styleComposite != null )
      m_styleComposite.updateControl();
  }

  @Override
  public void dispose( )
  {
    setKalypsoStyle( null, null );
  }

  public IKalypsoStyle getKalypsoStyle( )
  {
    return m_style;
  }
}