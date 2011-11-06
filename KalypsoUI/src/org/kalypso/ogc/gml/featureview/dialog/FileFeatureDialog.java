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
package org.kalypso.ogc.gml.featureview.dialog;

import java.io.File;
import java.util.Collection;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypsodeegree.model.feature.Feature;

/**
 * This class opens a file dialog.
 * 
 * @author Holger Albert
 */
public class FileFeatureDialog implements IFeatureDialog
{
  private FeatureChange m_change = null;

  private final Feature m_feature;

  private final IValuePropertyType m_ftp;

  public FileFeatureDialog( final Feature feature, final IValuePropertyType ftp )
  {
    m_feature = feature;
    m_ftp = ftp;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.dialog.IFeatureDialog#collectChanges(java.util.Collection)
   */
  @Override
  public void collectChanges( Collection<FeatureChange> c )
  {
    if( c != null && m_change != null )
      c.add( m_change );
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.dialog.IFeatureDialog#getLabel()
   */
  @Override
  public String getLabel( )
  {
    return Messages.getString( "org.kalypso.ogc.gml.featureview.dialog.FilePropertyDialog.editvalues" ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.dialog.IFeatureDialog#open(org.eclipse.swt.widgets.Shell)
   */
  @Override
  public int open( Shell shell )
  {
    FileDialog dialog = new FileDialog( shell );
    dialog.setText( Messages.getString( "org.kalypso.ogc.gml.featureview.dialog.FilePropertyDialog.text" ) ); //$NON-NLS-1$

    File file = (File) m_feature.getProperty( m_ftp );

    if( file != null && file.exists() )
    {
      if( file.isDirectory() )
        dialog.setFilterPath( file.getAbsolutePath() );
      else
        dialog.setFilterPath( file.getParent() );
    }

    String result = dialog.open();

    if( result != null )
    {
      File newFile = new File( result );
      m_change = new FeatureChange( m_feature, m_ftp, newFile );
      return Window.OK;
    }

    return Window.CANCEL;
  }
}