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
import java.util.LinkedList;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.kalypso.commons.command.ICommand;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.ogc.gml.command.ChangeFeaturesCommand;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.featureview.dialog.IFeatureDialog;
import org.kalypso.ogc.gml.featureview.dialog.NotImplementedFeatureDialog;
import org.kalypso.ogc.gml.gui.GuiTypeRegistrySingleton;
import org.kalypso.ogc.gml.gui.IFeatureDialogFactory;
import org.kalypso.ogc.gml.gui.IGuiTypeHandler;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.event.ModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEventListener;

/**
 * This control behaves in two ways: if the type of the proeprty to edit is simple, it opens a fitting dialog to edit
 * the property. If the property is a feature, it just informs its listeners, that it whichs to open that feature.
 * 
 * @author Gernot Belger
 */
public class ButtonFeatureControl extends AbstractFeatureControl implements ModellEventListener
{
  private Button m_button;

  private IFeatureDialog m_dialog = null;

  private final Collection<ModifyListener> m_modifyListener = new ArrayList<>();

  private final IFeatureChangeListener m_listener;

  public ButtonFeatureControl( final Feature feature, final IPropertyType ftp )
  {
    super( feature, ftp );

    m_listener = new IFeatureChangeListener()
    {
      @Override
      public void featureChanged( final ICommand changeCommand )
      {
        fireFeatureChange( changeCommand );
      }

      @Override
      public void openFeatureRequested( final Feature featureToOpen, final IPropertyType ftpToOpen )
      {
        fireOpenFeatureRequested( featureToOpen, ftpToOpen );
      }
    };
  }

  public static IFeatureDialog chooseDialog( final Feature feature, final IPropertyType pt, final IFeatureChangeListener listener )
  {
    // TODO: should never happen remove if fixed
    if( feature == null )
      return new NotImplementedFeatureDialog();

    final IFeatureDialogFactory factory = findDialogFactory( listener, pt );
    if( factory == null )
      return new NotImplementedFeatureDialog();

    return factory.createFeatureDialog( feature, pt );
  }

  public static IFeatureDialogFactory findDialogFactory( final IFeatureChangeListener listener, final IPropertyType pt )
  {
    if( pt instanceof IValuePropertyType )
    {
      final ITypeRegistry<IGuiTypeHandler> typeRegistry = GuiTypeRegistrySingleton.getTypeRegistry();
      return typeRegistry.getTypeHandlerFor( pt );
    }

    if( pt instanceof IRelationType )
      return new RelationFeatureDialogFactory( listener );

    return null;
  }

  @Override
  public void dispose( )
  {
    if( !(m_button.isDisposed()) )
      m_button.dispose();
  }

  @Override
  public Control createControl( final Composite parent, final int style )
  {
    m_button = new Button( parent, style );
    m_button.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        buttonPressed();
      }
    } );

    updateControl();

    return m_button;
  }

  protected void buttonPressed( )
  {
    if( m_dialog.open( m_button.getShell() ) == Window.OK )
    {
      final Collection<FeatureChange> c = new LinkedList<>();
      m_dialog.collectChanges( c );
      final FeatureChange[] changes = c.toArray( new FeatureChange[c.size()] );
      fireFeatureChange( new ChangeFeaturesCommand( getFeature().getWorkspace(), changes ) );

      fireModfied();

      updateControl();
    }
  }

  private void fireModfied( )
  {
    for( final Object element : m_modifyListener )
    {
      final ModifyListener l = (ModifyListener)element;
      final Event event = new Event();
      event.widget = m_button;
      l.modifyText( new ModifyEvent( event ) );
    }
  }

  /**
   * Die ButtonControl ist immer valid
   * 
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
    m_modifyListener.add( l );
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureControl#removeModifyListener(org.eclipse.swt.events.ModifyListener)
   */
  @Override
  public void removeModifyListener( final ModifyListener l )
  {
    m_modifyListener.remove( l );
  }

  /**
   * @see org.kalypsodeegree.model.feature.event.ModellEventListener#onModellChange(org.kalypsodeegree.model.feature.event.ModellEvent)
   */
  @Override
  public void onModellChange( final ModellEvent modellEvent )
  {
    updateControl();
  }

  @Override
  public void updateControl( )
  {
    final Feature feature = getFeature();
    m_dialog = chooseDialog( feature, getFeatureTypeProperty(), m_listener );
    if( m_dialog == null )
      return;

    m_button.setText( m_dialog.getLabel() );

    m_button.setEnabled( feature != null );
  }
}