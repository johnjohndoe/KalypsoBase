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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.editor.styleeditor.MessageBundle;
import org.kalypso.ui.editor.styleeditor.dialogs.StyleEditorErrorDialog;

/**
 * @author F.Lindemann
 */
public class RulePatternInputPanel
{

  private Composite composite = null;

  private Text minText = null;

  private Text maxText = null;

  private Text stepText = null;

  private double min;

  private double max;

  private double step;

  private final EventListenerList listenerList = new EventListenerList();

  private String label = null;

  public RulePatternInputPanel( final Composite parent, final String m_label, final double m_min, final double m_max, final double m_step )
  {
    setLabel( m_label );
    setMin( m_min );
    setMax( m_max );
    setStep( m_step );
    composite = new Composite( parent, SWT.NULL );
    final GridLayout compositeLayout = new GridLayout( 4, false );
    final GridData compositeData = new GridData();
    compositeData.widthHint = 225;
    composite.setLayoutData( compositeData );
    composite.setLayout( compositeLayout );
    compositeLayout.marginWidth = 0;
    compositeLayout.marginHeight = 0;
    composite.layout();
    init();
  }

  public void addPanelListener( final PanelListener pl )
  {
    listenerList.add( PanelListener.class, pl );
  }

  private void init( )
  {
    final Label urlLabel = new Label( composite, SWT.NULL );
    final GridData urlLabelData = new GridData( 62, 15 );
    urlLabel.setLayoutData( urlLabelData );
    urlLabel.setText( label );

    final Label minLabel = new Label( composite, SWT.NULL );
    minLabel.setText( MessageBundle.STYLE_EDITOR_MIN );

    minText = new Text( composite, SWT.BORDER );
    minText.setBackground( new org.eclipse.swt.graphics.Color( null, new RGB( 255, 255, 255 ) ) );
    final GridData minTextData = new GridData( 25, 10 );
    minText.setLayoutData( minTextData );
    minText.setText( "" + min ); //$NON-NLS-1$

    // null placeholder
    new Label( composite, SWT.NULL ).setLayoutData( new GridData( 50, 15 ) );

    // null placeholder
    new Label( composite, SWT.NULL ).setLayoutData( new GridData( 50, 15 ) );

    final Label maxLabel = new Label( composite, SWT.NULL );
    maxLabel.setText( MessageBundle.STYLE_EDITOR_MAX );

    maxText = new Text( composite, SWT.BORDER );
    maxText.setBackground( new org.eclipse.swt.graphics.Color( null, new RGB( 255, 255, 255 ) ) );
    final GridData maxTextData = new GridData( 25, 10 );
    maxText.setLayoutData( maxTextData );
    maxText.setText( "" + max ); //$NON-NLS-1$

    // null placeholder
    new Label( composite, SWT.NULL ).setLayoutData( new GridData( 50, 15 ) );

    // null placeholder
    new Label( composite, SWT.NULL ).setLayoutData( new GridData( 50, 15 ) );

    final Label stepLabel = new Label( composite, SWT.NULL );
    stepLabel.setText( MessageBundle.STYLE_EDITOR_STEP );

    stepText = new Text( composite, SWT.BORDER );
    stepText.setBackground( new org.eclipse.swt.graphics.Color( null, new RGB( 255, 255, 255 ) ) );
    final GridData stepTextData = new GridData( 25, 10 );
    stepText.setLayoutData( stepTextData );
    stepText.setText( "" + step ); //$NON-NLS-1$

    final Label okButton = new Label( composite, SWT.PUSH );
    okButton.setImage( ImageProvider.IMAGE_STYLEEDITOR_OK.createImage() );
    final GridData okButtonData = new GridData( 22, 15 );
    okButton.setLayoutData( okButtonData );
    okButton.setToolTipText( MessageBundle.STYLE_EDITOR_OK );
    okButton.addMouseListener( new MouseListener()
    {
      @Override
      public void mouseDoubleClick( final MouseEvent e )
      {
        try
        {
          final double t_min = Double.parseDouble( getMinText().getText() );
          final double t_max = Double.parseDouble( getMaxText().getText() );
          final double t_step = Double.parseDouble( getStepText().getText() );
          // check input
          // 1. min < max !!!
          if( t_min > t_max )
          {
            final StyleEditorErrorDialog errorDialog = new StyleEditorErrorDialog( getComposite().getShell(), MessageBundle.STYLE_EDITOR_ERROR_INVALID_INPUT, MessageBundle.STYLE_EDITOR_MIN_MAX );
            errorDialog.showError();
          }
          // step>(max-min)
          else if( t_step > t_max - t_min )
          {
            final StyleEditorErrorDialog errorDialog = new StyleEditorErrorDialog( getComposite().getShell(), MessageBundle.STYLE_EDITOR_ERROR_INVALID_INPUT, MessageBundle.STYLE_EDITOR_STEP_TOO_LARGE );
            errorDialog.showError();
          }
          // step needs to be positive
          else if( t_step <= 0 )
          {
            final StyleEditorErrorDialog errorDialog = new StyleEditorErrorDialog( getComposite().getShell(), MessageBundle.STYLE_EDITOR_ERROR_INVALID_INPUT, MessageBundle.STYLE_EDITOR_STEP_NOT_POSITIVE );
            errorDialog.showError();
          }
          // restrict editor to 35 steps
          else if( (int)Math.ceil( (t_max - t_min) / t_step ) > 35 )
          {
            new StyleEditorErrorDialog( getComposite().getShell(), MessageBundle.STYLE_EDITOR_REMARK, MessageBundle.STYLE_EDITOR_PATTERN_LIMIT ).showError();
          }
          else
          {
            setMin( t_min );
            setMax( t_max );
            setStep( t_step );
            fire();
          }
        }
        catch( final NumberFormatException nfe )
        {
          final StyleEditorErrorDialog errorDialog = new StyleEditorErrorDialog( getComposite().getShell(), MessageBundle.STYLE_EDITOR_ERROR_INVALID_INPUT, MessageBundle.STYLE_EDITOR_ERROR_NUMBER );
          errorDialog.showError();
          getMinText().setText( "" + getMin() ); //$NON-NLS-1$
          getMaxText().setText( "" + getMax() ); //$NON-NLS-1$
          getStepText().setText( "" + getStep() ); //$NON-NLS-1$
        }
        catch( final Exception ex )
        {
          ex.printStackTrace();
        }
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
  }

  public double getMax( )
  {
    return max;
  }

  public double getMin( )
  {
    return min;
  }

  public double getStep( )
  {
    return step;
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

  public Composite getComposite( )
  {
    return composite;
  }

  public void setComposite( final Composite m_composite )
  {
    composite = m_composite;
  }

  public Text getMaxText( )
  {
    return maxText;
  }

  public void setMaxText( final Text m_maxText )
  {
    maxText = m_maxText;
  }

  public Text getMinText( )
  {
    return minText;
  }

  public void setMinText( final Text m_minText )
  {
    minText = m_minText;
  }

  public Text getStepText( )
  {
    return stepText;
  }

  public void setStepText( final Text m_stepText )
  {
    stepText = m_stepText;
  }

  public String getLabel( )
  {
    return label;
  }

  public void setLabel( final String m_label )
  {
    label = m_label;
  }

  public void setMax( final double m_max )
  {
    max = m_max;
  }

  public void setMin( final double m_min )
  {
    min = m_min;
  }

  public void setStep( final double m_step )
  {
    step = m_step;
  }
}