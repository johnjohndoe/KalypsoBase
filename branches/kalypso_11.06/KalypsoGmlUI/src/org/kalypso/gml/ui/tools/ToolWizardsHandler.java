package org.kalypso.gml.ui.tools;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.wizards.IWizardRegistry;
import org.kalypso.contribs.eclipse.ui.dialogs.GenericWizardsHandler;
import org.kalypso.contribs.eclipse.ui.dialogs.GenericWizardsWizard;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;

public class ToolWizardsHandler extends GenericWizardsHandler
{
  private static final String TOOL_WIZARDS_EXTENSION_POINT = "toolWizards"; //$NON-NLS-1$

  public ToolWizardsHandler( )
  {
    super( KalypsoGmlUIPlugin.id(), TOOL_WIZARDS_EXTENSION_POINT );
  }

  @Override
  protected GenericWizardsWizard createWizard( final IStructuredSelection selection, final IWizardRegistry registry )
  {
    return new ToolWizardsWizard( selection, registry );
  }
}