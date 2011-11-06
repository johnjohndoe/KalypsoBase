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
package org.kalypso.ui.editor.mapeditor.views;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.kalypso.contribs.eclipse.core.runtime.AdapterUtils;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.IKalypsoUserStyle;
import org.kalypso.ogc.gml.outline.nodes.FeatureTypeStyleNode;
import org.kalypso.ogc.gml.outline.nodes.IThemeNode;
import org.kalypso.ogc.gml.outline.nodes.UserStyleNode;
import org.kalypso.ui.editor.styleeditor.SLDComposite;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.Rule;

public class StyleEditorViewPart extends ViewPart implements ISelectionChangedListener
{
  public static String ID = "org.kalypso.ui.editor.mapeditor.views.styleeditor"; //$NON-NLS-1$

  private ISelectionProvider m_gmop = null;

  private SLDComposite m_sldComposite = null;

  public void setSelectionChangedProvider( final ISelectionProvider selectionProvider )
  {
    if( m_gmop == null && selectionProvider == null )
    {
      /* REMARK: special case, happens, if map is closed */
      m_sldComposite.setKalypsoStyle( null, null );
      return;
    }

    if( m_gmop == selectionProvider )
      return;

    if( m_gmop != null )
    {
      m_gmop.removeSelectionChangedListener( this );
      selectionChanged( new SelectionChangedEvent( m_gmop, StructuredSelection.EMPTY ) );
    }

    m_gmop = selectionProvider;

    if( m_gmop != null )
    {
      m_gmop.addSelectionChangedListener( this );

      selectionChanged( new SelectionChangedEvent( m_gmop, m_gmop.getSelection() ) );
    }
  }

  /**
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  @Override
  public void dispose( )
  {
    super.dispose();

    if( m_gmop != null )
      m_gmop.removeSelectionChangedListener( this );

    if( m_sldComposite != null )
      m_sldComposite.dispose();
  }

  /**
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl( final Composite parent )
  {
    m_sldComposite = new SLDComposite( parent );
  }

  /**
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  @Override
  public void setFocus( )
  {
    if( m_sldComposite != null )
      m_sldComposite.setFocus();
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
   */
  @Override
  public void selectionChanged( final SelectionChangedEvent event )
  {
    final Object o = ((IStructuredSelection) event.getSelection()).getFirstElement();

    if( !(o instanceof IThemeNode) )
    {
      m_sldComposite.setKalypsoStyle( null, null );
      return;
    }

    final IThemeNode node = (IThemeNode) o;
    final IKalypsoTheme theme = findTheme( node );
    if( !(theme instanceof IKalypsoFeatureTheme) )
    {
      m_sldComposite.setKalypsoStyle( null, null );
      return;
    }

    final IKalypsoFeatureTheme featureTheme = ((IKalypsoFeatureTheme) theme);
    chooseStyle( featureTheme, node );
  }

  private void chooseStyle( final IKalypsoFeatureTheme featureTheme, final IThemeNode node )
  {
    final IFeatureType featureType = featureTheme == null ? null : featureTheme.getFeatureType();
    final IKalypsoTheme theme = AdapterUtils.getAdapter( node, IKalypsoTheme.class );

    if( node instanceof UserStyleNode )
    {
      final IKalypsoUserStyle kalypsoStyle = ((UserStyleNode) node).getStyle();
      m_sldComposite.setKalypsoStyle( kalypsoStyle, featureType );
    }
    else if( node.getElement() instanceof FeatureTypeStyle )
    {
      final FeatureTypeStyle fts = (FeatureTypeStyle) node.getElement();
      if( fts instanceof IKalypsoStyle )
        m_sldComposite.setKalypsoStyle( (IKalypsoStyle) fts, featureType );
      else
      {
        final IThemeNode parentNode = node.getParent();
        chooseStyle( featureTheme, parentNode );
      }
    }
    else if( node.getElement() instanceof Rule )
    {
      final Rule indexRule = (Rule) node.getElement();
      final FeatureTypeStyleNode ftsNode = (FeatureTypeStyleNode) node.getParent();
      final FeatureTypeStyle fts = ftsNode.getStyle();
      final Rule[] rules = fts.getRules();
      int index = -1;
      if( indexRule != null )
      {
        for( int i = 0; i < rules.length; i++ )
        {
          if( rules[i] == indexRule )
          {
            index = i;
            break;
          }
        }
      }

      final IKalypsoStyle style = findStyle( ftsNode );
      if( style == null )
        m_sldComposite.setKalypsoStyle( null, null );
      else
        m_sldComposite.setKalypsoStyle( style, featureType, index );
    }
    else if( theme instanceof IKalypsoFeatureTheme )
    {
      final IKalypsoFeatureTheme nodeTheme = (IKalypsoFeatureTheme) theme;
      // Reset style-editor, but the styles are not unique, so do not set anything
      final IFeatureType otherType = nodeTheme == null ? null : nodeTheme.getFeatureType();
      final IKalypsoStyle[] styles = nodeTheme.getStyles();
      if( styles != null && styles.length > 0 )
        m_sldComposite.setKalypsoStyle( styles[0], otherType );
      else
        m_sldComposite.setKalypsoStyle( null, null );
    }
    else
      m_sldComposite.setKalypsoStyle( null, null );
  }

  private IKalypsoStyle findStyle( final IThemeNode node )
  {
    if( node == null )
      return null;

    final Object element = node.getElement();
    if( element instanceof IKalypsoStyle )
      return (IKalypsoStyle) element;

    return findStyle( node.getParent() );
  }

  private IKalypsoTheme findTheme( final IThemeNode node )
  {
    if( node == null )
      return null;

    final Object element = node.getElement();
    if( element instanceof IKalypsoTheme )
      return (IKalypsoTheme) element;

    return findTheme( node.getParent() );
  }
}