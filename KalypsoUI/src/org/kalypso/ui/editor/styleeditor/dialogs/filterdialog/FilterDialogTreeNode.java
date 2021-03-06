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
/*
 * Created on 04.08.2004
 *
 */
package org.kalypso.ui.editor.styleeditor.dialogs.filterdialog;

import java.util.ArrayList;

import org.kalypso.ui.editor.styleeditor.MessageBundle;
import org.kalypso.ui.editor.styleeditor.panels.SpatialOperationPanel;
import org.kalypsodeegree_impl.filterencoding.OperationDefines;

/**
 * @author F.Lindemann
 */
public class FilterDialogTreeNode
{

  public static final int ROOT_TYPE = 0;

  public static final int LOGICAL_NODE_TYPE = 1;

  public static final int COMPARISON_NODE_TYPE = 2;

  public static final int FEATUREID_NODE_TYPE = 3;

  public static final int ELSEFILTER_TYPE = 4;

  // or PARAMETER_TYPE SUCH AS LITERAL, PROPERTY_NAME
  public static final int PARAMETER_TYPE = 4;

  public static final int SPATIAL_NODE_TYPE = 5;

  public static final int LOCICAL_NOT = OperationDefines.NOT;

  public static final int LOCICAL_AND = OperationDefines.AND;

  public static final int LOCICAL_OR = OperationDefines.OR;

  public static final int COMPARISON_LIKE = OperationDefines.PROPERTYISLIKE;

  public static final int COMPARISON_NULL = OperationDefines.PROPERTYISNULL;

  public static final int COMPARISON_BETWEEN = OperationDefines.PROPERTYISBETWEEN;

  public static final int COMPARISON_EQUALTO = OperationDefines.PROPERTYISEQUALTO;

  public static final int COMPARISON_LESSTHAN = OperationDefines.PROPERTYISLESSTHAN;

  public static final int COMPARISON_GREATERTHAN = OperationDefines.PROPERTYISGREATERTHAN;

  public static final int COMPARISON_LESSTHANOREQUALTO = OperationDefines.PROPERTYISLESSTHANOREQUALTO;

  public static final int COMPARISON_GREATERTHANOREQUALTO = OperationDefines.PROPERTYISGREATERTHANOREQUALTO;

  public static final int SPATIAL_INTERSECTS = OperationDefines.INTERSECTS;

  public static final int SPATIAL_DWITHIN = OperationDefines.DWITHIN;

  public static final int SPATIAL_DISJOINT = OperationDefines.DISJOINT;

  public static final int SPATIAL_CROSSES = OperationDefines.CROSSES;

  public static final int SPATIAL_CONTAINS = OperationDefines.CONTAINS;

  public static final int SPATIAL_BBOX = OperationDefines.BBOX;

  public static final int SPATIAL_BEYOND = OperationDefines.BEYOND;

  public static final int SPATIAL_TOUCHES = OperationDefines.TOUCHES;

  public static final int SPATIAL_EQUALS = OperationDefines.EQUALS;

  public static final int SPATIAL_OVERLAPS = OperationDefines.OVERLAPS;

  // not implemented yet
  // public static final int PROPERTYISNOTEQUALTO = 108;

  public FilterDialogTreeNode parent = null;

  public ArrayList<FilterDialogTreeNode> children = null;

  private String name = null;

  private AbstractData data = null;

  private int type = -1;

  private int subtype = -1;

  private FilterDialogTreeNode( )
  {
    // nothing
  }

  public FilterDialogTreeNode( final String string, final int m_type )
  {
    name = string;
    type = m_type;
    switch( type )
    {
      case LOGICAL_NODE_TYPE:
      {
        if( string.equals( "AND" ) ) //$NON-NLS-1$
          subtype = LOCICAL_AND;
        else if( string.equals( "OR" ) ) //$NON-NLS-1$
          subtype = LOCICAL_OR;
        else if( string.equals( "NOT" ) ) //$NON-NLS-1$
          subtype = LOCICAL_NOT;
        break;
      }
      case COMPARISON_NODE_TYPE:
      {
        if( string.equals( "LIKE" ) ) //$NON-NLS-1$
          subtype = COMPARISON_LIKE;
        else if( string.equals( "NULL" ) ) //$NON-NLS-1$
          subtype = COMPARISON_NULL;
        else if( string.equals( "BETWEEN" ) ) //$NON-NLS-1$
          subtype = COMPARISON_BETWEEN;
        else if( string.equals( "EQUAL_TO" ) ) //$NON-NLS-1$
          subtype = COMPARISON_EQUALTO;
        else if( string.equals( "LESS_THAN" ) ) //$NON-NLS-1$
          subtype = COMPARISON_LESSTHAN;
        else if( string.equals( "GREATER_THAN" ) ) //$NON-NLS-1$
          subtype = COMPARISON_GREATERTHAN;
        else if( string.equals( "LESS_THAN_OR_EQUAL_TO" ) ) //$NON-NLS-1$
          subtype = COMPARISON_LESSTHANOREQUALTO;
        else if( string.equals( "GREATER_THAN_OR_EQUAL_TO" ) ) //$NON-NLS-1$
          subtype = COMPARISON_GREATERTHANOREQUALTO;
      }
      case SPATIAL_NODE_TYPE:
      {
        if( string.equals( SpatialOperationPanel.INTERSECTS ) )
          subtype = SPATIAL_INTERSECTS;
        if( string.equals( SpatialOperationPanel.BBOX ) )
          subtype = SPATIAL_BBOX;
        if( string.equals( SpatialOperationPanel.BEYOND ) )
          subtype = SPATIAL_BEYOND;
        if( string.equals( SpatialOperationPanel.CONTAINS ) )
          subtype = SPATIAL_CONTAINS;
        if( string.equals( SpatialOperationPanel.CROSSES ) )
          subtype = SPATIAL_CROSSES;
        if( string.equals( SpatialOperationPanel.DWITHIN ) )
          subtype = SPATIAL_DWITHIN;
        if( string.equals( SpatialOperationPanel.DISJOINT ) )
          subtype = SPATIAL_DISJOINT;
        if( string.equals( SpatialOperationPanel.EQUALS ) )
          subtype = SPATIAL_EQUALS;
        if( string.equals( SpatialOperationPanel.OVERLAPS ) )
          subtype = SPATIAL_OVERLAPS;
        if( string.equals( SpatialOperationPanel.TOUCHES ) )
          subtype = SPATIAL_TOUCHES;
      }
      default:
        break;
    }

    if( type == ROOT_TYPE )
      createRoot( string );
  }

  public static boolean isBinaryComparisonType( final int type )
  {
    if( type >= OperationDefines.PROPERTYISEQUALTO && type <= OperationDefines.PROPERTYISGREATERTHANOREQUALTO )
      return true;
    return false;
  }

  public static boolean isBinarySpatialType( final int type )
  {
    if( type >= OperationDefines.EQUALS && type <= OperationDefines.OVERLAPS )
      return true;
    return false;
  }

  public String getName( )
  {
    return name;
  }

  public boolean validate( ) throws FilterDialogException
  {
    if( type == LOGICAL_NODE_TYPE )
    {
      if( subtype == LOCICAL_NOT )
      {
        if( children != null && children.size() == 1 )
          return true;
        throw new FilterDialogException( new FilterDialogError( this, MessageBundle.STYLE_EDITOR_FILTER_ERROR_CHILD ) );
      }

      if( children != null && children.size() > 1 )
        return true;
      throw new FilterDialogException( new FilterDialogError( this, MessageBundle.STYLE_EDITOR_FILTER_ERROR_CHILDREN ) );

    }
    // Spatial stuff
    else if( type == SPATIAL_NODE_TYPE )
    {
      return true;
    }

    if( data != null )
    {
      try
      {
        return data.verify();
      }
      catch( final FilterDialogException e )
      {
        e.getError().setNode( this );
        throw e;
      }
    }

    throw new FilterDialogException( new FilterDialogError( this, MessageBundle.STYLE_EDITOR_FILTER_ERROR_DATA_NULL ) );

  }

  private void createRoot( final String m_name )
  {
    parent = new FilterDialogTreeNode();
    parent.name = m_name;
    parent.type = ROOT_TYPE;
    children = new ArrayList<>();
    children.add( parent );
  }

  public void addNode( final FilterDialogTreeNode node )
  {
    if( children == null )
      children = new ArrayList<>();

    node.parent = this;
    children.add( node );
  }

  public void removeNode( final FilterDialogTreeNode node )
  {
    if( children != null )
    {
      children.remove( node );
    }
  }

  public Object[] getChildren( )
  {
    if( children == null )
      children = new ArrayList<>();

    return children.toArray();
  }

  public int getType( )
  {
    return type;
  }

  public int getSubType( )
  {
    return subtype;
  }

  public FilterDialogTreeNode getParent( )
  {
    return parent;
  }

  public AbstractData getData( )
  {
    return data;
  }

  public void setData( final AbstractData m_data )
  {
    data = m_data;
  }
}
