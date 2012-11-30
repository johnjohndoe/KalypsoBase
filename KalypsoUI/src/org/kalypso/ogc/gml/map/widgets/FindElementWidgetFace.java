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
package org.kalypso.ogc.gml.map.widgets;

/**
 *  @author ig
 */
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.handlers.IHandlerService;
import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.ogc.gml.IKalypsoCascadingTheme;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.command.ChangeExtentCommand;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.handlers.MapHandlerUtils;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

class FindElementWidgetFace
{
  private final String m_defaultCrs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

  // FIXME: both constants are ultra ugly: hidden dependency to 1d2d model! so this tool works only for 1d2d or is it
  // general??!! -> bad design
  private static final QName m_resultIdQName = new QName( "http://www.tu-harburg.de/wb/kalypso/schemata/1d2dResults", "calcId" ); //$NON-NLS-1$  //$NON-NLS-2$

  private static final QName m_nameQName = new QName( NS.GML3, "name" ); //$NON-NLS-1$

  private static final String m_wildCChar = "*"; //$NON-NLS-1$

  private final Map<String, Feature> m_mapCacheFound = new HashMap<>();

  private final Set<Feature> m_featureList = new HashSet<>();

  private final FindElementMapWidget m_findElementMapWidget;

  private boolean m_boolIsSimple = true;

  private boolean m_boolFound;

  private Feature m_feature;

  private Text m_name;

  private Text m_gmlId;

  private Text m_id;

  private Text m_posX;

  private Text m_posY;

  public FindElementWidgetFace( final FindElementMapWidget findElementMapWidget )
  {
    m_findElementMapWidget = findElementMapWidget;
  }

  public Control createControl( final FormToolkit toolkit, final Composite parent )
  {
    final Composite rootPanel = new Composite( parent, SWT.FILL );

    GridLayoutFactory.swtDefaults().numColumns( 2 ).applyTo( rootPanel );
    toolkit.adapt( rootPanel );

    createGUI( toolkit, rootPanel );

    final Button lButtonSearch = toolkit.createButton( rootPanel, Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.4" ), SWT.PUSH ); //$NON-NLS-1$
    lButtonSearch.setSize( 30, 15 );
    lButtonSearch.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        doSearchOperation();
      }
    } );

    final Button lButtonReset = toolkit.createButton( rootPanel, Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.5" ), SWT.PUSH ); //$NON-NLS-1$
    lButtonReset.setSize( 30, 15 );
    lButtonReset.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        handleResetClicked();
      }
    } );

    return rootPanel;
  }

  protected void handleResetClicked( )
  {
    m_gmlId.setText( "" ); //$NON-NLS-1$

    if( m_id != null && m_id.isVisible() )
      m_id.setText( "" ); //$NON-NLS-1$

    m_name.setText( "" ); //$NON-NLS-1$
    m_posX.setText( "" ); //$NON-NLS-1$
    m_posY.setText( "" ); //$NON-NLS-1$

    m_findElementMapWidget.reset();
  }

  private void createGUI( final FormToolkit toolkit, final Composite parent )
  {
    // FIXME: use binding!

    toolkit.createLabel( parent, Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.6" ) ); //$NON-NLS-1$
    m_gmlId = toolkit.createText( parent, "", SWT.SINGLE | SWT.BORDER ); //$NON-NLS-1$
    m_gmlId.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
    m_gmlId.addKeyListener( keyListnerEnter() );

    final boolean is1d2dModule = isIn1d2dModule();
    if( is1d2dModule )
    {
      toolkit.createLabel( parent, Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.7" ) ); //$NON-NLS-1$
      m_id = toolkit.createText( parent, "", SWT.SINGLE | SWT.BORDER ); //$NON-NLS-1$
      m_id.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
      m_id.addKeyListener( keyListnerEnter() );
    }

    toolkit.createLabel( parent, Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.3" ) ); //$NON-NLS-1$
    m_name = toolkit.createText( parent, "", SWT.SINGLE | SWT.BORDER ); //$NON-NLS-1$
    m_name.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
    m_name.addKeyListener( keyListnerEnter() );

    toolkit.createLabel( parent, Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.8" ) ); //$NON-NLS-1$
    toolkit.createLabel( parent, Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.9" ) ); //$NON-NLS-1$

    m_posX = toolkit.createText( parent, "", SWT.SINGLE | SWT.BORDER ); //$NON-NLS-1$
    m_posX.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
    m_posX.addKeyListener( keyListnerEnter() );

    m_posY = toolkit.createText( parent, "", SWT.SINGLE | SWT.BORDER ); //$NON-NLS-1$
    m_posY.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
    m_posY.addKeyListener( keyListnerEnter() );
  }

  /**
   * provides the check for activated 1d2d plugin or context. otherwise the search for result id will not be shown
   */
  private boolean isIn1d2dModule( )
  {
    boolean is1d2dModule = false;
    try
    {
      String lTmp = ""; //$NON-NLS-1$
      if( PlatformUI.isWorkbenchRunning() )
      {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final IHandlerService service = (IHandlerService)workbench.getService( IHandlerService.class );
        final IEvaluationContext currentState = service.getCurrentState();
        lTmp = currentState.getVariable( "activeCaseDataProvider" ).toString(); //$NON-NLS-1$
      }
      is1d2dModule = lTmp.contains( "kalypso1d2d" ); //$NON-NLS-1$
    }
    catch( final Exception e )
    {
    }
    return is1d2dModule;
  }

  private KeyListener keyListnerEnter( )
  {
    return new KeyListener()
    {

      @Override
      public void keyReleased( final org.eclipse.swt.events.KeyEvent e )
      {
      }

      @Override
      public void keyPressed( final org.eclipse.swt.events.KeyEvent e )
      {
        if( e.keyCode == 16777296 || e.character == '\n' || e.character == '\r' ) // KeyEvent.VK_ENTER )
          doSearchOperation();
      }
    };
  }

  void doSearchOperation( )
  {
    m_boolIsSimple = checkInputs();

    final IKalypsoTheme[] themes = m_findElementMapWidget.getCurrentThemes();

    if( themes != null )
    {
      for( final IKalypsoTheme lTheme : themes )
      {
        if( lTheme instanceof IKalypsoCascadingTheme )
        {
          final IKalypsoCascadingTheme lThemes = (IKalypsoCascadingTheme)lTheme;
          for( int i = 0; i < lThemes.getAllThemes().length; i++ )
          {
            try
            {
              findInTheme( lThemes.getAllThemes()[i] );
            }
            catch( final Exception e )
            {
            }

            if( m_boolFound && m_boolIsSimple )
              break;
          }
        }
        else
        {
          findInTheme( lTheme );
          if( m_boolFound && m_boolIsSimple )
            break;
        }
      }
    }

    try
    {
      showFound();
    }
    catch( final ExecutionException e1 )
    {
      e1.printStackTrace();
    }
  }

  private boolean checkInputs( )
  {
    int lIntCountInputs = 0;
    if( m_gmlId != null )
    {
      m_gmlId.setText( m_gmlId.getText().trim() );
      if( m_gmlId.getText().contains( m_wildCChar ) )
      {
        m_boolIsSimple = false;
      }
      if( !"".equals( m_gmlId.getText() ) ) //$NON-NLS-1$
      {
        lIntCountInputs++;
      }
    }
    if( m_id != null )
    {
      m_id.setText( m_id.getText().trim() );
      if( m_id.getText().contains( m_wildCChar ) )
      {
        m_boolIsSimple = false;
      }
      if( !"".equals( m_id.getText() ) ) //$NON-NLS-1$
      {
        lIntCountInputs++;
      }
    }
    if( m_name != null )
    {
      m_name.setText( m_name.getText().trim() );
      if( m_name.getText().contains( m_wildCChar ) )
      {
        m_boolIsSimple = false;
      }
      if( !"".equals( m_name.getText() ) ) //$NON-NLS-1$
      {
        lIntCountInputs++;
      }
    }

    if( m_posX != null && m_posY != null )
    {
      m_posX.setText( m_posX.getText().trim() );
      m_posY.setText( m_posY.getText().trim() );
      if( m_posX.getText().contains( m_wildCChar ) || m_posY.getText().contains( m_wildCChar ) )
      {
        throw new NumberFormatException( "Invalid Double!" ); //$NON-NLS-1$
      }
      if( !"".equals( m_posX.getText() ) && !"".equals( m_posY.getText() ) ) //$NON-NLS-1$ //$NON-NLS-2$
      {
        lIntCountInputs++;
      }
    }
    return m_boolIsSimple ? lIntCountInputs > 0 : false;
  }

  private void findInTheme( final IKalypsoTheme lTheme )
  {
    if( lTheme instanceof IKalypsoFeatureTheme )
    {
      final IKalypsoFeatureTheme lActTheme = (IKalypsoFeatureTheme)lTheme;
      final FeatureList featureList = lActTheme.getFeatureList();
      m_boolFound = findFeature( featureList );
    }
  }

  private boolean findFeature( final FeatureList featureList )
  {
    int lIntCountFounds = 0;

    if( m_gmlId != null && !"".equals( m_gmlId.getText() ) ) //$NON-NLS-1$
    {
      m_feature = findFeatureForGmlId( m_gmlId.getText().toLowerCase(), featureList );
      m_featureList.add( m_feature );
      lIntCountFounds += m_feature == null ? 0 : 1;
    }
    if( m_id != null && !"".equals( m_id.getText() ) ) //$NON-NLS-1$
    {
      m_feature = findResultFeatureForPropertyId( m_resultIdQName, m_id.getText().toLowerCase(), featureList );
      m_featureList.add( m_feature );
      lIntCountFounds += m_feature == null ? 0 : 1;
    }
    if( m_name != null && !"".equals( m_name.getText() ) ) //$NON-NLS-1$
    {
      m_feature = findResultFeatureForPropertyId( m_nameQName, m_name.getText().toLowerCase(), featureList );
      m_featureList.add( m_feature );
      lIntCountFounds += m_feature == null ? 0 : 1;
    }
    if( m_posX != null && !"".equals( m_posX.getText() ) && m_posY != null && !"".equals( m_posY.getText() ) ) //$NON-NLS-1$ //$NON-NLS-2$
    {
      m_feature = null;
      m_boolFound = true;
      try
      {
        showFound();
      }
      catch( final ExecutionException e )
      {
        e.printStackTrace();
      }
    }

    return lIntCountFounds > 0 ? true : false;
  }

  private Feature findFeatureForGmlId( final String gmlId, final FeatureList featureList )
  {
    Feature lFeature = m_mapCacheFound.get( gmlId );
    if( lFeature != null )
    {
      return lFeature;
    }
    for( final Iterator<Feature> iterator = featureList.iterator(); iterator.hasNext(); )
    {
      lFeature = iterator.next();
      if( checkEquals( lFeature.getId().toLowerCase(), gmlId ) )
      {
        m_mapCacheFound.put( gmlId, lFeature );
        if( m_boolIsSimple )
          return lFeature;

        m_featureList.add( lFeature );
        m_feature = lFeature;
      }
    }
    return null;
  }

  private Feature findResultFeatureForPropertyId( final QName propertyName, final String propertyValue, final FeatureList featureList )
  {
    Feature lFeature = m_mapCacheFound.get( propertyName + propertyValue );
    if( lFeature != null )
      return lFeature;

    for( final Iterator<Feature> iterator = featureList.iterator(); iterator.hasNext(); lFeature = iterator.next() )
    {
      Object lPropertyValue = null;
      try
      {
        lPropertyValue = lFeature.getProperty( propertyName );
      }
      catch( final Exception e )
      {
        continue;
      }
      // FIXME: can never work work gml:name (its a list...)
      if( checkEquals( ("" + lPropertyValue).toLowerCase(), propertyValue ) ) //$NON-NLS-1$
      {
        m_mapCacheFound.put( propertyName + propertyValue, lFeature );

        if( m_boolIsSimple )
          return lFeature;

        m_featureList.add( lFeature );
        m_feature = lFeature;
      }
    }
    return null;
  }

  private boolean checkEquals( final String idToCheck, final String pIdPattern )
  {
    if( !pIdPattern.contains( m_wildCChar ) )
    {
      return pIdPattern.toLowerCase().equals( idToCheck.toLowerCase() );
    }
    String idPattern = pIdPattern;
    if( idPattern.startsWith( "*" ) ) //$NON-NLS-1$
    {
      idPattern = idPattern.substring( 1 );
    }
    final StringTokenizer lStrTokenizer = new StringTokenizer( idPattern, m_wildCChar );
    String lStrRest = idToCheck;
    while( lStrTokenizer.hasMoreTokens() )
    {
      final String lStrToken = lStrTokenizer.nextToken();
      final int indexOfToken = lStrRest.indexOf( lStrToken );
      if( indexOfToken > -1 )
      {
        lStrRest = lStrRest.substring( indexOfToken + lStrToken.length() );
      }
      else
      {
        return false;
      }
    }
    return true;
  }

  /**
   * This function changes the extent of the map panel, so that it centers the centroid if the first geometry of the
   * given feature or to the X,Y position.
   */
  private void showFound( ) throws ExecutionException
  {
    GM_Point centroid = null;
    if( m_feature == null && m_boolFound || m_posX != null && m_posY != null && !"".equals( m_posX.getText() ) && !"".equals( m_posY.getText() ) ) //$NON-NLS-1$ //$NON-NLS-2$
    {
      centroid = GeometryFactory.createGM_Point( NumberUtils.parseQuietDouble( m_posX.getText() ), NumberUtils.parseQuietDouble( m_posY.getText() ), m_defaultCrs );
    }
    else
    {
      if( m_feature == null )
        return;
      final GM_Object[] geometries = m_feature.getGeometryPropertyValues();

      if( geometries.length == 0 )
        return;

      final GM_Object geometry = geometries[0];
      centroid = geometry.getCentroid();
    }

    final IMapPanel panel = m_findElementMapWidget.getPanel();
    if( panel == null )
      return;

    final GM_Envelope boundingBox = panel.getBoundingBox();
    if( boundingBox == null )
      return;

    /* Get the new paned bounding box to the centroid of the geometry. */
    final GM_Envelope paned = boundingBox.getPaned( centroid );
    /* Finally set the bounding box. */
    MapHandlerUtils.postMapCommandChecked( panel, new ChangeExtentCommand( panel, paned ), null );
    panel.repaintMap();
  }

  void clear( )
  {
    m_feature = null;
    m_featureList.clear();

    m_featureList.clear();
    m_mapCacheFound.clear();
  }

  Set<Feature> getFeatures( )
  {
    return m_featureList;
  }

  Feature getFeature( )
  {
    return m_feature;
  }
}