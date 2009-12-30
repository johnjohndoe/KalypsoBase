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
package org.kalypso.util.swt;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.kalypso.i18n.Messages;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoGisPlugin;

/**
 * A composite, showing an {@link org.eclipse.core.runtime.IStatus}.<br>
 * *
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>DETAILS, HIDE_TEXT</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * 
 * @author Gernot Belger
 */
@SuppressWarnings("restriction")
public class StatusComposite extends Composite
{
  /**
   * Style constant: if set, a details button is shown.
   */
  public static final int DETAILS = SWT.SEARCH;

  /**
   * Style constant: if set, the text label is hidden.
   */
  public static final int HIDE_TEXT = SWT.SIMPLE;

  private Label m_imageLabel;

  private Label m_messageLabel;

  private IStatus m_status;

  private Button m_detailsButton;

  private ILabelProvider m_labelProvider;

  public StatusComposite( final Composite parent, final int style )
  {
    super( parent, style );

    init( style );
  }

  protected void init( final int style )
  {
    int colCount = 1;
    createImageLabel();

    if( (style & HIDE_TEXT) == 0 )
    {
      colCount++;
      createMessageLabel();
    }

    if( (style & DETAILS) != 0 )
    {
      colCount++;
      createDetailsButton();
    }

    setStatus( m_status );

    final GridLayout gridLayout = new GridLayout( colCount, false );
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    super.setLayout( gridLayout );
  }

  private void createImageLabel( )
  {
    m_imageLabel = new Label( this, SWT.NONE );
    m_imageLabel.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );
    m_imageLabel.addMouseListener( new MouseAdapter()
    {
      /**
       * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
       */
      @Override
      public void mouseDoubleClick( final MouseEvent e )
      {
        detailsButtonPressed();
      }
    } );
  }

  private void createMessageLabel( )
  {
    m_messageLabel = new Label( this, SWT.NONE );
    m_messageLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    m_messageLabel.addMouseListener( new MouseAdapter()
    {
      /**
       * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
       */
      @Override
      public void mouseDoubleClick( final MouseEvent e )
      {
        detailsButtonPressed();
      }
    } );
  }

  private void createDetailsButton( )
  {
    m_detailsButton = new Button( this, SWT.PUSH );
    m_detailsButton.setText( Messages.getString( "org.kalypso.util.swt.StatusComposite.1" ) ); //$NON-NLS-1$
    m_detailsButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        detailsButtonPressed();
      }
    } );
  }

  /**
   * @see org.eclipse.swt.widgets.Control#setBackground(org.eclipse.swt.graphics.Color)
   */
  @Override
  public void setBackground( final Color color )
  {
    super.setBackground( color );

    if( m_detailsButton != null )
      m_detailsButton.setBackground( color );

    if( m_imageLabel != null )
      m_imageLabel.setBackground( color );

    if( m_messageLabel != null )
      m_messageLabel.setBackground( color );
  }

  protected void detailsButtonPressed( )
  {
    if( m_status == null )
      return;

    final StatusDialog statusTableDialog = new StatusDialog( getShell(), m_status, Messages.getString( "org.kalypso.util.swt.StatusComposite.2" ) ); //$NON-NLS-1$
    statusTableDialog.open();
  }

  /**
   * @see org.eclipse.swt.widgets.Composite#setLayout(org.eclipse.swt.widgets.Layout)
   */
  @Override
  public void setLayout( final Layout layout )
  {
    throw new UnsupportedOperationException( "The layout of this composite is fixed." ); //$NON-NLS-1$
  }

  /**
   * Sets the status of this composites and updates it to show it in the composite.
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *              </ul>
   */
  public void setStatus( final IStatus status )
  {
    m_status = status;

    if( isDisposed() )
      return;

    updateForStatus();
  }

  private void updateForStatus( )
  {
    final Image image = getStatusImage();
    final String text = getStatusText();
    final String tooltipText = getStatusTooltipText();
    final boolean enabled = getStatusIsEnabled();

    m_imageLabel.setImage( image );
    m_imageLabel.setToolTipText( tooltipText );

    if( m_messageLabel != null )
    {
      m_messageLabel.setText( text ); //$NON-NLS-1$
      // Set same text as tooltip, if label is too short to hold the complete text
      m_messageLabel.setToolTipText( tooltipText );
    }

    if( m_detailsButton != null )
      m_detailsButton.setEnabled( enabled );

    layout();
  }

  private boolean getStatusIsEnabled( )
  {
    if( m_status == null )
      return false;

    if( m_status.getException() != null )
      return true;

    return m_status.isMultiStatus();
  }

  private String getStatusText( )
  {
    if( m_status == null )
      return "";

    if( m_labelProvider != null )
    {
      final String providerText = m_labelProvider.getText( m_status );
      if( providerText != null )
        return providerText;
    }

    return m_status.getMessage();
  }

  private String getStatusTooltipText( )
  {
    // Status is same as text, but null instead of empty so totally suppress the tooltip
    final String statusText = getStatusText();
    if( statusText == null || statusText.isEmpty() )
      return null;

    return statusText;
  }

  private Image getStatusImage( )
  {
    if( m_status == null )
      return null;

    if( m_labelProvider != null )
    {
      final Image providerImage = m_labelProvider.getImage( m_status );
      if( providerImage != null )
        return providerImage;
    }

    return getStatusImage( m_status );
  }

  /**
   * Get the IDE image at path.
   * 
   * @param path
   * @return Image
   */
  public static Image getIDEImage( final String constantName )
  {
    return JFaceResources.getResources().createImageWithDefault( IDEInternalWorkbenchImages.getImageDescriptor( constantName ) );
  }

  public static Image getStatusImage( final IStatus status )
  {
    switch( status.getSeverity() )
    {
      case IStatus.OK:
        return KalypsoGisPlugin.getImageProvider().getImage( ImageProvider.DESCRIPTORS.STATUS_IMAGE_OK );

      case IStatus.ERROR:
        return getIDEImage( IDEInternalWorkbenchImages.IMG_OBJS_ERROR_PATH );

      case IStatus.WARNING:
        return getIDEImage( IDEInternalWorkbenchImages.IMG_OBJS_WARNING_PATH );

      case IStatus.INFO:
        return getIDEImage( IDEInternalWorkbenchImages.IMG_OBJS_INFO_PATH );

      default:
        return null;
    }
  }

  public IStatus getStatus( )
  {
    return m_status;
  }

  public void enableButton( final boolean b )
  {
    if( m_detailsButton != null )
      m_detailsButton.setEnabled( b );
  }

  /**
   * Registers a {@link ILabelProvider} with this {@link StatusComposite}.<br>
   * If a label provider is set, it is used to show text and image of the status.<br>
   * If the label provider returns <code>null</code> text or image for a certain status, the composite will fall back to
   * its default behaviour.
   */
  public void setLabelProvider( final ILabelProvider provider )
  {
    m_labelProvider = provider;
  }

}
