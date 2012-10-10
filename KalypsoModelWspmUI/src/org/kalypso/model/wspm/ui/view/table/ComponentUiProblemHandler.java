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
package org.kalypso.model.wspm.ui.view.table;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfilePointMarker;
import org.kalypso.model.wspm.core.profil.IProfilePointMarkerProvider;
import org.kalypso.model.wspm.core.profil.MarkerIndex;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.core.profil.wrappers.ProfileRecord;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler;

/**
 * TODO: show marker text as tooltip<br>
 * TODO: open dialog that shows all markers if user clicks on marker (use cell-editor)
 *
 * @author Gernot Belger
 * @author Kim Werner
 */
@SuppressWarnings("restriction")
public class ComponentUiProblemHandler implements IComponentUiHandler
{
  private static final String IMAGE_ERROR = "profilLabelProvider.img.error"; //$NON-NLS-1$

  private static final String IMAGE_WARNING = "profilLabelProvider.img.warning"; //$NON-NLS-1$

  private static final String IMAGE_INFO = "profilLabelProvider.img.info"; //$NON-NLS-1$

  private static final String IMAGE_NO_ERROR = "profilLabelProvider.img.no_error"; //$NON-NLS-1$

  /* Static as this class is created quite often, but cannot be disposed. */
  private static final ImageRegistry IMG_REGISTRY = new ImageRegistry();

  static
  {
    IMG_REGISTRY.put( IMAGE_ERROR, IDEInternalWorkbenchImages.getImageDescriptor( IDEInternalWorkbenchImages.IMG_OBJS_ERROR_PATH ) );// KalypsoModelWspmUIImages.ID_MARKER_ERROR

    IMG_REGISTRY.put( IMAGE_WARNING, IDEInternalWorkbenchImages.getImageDescriptor( IDEInternalWorkbenchImages.IMG_OBJS_WARNING_PATH ) );// KalypsoModelWspmUIImages.ID_MARKER_WARNING

    IMG_REGISTRY.put( IMAGE_INFO, IDEInternalWorkbenchImages.getImageDescriptor( IDEInternalWorkbenchImages.IMG_OBJS_INFO_PATH ) );// KalypsoModelWspmUIImages.ID_MARKER_WARNING

    IMG_REGISTRY.put( IMAGE_NO_ERROR, ImageDescriptor.createFromImageData( new ImageData( 16, 16, 1, new PaletteData( new RGB[] { new RGB( 255, 255, 255 ) } ) ) ) );
  }

  private final IProfile m_profile;

  public ComponentUiProblemHandler( final IProfile profile )
  {
    m_profile = profile;
  }

  @Override
  public CellEditor createCellEditor( final Table table )
  {
    return null;
  }

  @Override
  public Object doGetValue( final IRecord record )
  {
    return null;
  }

  @Override
  public String getColumnLabel( )
  {
    return "-"; //$NON-NLS-1$
  }

  @Override
  public int getColumnStyle( )
  {
    return SWT.CENTER;
  }

  @Override
  public int getColumnWidth( )
  {
    return 20;
  }

  @Override
  public int getColumnWidthPercent( )
  {
    return -1;
  }

  @Override
  public String getIdentity( )
  {
    return getClass().getName();
  }

  @Override
  public String getStringRepresentation( final IRecord record )
  {
    return ""; //$NON-NLS-1$
  }

  @Override
  public Image getImage( final IRecord record )
  {
    final MarkerIndex markerIndex = m_profile.getProblemMarker();
    Image backgroundImage = IMG_REGISTRY.get( IMAGE_NO_ERROR );
    final StringBuffer buffer = new StringBuffer();
    int severity = -1;
    if( markerIndex != null )
    {
      final IMarker[] markers = markerIndex.get( record );
      final IMarker worst = MarkerUtils.worstOf( markers );
      if( worst != null )
      {
        severity = MarkerUtils.getSeverity( worst );
        if( IMarker.SEVERITY_ERROR == severity )
        {
          backgroundImage = IMG_REGISTRY.get( IMAGE_ERROR );
        }
        else if( IMarker.SEVERITY_WARNING == severity )
        {
          backgroundImage = IMG_REGISTRY.get( IMAGE_WARNING );
        }
        else if( IMarker.SEVERITY_INFO == severity )
        {
          backgroundImage = IMG_REGISTRY.get( IMAGE_INFO );
        }
      }
    }
    final String[] deviderTypes = getMarkerTypes( record );
    if( deviderTypes == null || deviderTypes.length == 0 )
      return backgroundImage;

    buffer.append( severity );
    buffer.append( Arrays.toString( deviderTypes ) );
    final String key = buffer.toString();

    final IProfilePointMarkerProvider mp = KalypsoModelWspmCoreExtensions.getMarkerProviders( m_profile.getType() );
    final Image image = IMG_REGISTRY.get( key );
    if( image == null )
    {
      final Display display = Display.getCurrent();
      final Image img = new Image( display, 16, 16 );
      final GC gc = new GC( img );
      try
      {
        mp.drawMarker( deviderTypes, gc );
        if( severity != -1 )
        {
          gc.drawImage( backgroundImage, 0, 0 );
        }

        IMG_REGISTRY.put( key, img );
      }
      finally
      {
        gc.dispose();
      }
    }

    return IMG_REGISTRY.get( key );
  }

  @Override
  public boolean isEditable( )
  {
    // TODO: set to true and implement editor
    return false;
  }

  @Override
  public boolean isMoveable( )
  {
    return true;
  }

  @Override
  public boolean isResizeable( )
  {
    return false;
  }

  @Override
  public void doSetValue( final IRecord record, final Object value )
  {
  }

  /**
   * @deprecated Use {@link #getMarkerTypes(IRecord)} instead.
   * @return the first DeviderTyp for this point
   */

  @Deprecated
  public final IComponent getDeviderTyp( final IProfileRecord point )
  {
    final IProfilePointMarker[] markers = m_profile.getPointMarkerFor( point );
    if( markers == null )
      return null;

    return markers.length > 0 ? markers[0].getComponent() : null;
  }

  /**
   * @return the all DeviderTypeIds for this point, maybe null
   */
  public final String[] getMarkerTypes( final IRecord point )
  {
    final IProfilePointMarker[] markers = m_profile.getPointMarkerFor( point instanceof IProfileRecord ? (IProfileRecord) point : new ProfileRecord( m_profile, point ) );
    if( markers == null || markers.length == 0 )
      return null;
    final HashSet<String> types = new HashSet<>();
    for( final IProfilePointMarker marker : markers )
    {
      final IComponent type = marker.getComponent();
      if( !types.contains( type.getId() ) )
      {
        types.add( type.getId() );
      }
    }

    return types.toArray( new String[] {} );
  }

  @Override
  public Object parseValue( final String text )
  {
    return null;
  }

  @Override
  public void setValue( final IRecord record, final Object value )
  {
  }
}
