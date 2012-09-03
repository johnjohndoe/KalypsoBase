/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Bj�rnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universit�t Hamburg-Harburg, Institut f�r Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.contribs.eclipse.compare;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ZipFileStructureCreator;
import org.eclipse.compare.internal.BufferedResourceNode;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.DiffTreeViewer;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * Einziger Zweck dieser Klasse ist es, die nicht�ffentliche Klasse ResourcecompareInput sichtbar zu machen.
 * 
 * @author belger
 */
@SuppressWarnings("restriction")
public class ResourceCompareInputCopy extends CompareEditorInput
{
  private static final boolean NORMALIZE_CASE = true;

  private boolean fThreeWay = false;

  private Object fRoot;

  private IStructureComparator fAncestor;

  private IStructureComparator fLeft;

  private IStructureComparator fRight;

  private IResource fAncestorResource;

  private IResource fLeftResource;

  private IResource fRightResource;

  protected DiffTreeViewer fDiffViewer;

  protected IAction fOpenAction;

  class MyDiffNode extends DiffNode
  {
    private boolean fDirty = false;

    private ITypedElement fLastId;

    private String fLastName;

    public MyDiffNode( final IDiffContainer parent, final int description, final ITypedElement ancestor, final ITypedElement left, final ITypedElement right )
    {
      super( parent, description, ancestor, left, right );
    }

    @Override
    public void fireChange( )
    {
      super.fireChange();
      setDirty( true );
      fDirty = true;
      if( fDiffViewer != null )
        fDiffViewer.refresh( this );
    }

    void clearDirty( )
    {
      fDirty = false;
    }

    @Override
    public String getName( )
    {
      if( fLastName == null )
        fLastName = super.getName();
      if( fDirty )
        return '<' + fLastName + '>';
      return fLastName;
    }

    @Override
    public ITypedElement getId( )
    {
      final ITypedElement id = super.getId();
      if( id == null )
        return fLastId;
      fLastId = id;
      return id;
    }
  }

  static class FilteredBufferedResourceNode extends BufferedResourceNode
  {
    FilteredBufferedResourceNode( final IResource resource )
    {
      super( resource );
    }

    @Override
    protected IStructureComparator createChild( final IResource child )
    {
      final String name = child.getName();
      if( CompareUIPlugin.getDefault().filter( name, child instanceof IContainer, false ) )
        return null;
      return new FilteredBufferedResourceNode( child );
    }
  }

  /**
   * Creates an compare editor input for the given selection.
   */
  public ResourceCompareInputCopy( final CompareConfiguration config )
  {
    super( config );
  }

  @Override
  public Viewer createDiffViewer( final Composite parent )
  {
    fDiffViewer = new DiffTreeViewer( parent, getCompareConfiguration() )
    {
      /**
       * Overriden to get rid of yellow things
       * 
       * @see org.eclipse.jface.viewers.StructuredViewer#handleOpen(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      protected void handleOpen( final SelectionEvent event )
      {
        super.handleOpen( event );
      }

      @Override
      protected void fillContextMenu( final IMenuManager manager )
      {
        if( fOpenAction == null )
        {
          fOpenAction = new Action()
          {
            @Override
            public void run( )
            {
              handleOpen( null );
            }
          };
          Utilities.initAction( fOpenAction, getBundle(), "action.CompareContents." ); //$NON-NLS-1$
        }
        boolean enable = false;
        final ISelection selection = getSelection();
        if( selection instanceof IStructuredSelection )
        {
          final IStructuredSelection ss = (IStructuredSelection) selection;
          if( ss.size() == 1 )
          {
            final Object element = ss.getFirstElement();
            if( element instanceof MyDiffNode )
            {
              final ITypedElement te = ((MyDiffNode) element).getId();
              if( te != null )
                enable = !ITypedElement.FOLDER_TYPE.equals( te.getType() );
            }
            else
              enable = true;
          }
        }
        fOpenAction.setEnabled( enable );
        manager.add( fOpenAction );
        super.fillContextMenu( manager );
      }
    };
    return fDiffViewer;
  }

  public void setSelection( final ISelection s )
  {
    final IResource[] selection = Utilities.getResources( s );
    fThreeWay = selection.length == 3;
    fAncestorResource = null;
    fLeftResource = selection[0];
    fRightResource = selection[1];
    if( fThreeWay )
    {
      fLeftResource = selection[1];
      fRightResource = selection[2];
    }
    fAncestor = null;
    fLeft = getStructure( fLeftResource );
    fRight = getStructure( fRightResource );
    if( fThreeWay )
    {
      fAncestorResource = selection[0];
      fAncestor = getStructure( fAncestorResource );
    }
  }

  /**
   * Returns true if compare can be executed for the given selection.
   */
  public boolean isEnabled( final ISelection s )
  {
    final IResource[] selection = Utilities.getResources( s );
    if( selection.length < 2 || selection.length > 3 )
      return false;
    fThreeWay = selection.length == 3;
    fLeftResource = selection[0];
    fRightResource = selection[1];
    if( fThreeWay )
    {
      fLeftResource = selection[1];
      fRightResource = selection[2];
    }
    if( !comparable( fLeftResource, fRightResource ) )
      return false;
    if( fThreeWay )
    {
      fAncestorResource = selection[0];
      if( !comparable( fLeftResource, fRightResource ) )
        return false;
    }
    return true;
  }

  /**
   * Initializes the images in the compare configuration.
   */
  public void initializeCompareConfiguration( )
  {
    final CompareConfiguration cc = getCompareConfiguration();
    if( fLeftResource != null )
    {
      cc.setLeftLabel( buildLabel( fLeftResource ) );
      cc.setLeftImage( CompareUIPlugin.getImage( fLeftResource ) );
    }
    if( fRightResource != null )
    {
      cc.setRightLabel( buildLabel( fRightResource ) );
      cc.setRightImage( CompareUIPlugin.getImage( fRightResource ) );
    }
    if( fThreeWay && fAncestorResource != null )
    {
      cc.setAncestorLabel( buildLabel( fAncestorResource ) );
      cc.setAncestorImage( CompareUIPlugin.getImage( fAncestorResource ) );
    }
  }

  /**
   * Returns true if both resources are either structured or unstructured.
   */
  private boolean comparable( final IResource c1, final IResource c2 )
  {
    return hasStructure( c1 ) == hasStructure( c2 );
  }

  /**
   * Returns true if the given argument has a structure.
   */
  private boolean hasStructure( final IResource input )
  {
    if( input instanceof IContainer )
      return true;
    if( input instanceof IFile )
    {
      final IFile file = (IFile) input;
      String type = file.getFileExtension();
      if( type != null )
      {
        type = normalizeCase( type );
        return "JAR".equals( type ) || "ZIP".equals( type ); //$NON-NLS-2$ //$NON-NLS-1$
      }
    }
    return false;
  }

  /**
   * Creates a <code>IStructureComparator</code> for the given input. Returns <code>null</code> if no
   * <code>IStructureComparator</code> can be found for the <code>IResource</code>.
   */
  private IStructureComparator getStructure( final IResource input )
  {
    if( input instanceof IContainer )
      return new FilteredBufferedResourceNode( input );
    if( input instanceof IFile )
    {
      final IStructureComparator rn = new FilteredBufferedResourceNode( input );
      final IFile file = (IFile) input;
      final String type = normalizeCase( file.getFileExtension() );
      if( "JAR".equals( type ) || "ZIP".equals( type ) ) //$NON-NLS-2$ //$NON-NLS-1$
        return new ZipFileStructureCreator().getStructure( rn );
      return rn;
    }
    return null;
  }

  /**
   * Performs a two-way or three-way diff on the current selection.
   */
  @Override
  public Object prepareInput( final IProgressMonitor pm ) throws InvocationTargetException
  {
    try
    {
      // fix for PR 1GFMLFB: ITPUI:WIN2000 - files that are out of sync with the
      // file system appear as empty
      fLeftResource.refreshLocal( IResource.DEPTH_INFINITE, pm );
      fRightResource.refreshLocal( IResource.DEPTH_INFINITE, pm );
      if( fThreeWay && fAncestorResource != null )
        fAncestorResource.refreshLocal( IResource.DEPTH_INFINITE, pm );
      // end fix
      pm.beginTask( Utilities.getString( "ResourceCompare.taskName" ), IProgressMonitor.UNKNOWN ); //$NON-NLS-1$
      final String leftLabel = fLeftResource.getName();
      final String rightLabel = fRightResource.getName();
      String title;
      if( fThreeWay )
      {
        final String format = Utilities.getString( "ResourceCompare.threeWay.title" ); //$NON-NLS-1$
        final String ancestorLabel = fAncestorResource.getName();
        title = MessageFormat.format( format, ancestorLabel, leftLabel, rightLabel );
      }
      else
      {
        final String format = Utilities.getString( "ResourceCompare.twoWay.title" ); //$NON-NLS-1$
        title = MessageFormat.format( format, new Object[] { leftLabel, rightLabel } );
      }
      setTitle( title );
      final Differencer d = new Differencer()
      {
        @Override
        protected Object visit( final Object parent, final int description, final Object ancestor, final Object left, final Object right )
        {
          return new MyDiffNode( (IDiffContainer) parent, description, (ITypedElement) ancestor, (ITypedElement) left, (ITypedElement) right );
        }
      };
      fRoot = d.findDifferences( fThreeWay, pm, null, fAncestor, fLeft, fRight );
      return fRoot;
    }
    catch( final CoreException ex )
    {
      throw new InvocationTargetException( ex );
    }
    finally
    {
      pm.done();
    }
  }

  @Override
  public String getToolTipText( )
  {
    if( fLeftResource != null && fRightResource != null )
    {
      final String leftLabel = fLeftResource.getFullPath().makeRelative().toString();
      final String rightLabel = fRightResource.getFullPath().makeRelative().toString();
      if( fThreeWay )
      {
        final String format = Utilities.getString( "ResourceCompare.threeWay.tooltip" ); //$NON-NLS-1$
        final String ancestorLabel = fAncestorResource.getFullPath().makeRelative().toString();
        return MessageFormat.format( format, ancestorLabel, leftLabel, rightLabel );
      }
      final String format = Utilities.getString( "ResourceCompare.twoWay.tooltip" ); //$NON-NLS-1$
      return MessageFormat.format( format, leftLabel, rightLabel );
    }
    // fall back
    return super.getToolTipText();
  }

  private String buildLabel( final IResource r )
  {
    final String n = r.getFullPath().toString();
    if( n.charAt( 0 ) == IPath.SEPARATOR )
      return n.substring( 1 );
    return n;
  }

  @Override
  public void saveChanges( final IProgressMonitor pm ) throws CoreException
  {
    super.saveChanges( pm );
    if( fRoot instanceof DiffNode )
    {
      try
      {
        commit( pm, (DiffNode) fRoot );
      }
      finally
      {
        if( fDiffViewer != null )
          fDiffViewer.refresh();
        setDirty( false );
      }
    }
  }

  /*
   * Recursively walks the diff tree and commits all changes.
   */
  private static void commit( final IProgressMonitor pm, final DiffNode node ) throws CoreException
  {
    if( node instanceof MyDiffNode )
      ((MyDiffNode) node).clearDirty();
    final ITypedElement left = node.getLeft();
    if( left instanceof BufferedResourceNode )
      ((BufferedResourceNode) left).commit( pm );
    final ITypedElement right = node.getRight();
    if( right instanceof BufferedResourceNode )
      ((BufferedResourceNode) right).commit( pm );
    final IDiffElement[] children = node.getChildren();
    if( children != null )
    {
      for( final IDiffElement element : children )
      {
        if( element instanceof DiffNode )
          commit( pm, (DiffNode) element );
      }
    }
  }

  /**
   * @see IAdaptable.getAdapter
   */
  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    if( IFile[].class.equals( adapter ) )
    {
      final HashSet<IFile> collector = new HashSet<IFile>();
      collectDirtyResources( fRoot, collector );
      return collector.toArray( new IFile[collector.size()] );
    }
    return super.getAdapter( adapter );
  }

  private void collectDirtyResources( final Object o, final Set<IFile> collector )
  {
    if( o instanceof DiffNode )
    {
      final DiffNode node = (DiffNode) o;
      final ITypedElement left = node.getLeft();
      if( left instanceof BufferedResourceNode )
      {
        final BufferedResourceNode bn = (BufferedResourceNode) left;
        if( bn.isDirty() )
        {
          final IResource resource = bn.getResource();
          if( resource instanceof IFile )
            collector.add( (IFile) resource );
        }
      }
      final ITypedElement right = node.getRight();
      if( right instanceof BufferedResourceNode )
      {
        final BufferedResourceNode bn = (BufferedResourceNode) right;
        if( bn.isDirty() )
        {
          final IResource resource = bn.getResource();
          if( resource instanceof IFile )
            collector.add( (IFile) resource );
        }
      }
      final IDiffElement[] children = node.getChildren();
      if( children != null )
      {
        for( final IDiffElement element : children )
        {
          if( element instanceof DiffNode )
            collectDirtyResources( element, collector );
        }
      }
    }
  }

  private static String normalizeCase( final String s )
  {
    if( NORMALIZE_CASE && s != null )
      return s.toUpperCase();
    return s;
  }
}
