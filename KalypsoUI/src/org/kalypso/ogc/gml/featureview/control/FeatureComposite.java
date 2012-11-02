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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.i18n.ITranslator;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.featureview.maker.FeatureviewTypeWithContext;
import org.kalypso.ogc.gml.featureview.maker.IFeatureviewFactory;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.template.featureview.ControlType;
import org.kalypso.template.featureview.FeatureviewType;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Gernot Belger
 */
public class FeatureComposite extends AbstractFeatureControl implements IFeatureChangeListener, ModifyListener, IFeatureComposite
{
  /**
   * The flag, indicating, if the green hook should be displayed.
   */
  private boolean m_showOk = false;

  private final Collection<FeatureControlBuilder> m_controls = new ArrayList<>();

  private final Collection<ModifyListener> m_modifyListeners = new ArrayList<>( 5 );

  private Control m_control = null;

  private final IFeatureSelectionManager m_selectionManager;

  private FormToolkit m_formToolkit = null;

  private final IFeatureviewFactory m_featureviewFactory;

  private FeatureViewTranslator m_translator;

  /**
   * Constructs the FeatureComposite.
   * 
   * @param feature
   *          If you want to add a feature directly at instantiation time, provide it here, otherwise leave it null.
   * @param selectionManager
   *          A selection manager, which provides functionality for adding and removing a feature from an selection and
   *          it handels the registration of listerners and so on. It has to implement IFeatureSelectionManager. You can
   *          get a default one for the features here
   *          <strong>KalypsoCorePlugin.getDefault().getSelectionManager()</strong>.
   * @param featureviewFactory
   *          A factory which delivers feature-view-templates (e.g. FeatureviewHelper).
   */
  public FeatureComposite( final Feature feature, final IFeatureSelectionManager selectionManager, final IFeatureviewFactory featureviewFactory )
  {
    super( feature, null );

    m_selectionManager = selectionManager;
    m_featureviewFactory = featureviewFactory;
  }

  @Override
  public void updateControl( )
  {
    for( final FeatureControlBuilder control : m_controls )
      control.updateControl();

    if( m_control != null && !m_control.isDisposed() && m_control instanceof Composite )
      ((Composite)m_control).layout();
  }

  @Override
  public void dispose( )
  {
    disposeControl();

    m_modifyListeners.clear();
  }

  @Override
  public boolean isValid( )
  {
    for( final FeatureControlBuilder control : m_controls )
    {
      final IFeatureControl fc = control.getFeatureControl();
      if( !fc.isValid() )
        return false;
    }

    return true;
  }

  public Control createControl( final Composite parent, final int defaultStyle, final IFeatureType ft )
  {
    final FeatureviewTypeWithContext view = m_featureviewFactory.get( ft, getFeature() );

    m_translator = new FeatureViewTranslator( m_featureviewFactory.getTranslator( view, null ) );

    // TODO: dubious we shoudn't need to adapt the parent, that should already have been done by the calling code
    if( m_formToolkit != null )
      m_formToolkit.adapt( parent );

    m_control = createControl( parent, defaultStyle, view.getView(), m_translator );

    /* If a toolkit is set, use it. */
    if( m_formToolkit != null )
      m_formToolkit.adapt( m_control, true, true );

    return m_control;
  }

  @Override
  public final Control createControl( final Composite parent, final int defaultStyle )
  {
    try
    {
      return createControl( parent, defaultStyle, getFeature().getFeatureType() );
    }
    catch( final Throwable t )
    {
      final org.eclipse.swt.widgets.Text text = new org.eclipse.swt.widgets.Text( parent, SWT.MULTI );
      text.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
      text.setEditable( false );
      final String trace = ExceptionUtils.getStackTrace( t );
      text.setText( trace );
      return text;
    }
  }

  public Control createControl( final Composite parent, final int defaultStyle, final ControlType controlType, final ITranslator translator )
  {
    final Feature feature = getFeature();

    final FeatureControlBuilder controlBuilder = new FeatureControlBuilder( this, controlType, feature, translator );

    final Control control = controlBuilder.create( m_formToolkit, parent, defaultStyle );

    m_controls.add( controlBuilder );

    return control;
  }

  @Override
  public void addModifyListener( final ModifyListener l )
  {
    m_modifyListeners.add( l );
  }

  @Override
  public void removeModifyListener( final ModifyListener l )
  {
    m_modifyListeners.remove( this );
  }

  @Override
  public void setFeature( final Feature feature )
  {
    super.setFeature( feature );

    for( final FeatureControlBuilder control : m_controls )
      control.setFeature( feature );
  }

  public void disposeControl( )
  {
    for( final FeatureControlBuilder control : m_controls )
      control.dispose();
    m_controls.clear();

    if( m_control != null )
    {
      m_control.dispose();
      m_control = null;
    }
  }

  public Control getControl( )
  {
    return m_control;
  }

  @Override
  public void featureChanged( final ICommand changeCommand )
  {
    fireFeatureChange( changeCommand );
  }

  @Override
  public void openFeatureRequested( final Feature feature, final IPropertyType ftp )
  {
    fireOpenFeatureRequested( feature, ftp );
  }

  @Override
  public void modifyText( final ModifyEvent e )
  {
    final ModifyListener[] listeners = m_modifyListeners.toArray( new ModifyListener[m_modifyListeners.size()] );
    for( final ModifyListener listener : listeners )
      SafeRunnable.run( new SafeRunnable()
      {
        @Override
        public void run( ) throws Exception
        {
          listener.modifyText( e );
        }
      } );
  }

  /** Traverse the tree feature controls adds all found feature view types to the given collection */
  public void collectViewTypes( final Collection<FeatureviewType> types )
  {
    final Feature feature = getFeature();
    if( feature == null )
      return;

    final FeatureviewTypeWithContext type = m_featureviewFactory.get( feature.getFeatureType(), feature );
    types.add( type.getView() );

    for( final FeatureControlBuilder controlToolkit : m_controls )
    {
      final IFeatureControl control = controlToolkit.getFeatureControl();

      if( control instanceof FeatureComposite )
        ((FeatureComposite)control).collectViewTypes( types );
      else if( control instanceof SubFeatureControl )
      {
        final IFeatureControl fc = ((SubFeatureControl)control).getFeatureControl();
        if( fc instanceof FeatureComposite )
          ((FeatureComposite)fc).collectViewTypes( types );
      }
    }
  }

  @Override
  public IFeatureviewFactory getFeatureviewFactory( )
  {
    return m_featureviewFactory;
  }

  @Override
  public IFeatureSelectionManager getSelectionManager( )
  {
    return m_selectionManager;
  }

  @Override
  public FormToolkit getFormToolkit( )
  {
    return m_formToolkit;
  }

  public void setFormToolkit( final FormToolkit formToolkit )
  {
    m_formToolkit = formToolkit;
  }

  /**
   * This function sets, if the green hook on a ok validated feature should be displayed. The default is false. This
   * flag has only an effect, if the validator label is activated.
   * 
   * @param showOk
   *          The flag, indicating, if the green hook should be displayed.
   */
  public void setShowOk( final boolean showOk )
  {
    m_showOk = showOk;
  }

  /**
   * This function returns the flag for displaying the green hook on a ok validated feature.
   * 
   * @return The flag, indicating, if the green hook should be displayed.
   */
  @Override
  public boolean isShowOk( )
  {
    return m_showOk;
  }

  @Override
  public URL getFeatureviewContext( )
  {
    final Feature feature = getFeature();
    if( feature == null )
      return null;

    final IFeatureType featureType = feature.getFeatureType();

    final FeatureviewTypeWithContext featureviewWithContext = m_featureviewFactory.get( featureType, feature );
    if( featureviewWithContext == null )
      return null;

    return featureviewWithContext.getContext();
  }

  @Override
  public ITranslator getTranslator( )
  {
    return m_translator;
  }

  boolean hasToolkit( )
  {
    return m_formToolkit != null;
  }
}