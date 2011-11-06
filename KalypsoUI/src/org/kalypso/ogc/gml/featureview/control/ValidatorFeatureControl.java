/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import java.util.Vector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.validation.rules.IRule;
import org.kalypsodeegree_impl.model.feature.validation.rules.RuleFactory;

/**
 * This class controls the ValidatorLabel and its behavior.
 * 
 * @author Holger Albert
 */
public class ValidatorFeatureControl extends AbstractFeatureControl
{
  /**
   * This variable stores the label itself.
   */
  protected Label m_label;

  /**
   * This variable stores the rules, that should be applied for the property_type.
   */
  private IRule[] m_rules;

  /**
   * This variable stores, if the ok_status should be shown.
   */
  private boolean m_showok;

  /* This creates the images once for the ValidatorFeature once, cause they are not disposed at this time. */
  // TODO: use Plugin-Imageprovider to handle life-cycle of images!
  protected static Image image_empty;

  private static Image image_ok;

  private static Image image_warning;

  protected static Image image_error;

  private final boolean m_showWarning;

  private final String m_warningText;

  static
  {
    /*
     * The empty image is needed, if the ok_image is not to be displayed. There must be an image set, otherwise, if the
     * image in the label is null and you are trying to set a new image it will result somehow in an error. And no image
     * is set anymore for this label.
     */
    ImageDescriptor imgdesc = org.kalypso.ui.ImageProvider.IMAGE_FEATURE_VALIDATION_EMPTY;
    image_empty = imgdesc.createImage();

    imgdesc = org.kalypso.ui.ImageProvider.IMAGE_FEATURE_VALIDATION_OK;
    image_ok = imgdesc.createImage();

    imgdesc = org.kalypso.ui.ImageProvider.IMAGE_FEATURE_VALIDATION_WARNING;
    image_warning = imgdesc.createImage();

    imgdesc = org.kalypso.ui.ImageProvider.IMAGE_FEATURE_VALIDATION_NOTOK;
    image_error = imgdesc.createImage();
  }

  /**
   * The Constructor.
   * 
   * @param feature
   *          The GML that represents the feature.
   * @param ftp
   *          The GML schema that represents a feature property.
   * @param showok
   *          Determines, if a status (image and text) should be shown, if everything ist ok.
   */
  public ValidatorFeatureControl( final Feature feature, final IPropertyType ftp, final boolean showok )
  {
    this( feature, ftp, showok, false, null );
  }

  /**
   * @param showWarning
   *          Show warning icon
   * @param warningText
   *          tooltip text for warning icon
   */
  public ValidatorFeatureControl( final Feature feature, final IPropertyType ftp, final boolean showok, final boolean showWarning, final String warningText )
  {
    super( feature, ftp );

    m_showok = showok;
    m_showWarning = showWarning;
    m_warningText = warningText;

    /* Get the specific rules for this PropertyType. */
    m_rules = RuleFactory.getRules( ftp );
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureControl#createControl(org.eclipse.swt.widgets.Composite, int)
   */
  @Override
  public Control createControl( final Composite parent, final int style )
  {
    /* Create a new label. */
    m_label = new Label( parent, style );

    if( m_rules.length > 0 )
    {
      /* Set the image. */
      m_label.setImage( image_warning );

      /* Check the first time. */
      updateControl();
    }
    else
    {
      m_label.setVisible( false );
    }

    return m_label;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureControl#updateControl()
   */
  @Override
  public void updateControl( )
  {
    /* If there are now rules, than do nothing to validate the input. */
    if( m_rules.length == 0 )
      return;

    final Feature feature = getFeature();

    if( feature != null )
    {
      final IPropertyType ftp = getFeatureTypeProperty();

      if( ftp != null )
      {
        /* Check for validation here. */
        Vector<IStatus> status = new Vector<IStatus>();

        for( int i = 0; i < m_rules.length; i++ )
          status.add( m_rules[i].isValid( feature.getProperty( ftp ) ) );

        /* Set the image and a tooltip. */
        MultiStatus mstatus = new MultiStatus( Platform.PI_RUNTIME, Status.OK, status.toArray( new Status[] {} ), "", null ); //$NON-NLS-1$
        if( mstatus.isOK() == true )
        {
          if( m_showok == true )
            m_label.setImage( image_ok );
          else if( m_showWarning == true )
            m_label.setImage( image_warning );
          else
            m_label.setImage( image_empty );

          if( m_showWarning )
            m_label.setToolTipText( m_warningText );
          else
            m_label.setToolTipText( "" ); //$NON-NLS-1$

        }
        else
        {
          m_label.setImage( image_error );

          String message = ""; //$NON-NLS-1$
          for( int i = 0; i < status.size(); i++ )
          {
            if( !status.get( i ).isOK() )
              message = message + status.get( i ).getMessage() + "\r\n"; //$NON-NLS-1$
          }

          m_label.setToolTipText( message );
        }
      }
    }
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureControl#isValid()
   */
  @Override
  public boolean isValid( )
  {
    return true;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureControl#addModifyListener(org.eclipse.swt.events.ModifyListener)
   */
  @Override
  public void addModifyListener( final ModifyListener l )
  {
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureControl#removeModifyListener(org.eclipse.swt.events.ModifyListener)
   */
  @Override
  public void removeModifyListener( final ModifyListener l )
  {
  }
}