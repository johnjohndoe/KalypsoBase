package org.kalypso.ui.editor.gmleditor.part;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * A {@link org.eclipse.ui.IViewPart} implementation showing a .gmv file.
 * 
 * @author Gernot Belger
 */
public class GmvViewPart extends AbstractGmvPart implements IViewPart
{
  @Override
  public IViewSite getViewSite( )
  {
    return (IViewSite)getSite();
  }

  @Override
  public void init( final IViewSite site )
  {
    setSite( site );
  }

  @Override
  public void init( final IViewSite site, final IMemento memento )
  {
    init( site );
  }

  @Override
  public void saveState( final IMemento memento )
  {
    // do nothing
  }

  /**
   * Copied from {@link org.eclipse.ui.part.ViewPart}.<br/>
   * Checks that the given site is valid for this type of part. The site for a view must be an <code>IViewSite</code>.
   * 
   * @param site
   *          the site to check
   * @since 3.1
   */
  @Override
  protected final void checkSite( final IWorkbenchPartSite site )
  {
    super.checkSite( site );
    Assert.isTrue( site instanceof IViewSite, "The site for a view must be an IViewSite" ); //$NON-NLS-1$
  }
}