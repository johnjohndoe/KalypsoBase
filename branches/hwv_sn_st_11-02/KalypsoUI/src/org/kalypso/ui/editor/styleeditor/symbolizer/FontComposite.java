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
package org.kalypso.ui.editor.styleeditor.symbolizer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.ui.editor.styleeditor.MessageBundle;
import org.kalypso.ui.editor.styleeditor.util.IValueReceiver;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.sld.Font;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * @author F.Lindemann
 */
public class FontComposite extends Composite
{
  private final Label m_previewLabel;

  private final IValueReceiver<Font> m_receiver;

  private org.eclipse.swt.graphics.Font m_swtFont;

  private Color m_swtColor = null;

  private Font m_sldFont;

  public FontComposite( final FormToolkit toolkit, final Composite parent, final IValueReceiver<Font> receiver )
  {
    super( parent, SWT.NONE );

    m_receiver = receiver;

    toolkit.adapt( this );
    setLayout( new GridLayout( 3, false ) );

    addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        dispose();
      }
    } );

    toolkit.createLabel( this, MessageBundle.STYLE_EDITOR_FONT );

    m_previewLabel = toolkit.createLabel( this, StringUtils.EMPTY, SWT.BORDER );
    m_previewLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    m_previewLabel.setAlignment( SWT.CENTER );
    m_previewLabel.setText( "Demotext" );

    final Button fontChooserButton = new Button( this, SWT.PUSH );
    fontChooserButton.setText( "..." ); //$NON-NLS-1$
    fontChooserButton.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        handleFontButtonPressed();
      }
    } );
  }

  protected void handleFontButtonPressed( )
  {
    final FontDialog dialog = new FontDialog( this.getShell() );
    if( m_swtFont != null )
      dialog.setFontList( m_swtFont.getFontData() );
    if( m_swtColor != null )
      dialog.setRGB( new RGB( m_swtColor.getRed(), m_swtColor.getGreen(), m_swtColor.getBlue() ) );

    final FontData result = dialog.open();
    if( result == null )
      return;

    final RGB rgb = dialog.getRGB();
    setFont( result, rgb );
  }

  protected void setFont( final FontData data, final RGB rgb )
  {
    final boolean isBold = (data.getStyle() & SWT.BOLD) != 0;
    final boolean isItalic = (data.getStyle() & SWT.ITALIC) != 0;

    final Font newFont = StyleFactory.createFont( data.getName(), isItalic, isBold, data.getHeight() );
    newFont.setColor( new java.awt.Color( rgb.red, rgb.green, rgb.blue ) );

    m_receiver.updateValue( newFont );
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    disposeSwtResources();

    super.dispose();
  }

  public org.eclipse.swt.graphics.Font convertDegreeToSWTFont( final Font font ) throws FilterEvaluationException
  {
    if( font == null )
      return null;

    final FontData fontData = new FontData();
    fontData.setName( font.getFamily( null ) );
    fontData.setHeight( font.getSize( null ) );

    final int style = font.getStyle( null );
    final int weight = font.getWeight( null );
    if( style == Font.STYLE_NORMAL && weight == Font.WEIGHT_NORMAL )
      fontData.setStyle( SWT.NORMAL );
    else if( style == Font.STYLE_NORMAL && weight == Font.WEIGHT_BOLD )
      fontData.setStyle( SWT.BOLD );
    else if( style == Font.STYLE_ITALIC && weight == Font.WEIGHT_NORMAL )
      fontData.setStyle( SWT.ITALIC );
    else if( style == Font.STYLE_ITALIC && weight == Font.WEIGHT_BOLD )
      fontData.setStyle( SWT.ITALIC | SWT.BOLD );

    return new org.eclipse.swt.graphics.Font( getDisplay(), fontData );
  }

  public void setFont( final Font sldFont )
  {
    m_sldFont = sldFont;

    updateControl();
  }

  public void updateControl()
  {
    m_previewLabel.setFont( null );
    m_previewLabel.setForeground( null );

    disposeSwtResources();

    m_previewLabel.setEnabled( m_sldFont != null );
    if( m_sldFont == null )
      return;

    try
    {
      final java.awt.Color sldColor = m_sldFont.getColor( null );
      if( sldColor != null )
        m_swtColor = new Color( getDisplay(), sldColor.getRed(), sldColor.getGreen(), sldColor.getBlue() );

      m_swtFont = convertDegreeToSWTFont( m_sldFont );

      m_previewLabel.setFont( m_swtFont );
      m_previewLabel.setForeground( m_swtColor );
    }
    catch( final FilterEvaluationException e )
    {
      e.printStackTrace();
    }
  }

  private void disposeSwtResources( )
  {
    if( m_swtFont != null )
    {
      m_swtFont.dispose();
      m_swtFont = null;
    }

    if( m_swtColor != null )
    {
      m_swtColor.dispose();
      m_swtColor = null;
    }
  }
}