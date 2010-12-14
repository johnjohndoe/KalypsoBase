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
package org.kalypso.contribs.eclipse.jface.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

/**
 * An {@link org.eclipse.ui.forms.widgets.ImageHyperlink} based on a {@link IAction}.<br/>
 * The hyperlink will be configured and updated automatically depending on the state of an {@link IAction}.<br/>
 * If the link is activated, the action is run.
 * 
 * @author Gernot Belger
 */
public class ActionHyperlink
{
  private static final String DATA_IMAGE = "image"; //$NON-NLS-1$

  private static final String DATA_HOVER_IMAGE = "hoverImage"; //$NON-NLS-1$

  private static final String DATA_MENU = "menuCreator"; //$NON-NLS-1$

  private static final String DATA_DESCRIPTOR = "_descriptor"; //$NON-NLS-1$

  private final IAction m_action;

  private final ImageHyperlink m_link;

  public static ImageHyperlink createHyperlink( final FormToolkit toolkit, final Composite parent, final int style, final IAction action )
  {
    // REMARK: could we use some style of the action here?
    // action.getStyle()

    final ImageHyperlink link = createHyperlink( toolkit, parent, style );
    new ActionHyperlink( link, action );
    return link;
  }

  private static ImageHyperlink createHyperlink( final FormToolkit toolkit, final Composite parent, final int style )
  {
    if( toolkit == null )
      return new ImageHyperlink( parent, style );
    else
      return toolkit.createImageHyperlink( parent, style );
  }

  public ActionHyperlink( final ImageHyperlink link, final IAction action )
  {
    m_link = link;
    m_action = action;

    hookListeners( link, action );

    initializeLink( link, action );

    updateButton();
  }

  private void hookListeners( final ImageHyperlink link, final IAction action )
  {
    final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener()
    {
      @Override
      public void propertyChange( final PropertyChangeEvent event )
      {
        updateButton();
      }
    };

    action.addPropertyChangeListener( propertyChangeListener );

    link.addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        action.removePropertyChangeListener( propertyChangeListener );
        unhookData();
      }
    } );

    link.addHyperlinkListener( new HyperlinkAdapter()
    {
      /**
       * @see org.eclipse.ui.forms.events.HyperlinkAdapter#linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent)
       */
      @Override
      public void linkActivated( final HyperlinkEvent e )
      {
        final Event event = new Event();

        event.data = e.data;
        event.time = e.time;
        event.display = e.display;
        event.widget = e.widget;

        handleLinkActivated( event );
      }
    } );
  }

  protected void unhookData( )
  {
    final Object menuCreator = m_link.getData( DATA_MENU );
    if( menuCreator instanceof IMenuCreator )
      ((IMenuCreator) menuCreator).dispose();

    checkImage( null, DATA_IMAGE );
    checkImage( null, DATA_HOVER_IMAGE );
  }

  protected void handleLinkActivated( final Event event )
  {
    m_action.runWithEvent( event );
  }

  /**
   * Set properties that do NOT vary over time, only once.
   */
  private void initializeLink( final ImageHyperlink link, final IAction action )
  {
    link.setHref( action );

    final IMenuCreator menuCreator = m_action.getMenuCreator();
    if( menuCreator != null )
    {
      final Menu menu = menuCreator.getMenu( link );
      m_link.setMenu( menu );
      m_link.setData( DATA_MENU, menuCreator );
    }

    // REMARK: not yet implemented
    // m_action.getAccelerator();
    // m_action.getHelpListener();
  }

  /**
   * Change properties that may vary over time.
   */
  protected void updateButton( )
  {
    final boolean enabled = m_action.isEnabled();
    m_link.setEnabled( enabled );

    final ImageDescriptor imageDescriptor = getImageDescriptor( enabled );

    final Image image = checkImage( imageDescriptor, DATA_IMAGE );
    m_link.setImage( image );

    final ImageDescriptor hoverImageDescriptor = m_action.getHoverImageDescriptor();
    final Image hoverImage = checkImage( hoverImageDescriptor, DATA_HOVER_IMAGE );
    m_link.setHoverImage( hoverImage );

    // Which action image should we use for active image?
    // m_link.setActiveImage( image );

    final String text = m_action.getText();
    m_link.setText( text ); //$NON-NLS-1$
    m_link.setToolTipText( m_action.getDescription() );

    final boolean textIsBlank = text == null || text.trim().isEmpty();
    final boolean underlined = !textIsBlank;

    m_link.setUnderlined( underlined );
  }

  private ImageDescriptor getImageDescriptor( final boolean enabled )
  {
    if( !enabled )
    {
      final ImageDescriptor disabledDescriptor = m_action.getDisabledImageDescriptor();
      if( disabledDescriptor != null )
        return disabledDescriptor;
    }

    return m_action.getImageDescriptor();
  }

  private Image checkImage( final ImageDescriptor imageDescriptor, final String dataImage )
  {
    final String dataDescriptor = dataImage + DATA_DESCRIPTOR;

    final Image existingImage = (Image) m_link.getData( dataImage );
    final ImageDescriptor existingDescriptor = (ImageDescriptor) m_link.getData( dataDescriptor );

    /* If image is still the same (we can only check the descriptor) we return it */
    if( existingDescriptor != null && existingDescriptor == imageDescriptor )
      return existingImage;

    /* dispose old image */
    if( existingImage != null )
      existingImage.dispose();

    /* Prepare for exception */
    m_link.setData( dataImage, null );
    m_link.setData( dataDescriptor, null );

    if( imageDescriptor == null )
      return null;

    final Image image = imageDescriptor.createImage();
    m_link.setData( dataImage, image );
    m_link.setData( dataDescriptor, imageDescriptor );

    return image;
  }

}
