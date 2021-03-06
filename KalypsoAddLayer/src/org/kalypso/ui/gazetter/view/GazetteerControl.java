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
package org.kalypso.ui.gazetter.view;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.ogc.wfs.WFSClient;
import org.kalypso.ui.KalypsoAddLayerPlugin;
import org.kalypso.ui.i18n.Messages;
import org.kalypso.view.gazetter.GazetterLocationType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * @author doemming
 */
public class GazetteerControl implements ISelectionChangedListener, IStructuredContentProvider
{
  private final GazetterLocationType m_gazetteerLocation;

  private final GazetterView m_view;

  final URL m_baseURL;

  ComboViewer m_comboViewer;

  Control m_button;

  public GazetteerControl( final GazetterLocationType gazetteerLocation, final URL baseURL, final GazetterView view )
  {
    m_gazetteerLocation = gazetteerLocation;
    m_baseURL = baseURL;
    m_view = view;
  }

  public void init( final GazetterLocationType parent, final Feature selectedFeature, final String query )
  {
    initChilds();
    if( parent != null && selectedFeature == null )
    {
      // parent exists but nothing is selected...
      setEnable( false );

      return;
    }
    final QName featureTypeToLoad = m_gazetteerLocation.getFeatureType();
    final String filter;
    final Integer maxFeatures = 100; //$NON-NLS-1$

    // no parent and nothing seleced
    if( parent == null && selectedFeature == null )
    {
      final QName labelProperty = m_gazetteerLocation.getLabelProperty();
      final StringBuffer buffer = new StringBuffer();
      // TODO replace all non xml-string characters... <
      if( query != null && query.length() > 0 )
      {
        final StringBuffer queryBuffer = new StringBuffer( query );
        if( query.indexOf( "*" ) < 0 ) //$NON-NLS-1$
          queryBuffer.append( "*" ); //$NON-NLS-1$
        buffer.append( "<ogc:Filter>\n" ); //$NON-NLS-1$
        buffer.append( "<ogc:PropertyIsLike wildCard=\"*\" singleChar=\"#\" escape=\"!\">\n" ); //$NON-NLS-1$
        buffer.append( "<ogc:PropertyName>" + labelProperty.getLocalPart() + "</ogc:PropertyName>\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        buffer.append( "<ogc:Literal>" + queryBuffer.toString() + "</ogc:Literal>\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        buffer.append( "</ogc:PropertyIsLike>\n" ); //$NON-NLS-1$
        buffer.append( "</ogc:Filter>\n" ); //$NON-NLS-1$
      }
      filter = buffer.toString();

    }
    else if( parent != null && selectedFeature != null )
    {
      // build filter
      final QName spacialIdentifier = parent.getSpacialIdentifierProperty();
      final Object value = selectedFeature.getProperty( spacialIdentifier );
      if( value == null )
        return;
      // TODO better use binding
      final String valueAsString = value.toString();
      final StringBuffer buffer = new StringBuffer();
      buffer.append( "<ogc:Filter>\n" ); //$NON-NLS-1$
      // buffer.append( "<ogc:PropertyIsEqualTo wildCard=\"*\" singleChar=\"#\" escape=\"!\">\n" );
      buffer.append( "<ogc:PropertyIsEqualTo>\n" ); //$NON-NLS-1$
      buffer.append( "<ogc:PropertyName>parents/" + spacialIdentifier.getLocalPart() + "</ogc:PropertyName>\n" ); //$NON-NLS-1$ //$NON-NLS-2$
      buffer.append( "<ogc:Literal>" + valueAsString + "</ogc:Literal>\n" ); //$NON-NLS-1$ //$NON-NLS-2$
      buffer.append( "</ogc:PropertyIsEqualTo>\n" ); //$NON-NLS-1$
      buffer.append( "</ogc:Filter>\n" ); //$NON-NLS-1$
      filter = buffer.toString();
    }
    else
      return; // never happens
    final Job job = new Job( Messages.getString( "org.kalypso.ui.gazetter.view.GazetteerControl.0" ) + featureTypeToLoad.getLocalPart() ) //$NON-NLS-1$
    {
      @Override
      protected IStatus run( final IProgressMonitor monitor )
      {
        try
        {
          final WFSClient wfsClient = new WFSClient( m_baseURL );
          final GMLWorkspace workspace = wfsClient.operationGetFeature( featureTypeToLoad, filter, maxFeatures );

          setContent( workspace );
          setEnable( true );
        }
        catch( final Exception e )
        {
          e.printStackTrace();
          setContent( null );
          setEnable( false );
          return new Status( IStatus.ERROR, KalypsoAddLayerPlugin.getId(), Messages.getString( "org.kalypso.ui.gazetter.view.GazetteerControl.1" ) ); //$NON-NLS-1$
        }
        return Status.OK_STATUS;
      }

    };
    if( filter.length() > 0 || !m_gazetteerLocation.isDoTextSearch() )
      job.schedule();
    else
    {
      setContent( null );
      setEnable( false );
    }

  }

  void setEnable( final boolean enable )
  {
    if( m_comboViewer != null && !m_comboViewer.getControl().isDisposed() )
    {
      m_comboViewer.getControl().getDisplay().asyncExec( new Runnable()
      {

        @Override
        public void run( )
        {
          if( m_comboViewer != null && !m_comboViewer.getControl().isDisposed() )
            m_comboViewer.getControl().setEnabled( enable );
          if( m_button != null && !m_button.isDisposed() )
            m_button.setEnabled( enable );
        }
      } );
    }

  }

  private void initChilds( )
  {

    final List<GazetterLocationType> gChildLocations = m_gazetteerLocation.getGazetterLocation();
    final Iterator<GazetterLocationType> iterator = gChildLocations.iterator();
    while( iterator.hasNext() )
    {
      final GazetterLocationType gChildLocation = iterator.next();
      final GazetteerControl control = m_view.getGControlForGLocation( gChildLocation );
      try
      {
        control.init( m_gazetteerLocation, null, null );
      }
      catch( final Exception e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  void setContent( final GMLWorkspace workspace )
  {
    if( m_comboViewer != null && !m_comboViewer.getControl().isDisposed() )
    {
      m_comboViewer.getControl().getDisplay().asyncExec( new Runnable()
      {

        @Override
        public void run( )
        {
          m_comboViewer.setInput( workspace );
          m_comboViewer.setSelection( new StructuredSelection( GazetteerConstants.NO_SELECTION_IN_COMBO ) );
        }
      } );
    }
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
   */
  @Override
  public void selectionChanged( final SelectionChangedEvent event )
  {
    // combo invoked
    // let childs update
    final Feature selectedFeature;
    final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
    final Object firstElement = selection.getFirstElement();
    if( firstElement instanceof Feature )
      selectedFeature = (Feature) firstElement;
    else
      selectedFeature = null;
    final List<GazetterLocationType> gChildLocations = m_gazetteerLocation.getGazetterLocation();
    final Iterator<GazetterLocationType> iterator = gChildLocations.iterator();
    while( iterator.hasNext() )
    {
      final GazetterLocationType gChildLocation = iterator.next();
      final GazetteerControl gChildControl = m_view.getGControlForGLocation( gChildLocation );
      gChildControl.init( m_gazetteerLocation, selectedFeature, null );
    }
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  @Override
  public Object[] getElements( final Object inputElement )
  {
    if( inputElement instanceof GMLWorkspace )
    {
      final GMLWorkspace workspace = (GMLWorkspace) inputElement;
      final QName featureType = m_gazetteerLocation.getFeatureType();
      final IFeatureType ft = GMLSchemaUtilities.getFeatureTypeQuiet( featureType );
      final Feature[] features = FeatureHelper.getFeaturesWithType( workspace, ft );
      final Object result[] = new Object[features.length + 1];
      result[0] = GazetteerConstants.NO_SELECTION_IN_COMBO;
      for( int i = 1; i < result.length; i++ )
        result[i] = features[i - 1];
      return result;
    }
    else
      return new String[] { Messages.getString( "org.kalypso.ui.gazetter.view.GazetteerControl.2" ) }; //$NON-NLS-1$
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    // nothing to do
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
   *      java.lang.Object)
   */
  @Override
  public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
  {
    // TODO Auto-generated method stub
  }

  public void setViewer( final ComboViewer comboViewer, final Button control )
  {
    m_comboViewer = comboViewer;
    m_button = control;
  }
}
