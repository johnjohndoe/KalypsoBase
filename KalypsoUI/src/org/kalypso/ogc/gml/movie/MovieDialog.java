/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.ogc.gml.movie;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.ogc.gml.AbstractCascadingLayerTheme;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.movie.controls.MovieComposite;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * The movie dialog.
 * 
 * @author Holger Albert
 */
public class MovieDialog extends Dialog
{
  /**
   * The gis template map model.
   */
  private GisTemplateMapModell m_mapModel;

  /**
   * The theme, marked as movie theme.
   */
  private AbstractCascadingLayerTheme m_movieTheme;

  /**
   * The bounding box.
   */
  private GM_Envelope m_boundingBox;

  /**
   * The movie composite.
   */
  private MovieComposite m_movieComposite;

  /**
   * Creates a dialog instance. Note that the window will have no visual representation (no widgets) until it is told to
   * open. By default, open blocks for dialogs.
   * 
   * @param parentShell
   *          The parent shell, or null to create a top-level shell.
   * @param mapModel
   *          The gis template map model.
   * @param movieTheme
   *          The theme, marked as movie theme.
   * @param boundingBox
   *          The bounding box.
   */
  public MovieDialog( Shell parentShell, GisTemplateMapModell mapModel, AbstractCascadingLayerTheme movieTheme, GM_Envelope boundingBox )
  {
    super( parentShell );

    m_mapModel = mapModel;
    m_movieTheme = movieTheme;
    m_boundingBox = boundingBox;
    m_movieComposite = null;
  }

  /**
   * Creates a dialog with the given parent.
   * 
   * @param parentShell
   *          Object that returns the current parent shell.
   * @param mapModel
   *          The gis template map model.
   * @param movieTheme
   *          The theme, marked as movie theme.
   * @param boundingBox
   *          The bounding box.
   */
  public MovieDialog( IShellProvider parentShell, GisTemplateMapModell mapModel, AbstractCascadingLayerTheme movieTheme, GM_Envelope boundingBox )
  {
    super( parentShell );

    m_mapModel = mapModel;
    m_movieTheme = movieTheme;
    m_boundingBox = boundingBox;
    m_movieComposite = null;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea( Composite parent )
  {
    /* Set the title. */
    getShell().setText( "Film" );

    /* Create the main composite. */
    Composite main = (Composite) super.createDialogArea( parent );
    main.setLayout( new GridLayout( 1, false ) );
    GridData mainData = new GridData( SWT.FILL, SWT.FILL, true, true );
    mainData.widthHint = 400;
    main.setLayoutData( mainData );

    /* Create the movie composite. */
    m_movieComposite = new MovieComposite( main, SWT.NONE, m_mapModel, m_movieTheme, m_boundingBox );
    m_movieComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    return main;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createButtonBar( Composite parent )
  {
    Composite buttonComposite = m_movieComposite.createButtonControls( parent );
    buttonComposite.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, true, false ) );

    return buttonComposite;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#isResizable()
   */
  @Override
  protected boolean isResizable( )
  {
    return true;
  }
}