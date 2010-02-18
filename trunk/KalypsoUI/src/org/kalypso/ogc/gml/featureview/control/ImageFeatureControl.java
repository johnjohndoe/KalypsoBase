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
package org.kalypso.ogc.gml.featureview.control;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.contribs.eclipse.core.runtime.jobs.MutexRule;
import org.kalypso.contribs.eclipse.swt.widgets.ImageCanvas;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.i18n.Messages;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.util.swt.StatusComposite;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author Gernot Belger
 */
public class ImageFeatureControl extends AbstractFeatureControl
{
  private final ISchedulingRule m_mutex = new MutexRule();

  private ImageCanvas m_imgCanvas;

  private static final QName QNAME_STRING = new QName( XMLConstants.W3C_XPATH_DATATYPE_NS_URI, "string" ); //$NON-NLS-1$

  private static final String DATA_DISPOSE_IMAGE = "doDisposeImage"; //$NON-NLS-1$

  public ImageFeatureControl( final IPropertyType ftp )
  {
    super( ftp );
  }

  public ImageFeatureControl( final Feature feature, final IPropertyType ftp )
  {
    super( feature, ftp );
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureControl#createControl(org.eclipse.swt.widgets.Composite, int)
   */
  public Control createControl( final Composite parent, final int style )
  {
    final ImageCanvas imgCanvas = new ImageCanvas( parent, style );
    m_imgCanvas = imgCanvas;
    imgCanvas.addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        disposeOldImage();
      }
    } );

    updateControl();

    return m_imgCanvas;
  }

  protected void disposeOldImage( )
  {
    if( m_imgCanvas.isDisposed() )
      return;

    final Image oldImage = m_imgCanvas.getImage();
    final Boolean oldDoDispose = (Boolean) m_imgCanvas.getData( DATA_DISPOSE_IMAGE );
    if( oldImage != null && oldDoDispose != null && oldDoDispose )
      oldImage.dispose();
  }

  protected void handleControlResized( )
  {
    updateControl();
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureControl#isValid()
   */
  public boolean isValid( )
  {
    // this control does not modify, so its always valid
    return true;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureControl#addModifyListener(org.eclipse.swt.events.ModifyListener)
   */
  public void addModifyListener( final ModifyListener l )
  {
    // this control does not modify, so listeners are ignored
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureControl#removeModifyListener(org.eclipse.swt.events.ModifyListener)
   */
  public void removeModifyListener( final ModifyListener l )
  {
    // this control does not modify, so listeners are ignored
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureControl#updateControl()
   */
  public void updateControl( )
  {
    final Image waitingImage = KalypsoGisPlugin.getImageProvider().getImage( ImageProvider.DESCRIPTORS.WAIT_LOADING_OBJ );
    setImage( waitingImage, "Loading...", false );

    // must be a string property
    final String imgPath = getImagePath();
    
    final String error = loadImage( imgPath );
    if( error != null )
      setImage( StatusComposite.getStatusImage( IStatus.WARNING ), error, false );
  }

  void setImage( final Image image, final String tooltipText, final boolean disposeImageAfterUse )
  {
    disposeOldImage();

    if( m_imgCanvas.isDisposed() )
    {
      if( disposeImageAfterUse && image != null )
        image.dispose();
      return;
    }

    m_imgCanvas.setData( DATA_DISPOSE_IMAGE, disposeImageAfterUse );
    m_imgCanvas.setImage( image );
    m_imgCanvas.setToolTipText( tooltipText );

    layoutScrolledParent( m_imgCanvas );
  }

  private String loadImage( final String imgPath )
  {
    if( imgPath == null )
      return Messages.getString( "org.kalypso.ogc.gml.featureview.control.ImageFeatureControl.2" ) + getFeatureTypeProperty(); //$NON-NLS-1$

    if( imgPath.length() == 0 )
      return ""; //$NON-NLS-1$

    try
    {
      final GMLWorkspace workspace = getFeature().getWorkspace();
      final URL context = workspace.getContext();
      final URL url = new URL( context, imgPath );

      startImageJob( url );
      return null;
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();

      return Messages.getString( "org.kalypso.ogc.gml.featureview.control.ImageFeatureControl.5" ) + imgPath; //$NON-NLS-1$
    }
  }

  private void startImageJob( final URL url )
  {
    final Job loadImageJob = new Job( "Load Image" )
    {
      @Override
      protected IStatus run( final IProgressMonitor monitor )
      {
        final ImageDescriptor imgDesc = ImageDescriptor.createFromURL( url );
        final Image image = imgDesc.createImage( false );
        if( image == null )
        {
          final String msg = Messages.getString( "org.kalypso.ogc.gml.featureview.control.ImageFeatureControl.4" ) + url.toExternalForm(); //$NON-NLS-1$
          setImageInUIJob( null, msg, false );
          return Status.OK_STATUS;
        }

        final String tooltip = url.toExternalForm();
        setImageInUIJob( image, tooltip, true );
        return Status.OK_STATUS;
      }
    };
    loadImageJob.setRule( m_mutex );
    loadImageJob.setSystem( true );
    loadImageJob.schedule();
  }

  protected void setImageInUIJob( final Image image, final String tooltip, final boolean disposeImageAfterUse )
  {
    if( m_imgCanvas.isDisposed() )
      return;

    final Display display = m_imgCanvas.getDisplay();
    final UIJob uiJob = new UIJob( display, "Update image" )
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        setImage( image, tooltip, disposeImageAfterUse );
        return Status.OK_STATUS;
      }
    };

    uiJob.setRule( m_mutex );
    uiJob.setSystem( true );
    uiJob.schedule();
  }

  private String getImagePath( )
  {
    final Feature feature = getFeature();
    final IPropertyType pt = getFeatureTypeProperty();

    if( feature == null || pt == null || GMLSchemaUtilities.substitutes( feature.getFeatureType(), QNAME_STRING ) )
      return null;

    final String uriString = (String) feature.getProperty( pt );
    if( uriString == null )
      return "";

    return uriString;
  }

  public static void layoutScrolledParent( final Control control )
  {
    if( control == null )
      return;

    final Composite parent = control.getParent();
    if( parent == null )
      return;

    if( parent instanceof ScrolledComposite )
    {
      parent.layout();
      return;
    }

    parent.layout();
    layoutScrolledParent( parent );
  }
}
