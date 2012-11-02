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
package org.kalypso.ogc.gml.featureview.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.commons.command.ICommand;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Gernot Belger
 */
public abstract class AbstractFeatureControl implements IFeatureControl
{
  private final List<ModifyListener> m_listeners = new ArrayList<>( 5 );

  private Feature m_feature;

  private final IPropertyType m_ftp;

  private final Collection<IFeatureChangeListener> m_changelisteners = new ArrayList<>();

  public AbstractFeatureControl( final IPropertyType ftp )
  {
    this( null, ftp );
  }

  public AbstractFeatureControl( final Feature feature, final IPropertyType ftp )
  {
    m_feature = feature;
    m_ftp = ftp;
  }

  @Override
  public void dispose( )
  {
    m_changelisteners.clear();
  }

  @Override
  public Control createControl( final FormToolkit toolkit, final Composite parent, final int style )
  {
    final Control control = createControl( parent, style );
    applyToolkit( toolkit, control );
    return control;
  }

  @SuppressWarnings( "unused" )
  protected Control createControl( final Composite parent, final int style )
  {
    // Implementors need either to overwrite #createControl( toolkit.. ) or overwrite this method.
    throw new IllegalStateException();
  }

  protected void applyToolkit( final FormToolkit toolkit, final Control control )
  {
    if( toolkit == null )
      return;

    if( control instanceof Composite )
    {
      final Composite panel = (Composite)control;
      toolkit.adapt( panel );

      final Control[] children = panel.getChildren();
      for( final Control child : children )
        applyToolkit( toolkit, child );
    }
    else
      toolkit.adapt( control, true, true );
  }

  @Override
  public Feature getFeature( )
  {
    return m_feature;
  }

  @Override
  public void setFeature( final Feature feature )
  {
    m_feature = feature;
  }

  @Override
  public IPropertyType getFeatureTypeProperty( )
  {
    return m_ftp;
  }

  @Override
  public void addModifyListener( final ModifyListener l )
  {
    m_listeners.add( l );
  }

  @Override
  public void removeModifyListener( final ModifyListener l )
  {
    m_listeners.remove( l );
  }

  protected void fireModifyText( final ModifyEvent e )
  {
    final ModifyListener[] listeners = m_listeners.toArray( new ModifyListener[m_listeners.size()] );
    for( final ModifyListener listener : listeners )
      listener.modifyText( e );
  }

  @Override
  public final void addChangeListener( final IFeatureChangeListener l )
  {
    m_changelisteners.add( l );
  }

  @Override
  public final void removeChangeListener( final IFeatureChangeListener l )
  {
    m_changelisteners.remove( l );
  }

  protected final void fireFeatureChange( final ICommand changeCommand )
  {
    // do nothing if there are no changes
    if( changeCommand == null )
      return;

    final IFeatureChangeListener[] listeners = m_changelisteners.toArray( new IFeatureChangeListener[m_changelisteners.size()] );
    for( final IFeatureChangeListener listener : listeners )
    {
      SafeRunner.run( new SafeRunnable()
      {
        @Override
        public void run( ) throws Exception
        {
          listener.featureChanged( changeCommand );
        }
      } );
    }
  }

  protected final void fireOpenFeatureRequested( final Feature feature, final IPropertyType ftp )
  {
    final IFeatureChangeListener[] listeners = m_changelisteners.toArray( new IFeatureChangeListener[m_changelisteners.size()] );
    for( final IFeatureChangeListener listener : listeners )
    {
      SafeRunner.run( new SafeRunnable()
      {
        @Override
        public void run( ) throws Exception
        {
          listener.openFeatureRequested( feature, ftp );
        }
      } );
    }
  }
}
