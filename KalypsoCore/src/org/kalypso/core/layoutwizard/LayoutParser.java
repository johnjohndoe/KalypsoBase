/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.core.layoutwizard;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.sourceforge.projects.kalypsobase.layout.AbstractContainerType;
import net.sourceforge.projects.kalypsobase.layout.AbstractPartType;
import net.sourceforge.projects.kalypsobase.layout.Controller;
import net.sourceforge.projects.kalypsobase.layout.GridContainer;
import net.sourceforge.projects.kalypsobase.layout.Page;
import net.sourceforge.projects.kalypsobase.layout.TabFolder;
import net.sourceforge.projects.kalypsobase.layout.TabItem;
import net.sourceforge.projects.kalypsobase.swt.AbstractDataType;
import net.sourceforge.projects.kalypsobase.swt.GridData;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.layout.GridLayout;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.contribs.eclipse.swt.SWTUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.core.internal.layoutwizard.LayoutExtensions;
import org.kalypso.core.internal.layoutwizard.controller.ModificationLayoutController;
import org.kalypso.core.internal.layoutwizard.controller.SelectionLayoutController;
import org.kalypso.core.internal.layoutwizard.part.GridLayoutContainer;
import org.kalypso.core.internal.layoutwizard.part.SashConfiguration;
import org.kalypso.core.internal.layoutwizard.part.SashContainer;
import org.kalypso.core.internal.layoutwizard.part.StatusLayoutPart;
import org.kalypso.core.internal.layoutwizard.part.TabFolderContainer;
import org.kalypso.core.internal.layoutwizard.part.TabItemContainer;
import org.kalypso.core.internal.layoutwizard.part.ViewLayoutPart;

/**
 * @author Gernot Belger
 */
public class LayoutParser
{
  public static final Object TYPE_CONTROLLER_SELECTION = "selection"; //$NON-NLS-1$

  public static final Object TYPE_CONTROLLER_MODIFICATION = "modification"; //$NON-NLS-1$

  private final ILayoutPageContext m_defaultContext;

  private ILayoutPart m_layoutPart;

  private ILayoutController[] m_controllers = new ILayoutController[0];

  public LayoutParser( final ILayoutPageContext defaultContext )
  {
    m_defaultContext = defaultContext;
  }

  public void read( )
  {
    try
    {
      final URL location = LayoutFactory.findLayoutLocation( m_defaultContext );

      final Page page = readXml( location );

      final JAXBElement< ? extends AbstractPartType> abstractPart = page.getAbstractPart();

      m_layoutPart = buildLayout( abstractPart.getValue() );

      m_controllers = createControllers( page.getController() );
    }
    catch( final CoreException e )
    {
      m_layoutPart = new StatusLayoutPart( "rootStatus", m_defaultContext, e.getStatus() ); //$NON-NLS-1$
    }
  }

  private ILayoutPart buildLayout( final AbstractPartType xmlPart ) throws CoreException
  {
    final ILayoutPart layoutPart = createLayoutPart( xmlPart );
    layoutPart.setStyle( SWTUtilities.createStyleFromString( xmlPart.getStyle() ) );

    final JAXBElement< ? extends AbstractDataType> dataElement = xmlPart.getData();
    if( dataElement != null )
    {
      final AbstractDataType dataType = dataElement.getValue();
      layoutPart.setLayoutData( parseData( dataType ) );
    }

    return layoutPart;
  }

  private Page readXml( final URL location ) throws CoreException
  {
    try
    {
      final Unmarshaller unmarshaller = LayoutBinding.createLayoutUnmarshaller();
      return (Page) unmarshaller.unmarshal( location );
    }
    catch( final JAXBException e )
    {
      final String message = String.format( Messages.getString("LayoutParser_2") ); //$NON-NLS-1$
      final IStatus status = new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), message, e );
      throw new CoreException( status );
    }
  }

  private ILayoutPart createLayoutPart( final AbstractPartType part ) throws CoreException
  {
    if( part instanceof AbstractContainerType )
      return buildContainer( (AbstractContainerType) part );

    final ILayoutPart layoutPart = buildPart( part );
    return layoutPart;
  }

  /**
   * This function builds the layout part from the primary id. It will first check the extension point
   * 'layout#layoutPartFactory' for a layout part factory, which can create the layout part. If none could be found, it
   * will create a view layout part, which will check the view registry as well. If there is also no view with this
   * primary id registered, it will display an error.
   *
   * @param primaryId
   *          The first part of the complete id (e.g. gisMap from gisMap.1).
   * @param id
   *          The complete id of the layout part.
   * @return The layout part.
   */
  private ILayoutPart buildPart( final AbstractPartType part ) throws CoreException
  {
    final String id = part.getId();
    final String[] idSplit = id.split( "\\." ); //$NON-NLS-1$

    final String primaryId = idSplit[0];

    /* Get the context. */
    final ILayoutPageContext context = getContext( id );

    /* Get the layout part factory with the same id as the primary id of the layout part. */
    final ILayoutPartFactory layoutPartFactory = LayoutExtensions.getLayoutPartFactory( primaryId );
    if( layoutPartFactory != null )
      return layoutPartFactory.createLayoutPart( id, context );

    /* The view layout part checks the view registry or displays an error. */
    return new ViewLayoutPart( id, context, primaryId );
  }

  private ILayoutPageContext getContext( final String id )
  {
    final Arguments arguments = m_defaultContext.getArguments();
    final Arguments specificArgs = arguments.getArguments( id );
    if( specificArgs == null )
      return m_defaultContext;

    return LayoutFactory.createChildContext( m_defaultContext, specificArgs );
  }

  private ILayoutContainer buildContainer( final AbstractContainerType containerType ) throws CoreException
  {
    final ILayoutContainer container = createContainer( containerType );

    final List<JAXBElement< ? extends AbstractPartType>> partTypes = containerType.getAbstractPart();
    for( final JAXBElement< ? extends AbstractPartType> partElement : partTypes )
    {
      final AbstractPartType partType = partElement.getValue();
      final ILayoutPart childPart = buildLayout( partType );
      container.addChild( childPart );
    }

    return container;
  }

  private Object parseData( final AbstractDataType dataType )
  {
    if( dataType instanceof GridData )
    {
      final GridData gridType = (GridData) dataType;
      final int horizontalAlignment = SWTUtilities.createStyleFromString( gridType.getHorizontalAlignment() );
      final int verticalAlignment = SWTUtilities.createStyleFromString( gridType.getVerticalAlignment() );
      final int horizontalSpan = gridType.getHorizontalSpan();
      final int verticalSpan = gridType.getVerticalSpan();
      final boolean grabExcessHorizontalSpace = gridType.isGrabExcessHorizontalSpace();
      final boolean grabExcessVerticalSpace = gridType.isGrabExcessVerticalSpace();
      final int widthHint = gridType.getWidthHint();
      final int heightHint = gridType.getHeightHint();
      final int horizontalIndent = gridType.getHorizontalIndent();
      final int verticalIndent = gridType.getVerticalIndent();

      final org.eclipse.swt.layout.GridData gridData = new org.eclipse.swt.layout.GridData( horizontalAlignment, verticalAlignment, grabExcessHorizontalSpace, grabExcessVerticalSpace, horizontalSpan, verticalSpan );
      gridData.widthHint = widthHint;
      gridData.heightHint = heightHint;
      gridData.horizontalIndent = horizontalIndent;
      gridData.verticalIndent = verticalIndent;

      return gridData;
    }

    throw new UnsupportedOperationException( String.format( Messages.getString("LayoutParser_4"), dataType.getClass().getName() ) ); //$NON-NLS-1$
  }

  private ILayoutContainer createContainer( final AbstractContainerType containerType )
  {
    final String id = containerType.getId();
// final int style = SWTUtilities.createStyleFromString( containerType.getStyle() );

    if( containerType instanceof net.sourceforge.projects.kalypsobase.layout.SashContainer )
    {
      final net.sourceforge.projects.kalypsobase.layout.SashContainer sashContainer = (net.sourceforge.projects.kalypsobase.layout.SashContainer) containerType;
      final List<Integer> weights = sashContainer.getWeights();
      final AbstractPartType maximizedChild = (AbstractPartType) sashContainer.getMaximizedChild();
      final String maximizedChildId = maximizedChild == null ? null : maximizedChild.getId();

      final int[] sashWeights = ArrayUtils.toPrimitive( weights.toArray( new Integer[weights.size()] ) );
      return new SashContainer( id, new SashConfiguration( sashWeights, maximizedChildId ) );
    }

    if( containerType instanceof GridContainer )
    {
      final GridContainer gridContainer = (GridContainer) containerType;
      final GridLayout gridLayout = new GridLayout( gridContainer.getNumColumns(), gridContainer.isMakeColumnsEqualWidth() );
      gridLayout.marginWidth = gridContainer.getMarginWidth();
      gridLayout.marginHeight = gridContainer.getMarginHeight();

      gridLayout.marginLeft = gridContainer.getMarginLeft();
      gridLayout.marginRight = gridContainer.getMarginRight();
      gridLayout.marginTop = gridContainer.getMarginTop();
      gridLayout.marginBottom = gridContainer.getMarginBottom();

      gridLayout.horizontalSpacing = gridContainer.getHorizontalSpacing();
      gridLayout.verticalSpacing = gridContainer.getVerticalSpacing();

      final String groupText = gridContainer.getText();
      return new GridLayoutContainer( id, gridLayout, groupText );
    }

    if( containerType instanceof TabFolder )
    {
      return new TabFolderContainer( id );
    }

    if( containerType instanceof TabItem )
    {
      final TabItem tabItem = (TabItem) containerType;
      final String text = tabItem.getText();
      final String tooltip = tabItem.getTooltip();
      final String image = tabItem.getImage();
      return new TabItemContainer( id, text, tooltip, image );
    }

    throw new UnsupportedOperationException( String.format( Messages.getString("LayoutParser_5"), containerType.getClass().getName() ) ); //$NON-NLS-1$
  }

  private ILayoutController[] createControllers( final List<Controller> controllers )
  {
    final Collection<ILayoutController> result = new ArrayList<>( controllers.size() );

    for( final Controller controller : controllers )
    {
      try
      {
        final String type = controller.getType();
        final AbstractPartType sourcePart = (AbstractPartType) controller.getSourcePart();
        if( sourcePart == null )
          throw new IllegalArgumentException( String.format( Messages.getString("LayoutParser_6"), type ) ); //$NON-NLS-1$

        final AbstractPartType targetPart = (AbstractPartType) controller.getTargetPart();
        if( targetPart == null )
          throw new IllegalArgumentException( String.format( Messages.getString("LayoutParser_7"), type ) ); //$NON-NLS-1$

        final String sourceId = sourcePart.getId();
        final String targetId = targetPart.getId();

        final ILayoutController layoutController = createController( type, sourceId, targetId );
        if( layoutController != null )
          result.add( layoutController );
      }
      catch( final CoreException e )
      {
        KalypsoCorePlugin.getDefault().getLog().log( e.getStatus() );
      }
      catch( final Exception e )
      {
        final String msg = String.format( Messages.getString("LayoutParser_8"), e.toString() ); //$NON-NLS-1$
        final IStatus status = new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), msg, e );
        KalypsoCorePlugin.getDefault().getLog().log( status );
      }
    }

    return result.toArray( new ILayoutController[result.size()] );
  }

  private ILayoutController createController( final String type, final String sourceId, final String targetId ) throws CoreException
  {
    if( TYPE_CONTROLLER_SELECTION.equals( type ) )
    {
      final ILayoutPart sourcePart = findPart( sourceId, "source" ); //$NON-NLS-1$
      final ILayoutPart targetPart = findPart( targetId, "target" ); //$NON-NLS-1$
      return new SelectionLayoutController( sourcePart, targetPart );
    }

    if( TYPE_CONTROLLER_MODIFICATION.equals( type ) )
    {
      final ILayoutPart sourcePart = findPart( sourceId, "source" ); //$NON-NLS-1$
      final ILayoutPart targetPart = findPart( targetId, "target" ); //$NON-NLS-1$
      return new ModificationLayoutController( sourcePart, targetPart );
    }

    throw new IllegalArgumentException( String.format( Messages.getString("LayoutParser_13"), type ) ); //$NON-NLS-1$
  }

  private ILayoutPart findPart( final String id, final String name ) throws CoreException
  {
    final ILayoutPart foundPart = m_layoutPart.findPart( id );
    if( foundPart != null )
      return foundPart;

    final String message = String.format( Messages.getString("LayoutParser_14"), name, id ); //$NON-NLS-1$
    final Status status = new Status( IStatus.WARNING, KalypsoCorePlugin.getID(), message );
    throw new CoreException( status );
  }

  public ILayoutPart getLayoutPart( )
  {
    return m_layoutPart;
  }

  public ILayoutController[] getControllers( )
  {
    return m_controllers;
  }
}