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
package org.kalypso.simulation.ui.wizards.calculation;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.auth.scenario.IScenario;
import org.kalypso.auth.scenario.Scenario;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.commons.bind.JaxbUtilities;
import org.kalypso.contribs.eclipse.core.resources.IProjectProvider;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StoreExceptionSafeRunnable;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.eclipse.jface.wizard.IResetablePage;
import org.kalypso.contribs.eclipse.jface.wizard.IResetableWizard;
import org.kalypso.contribs.eclipse.jface.wizard.view.IWizard2;
import org.kalypso.contribs.eclipse.swt.graphics.RGBUtilities;
import org.kalypso.contribs.java.lang.reflect.ClassUtilities;
import org.kalypso.model.xml.ArgListType;
import org.kalypso.model.xml.ArgType;
import org.kalypso.model.xml.Calcwizard;
import org.kalypso.model.xml.ObjectFactory;
import org.kalypso.simulation.ui.KalypsoSimulationUIPlugin;
import org.kalypso.simulation.ui.calccase.ModelNature;
import org.kalypso.simulation.ui.calccase.ModelSynchronizer;
import org.kalypso.simulation.ui.wizards.calculation.createchoices.AddNewCalcCaseChoice;
import org.kalypso.simulation.ui.wizards.calculation.createchoices.ContinueOldCalcCaseChoice;
import org.kalypso.simulation.ui.wizards.calculation.createchoices.CopyCalcCaseChoice;
import org.kalypso.simulation.ui.wizards.calculation.createchoices.CopyServerCalcCaseChoice;
import org.kalypso.simulation.ui.wizards.calculation.createchoices.IAddCalcCaseChoice;
import org.kalypso.simulation.ui.wizards.calculation.createchoices.IChoiceListener;
import org.kalypso.simulation.ui.wizards.createCalcCase.SteuerparameterWizardPage;
import org.kalypso.ui.ImageProvider;
import org.xml.sax.InputSource;

/**
 * The calc wiazrd.
 * <p>
 * Supported arguments in the CalcWizard xml-definition:
 * <ul>
 * <li>showCancelButton: [Boolean] do show the cancel button, defaults to <code>false</code></li>
 * <li>showSaveButton: [Boolean] do show the save button, defaults to <code>false</code></li>
 * <li>askForSave: [Boolean] If <code>true</code>, the user is asked if the wizard shall be saved when the wizard is
 * finished. Defaults to <code>false</code>.</li>
 * <li>saveOnPageChange: [Boolean] If <code>true</code>, the state of every (model-)page is saved when leaving it.</li>
 * </ul>
 * 
 * @author Gernot Belger
 */
public class CalcWizard implements IWizard2, IProjectProvider, IChoiceListener, IResetableWizard
{
  private static final JAXBContext JC = JaxbUtilities.createQuiet( ObjectFactory.class );

  private static final RGB DEFAULT_TITLE_RGB = new RGB( 255, 255, 255 );

  private final IProject m_project;

  protected CreateCalcCasePage m_createCalcCasePage;

  protected SteuerparameterWizardPage m_controlPage;

  private final List<IWizardPage> m_pages = new ArrayList<IWizardPage>();

  private IWizardContainer m_container;

  private IDialogSettings m_dialogSettings;

  private final ModelSynchronizer m_synchronizer;

  private boolean m_buttonsLocked;

  private Calcwizard m_calcwizard;

  /** Scenario of current calcCase */
  protected IScenario m_scenario = Scenario.DEFAULT_SCENARIO;

  private Arguments m_arguments;

  /** Must call {@link #initWizard(IProgressMonitor)}afterwards. */
  public CalcWizard( final IProject project )
  {
    m_project = project;

    final File serverRoot = KalypsoSimulationUIPlugin.getServerModelRoot();
    final File serverProject = new File( serverRoot, project.getName() );

    m_synchronizer = new ModelSynchronizer( project, serverProject );
  }

  public void initWizard( final IProgressMonitor monitor ) throws CoreException
  {
    final IFile wizardConfigFile = (IFile) m_project.findMember( ModelNature.MODELLTYP_CALCWIZARD_XML );

    monitor.beginTask( "Lade " + wizardConfigFile.getName(), 1000 );

    try
    {
      final InputSource inputSource = new InputSource( wizardConfigFile.getContents() );
      inputSource.setEncoding( wizardConfigFile.getCharset() );

      m_calcwizard = (Calcwizard) JC.createUnmarshaller().unmarshal( inputSource );

      m_arguments = buildArguments( m_calcwizard );
    }
    catch( final JAXBException e )
    {
      e.printStackTrace();

      throw new CoreException( StatusUtilities.statusFromThrowable( e, "Fehler beim Erzeugen der Seiten zur Modellbearbeitung" ) );
    }
    finally
    {
      monitor.done();
    }
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#addPages()
   */
  public void addPages( )
  {
    m_createCalcCasePage = new CreateCalcCasePage( "addCalcCasePage", "Vorhersage starten", ImageProvider.IMAGE_KALYPSO_ICON_BIG );
    m_createCalcCasePage.addChoiceListener( this );

    m_controlPage = new SteuerparameterWizardPage( this, ImageProvider.IMAGE_KALYPSO_ICON_BIG, true );

    m_createCalcCasePage.addChoice( new AddNewCalcCaseChoice( "neue Rechenvariante erzeugen", m_project, m_createCalcCasePage ) );
    m_createCalcCasePage.addChoice( new ContinueOldCalcCaseChoice( "vorhandene Rechenvariante öffnen", m_project, m_createCalcCasePage ) );
    m_createCalcCasePage.addChoice( new CopyCalcCaseChoice( "vorhandene Rechenvariante kopieren", m_project, m_createCalcCasePage ) );
    m_createCalcCasePage.addChoice( new CopyServerCalcCaseChoice( "auf dem Server archivierte Rechenvariante kopieren", m_project, m_createCalcCasePage, m_synchronizer ) );
    addPage( m_createCalcCasePage );
    addPage( m_controlPage );
  }

  public void addPage( final IWizardPage page )
  {
    m_pages.add( page );
    page.setWizard( this );
  }

  protected void addModelPages( final IFolder calcCaseFolder, final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      monitor.beginTask( "Seiten zur Modellbearbeitung werden geladen", 1000 );

      final List<Calcwizard.Page> pages = m_calcwizard.getPage();
      for( final Object element : pages )
      {
        final Calcwizard.Page page = (Calcwizard.Page) element;

        final Arguments arguments = buildArguments( page );

        final String className = page.getClassName();
        final String pageTitle = page.getPageTitle();
        final String imageLocation = page.getImageLocation();
        final ImageDescriptor imageDesc = imageLocation == null ? null : ImageProvider.id( imageLocation );

        final IModelWizardPage wizardPage = (IModelWizardPage) ClassUtilities.newInstance( className, IModelWizardPage.class, ModelNature.class.getClassLoader(), null, null );
        wizardPage.init( m_project, pageTitle, imageDesc, arguments, calcCaseFolder );

        addPage( wizardPage );
      }
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      throw new CoreException( StatusUtilities.statusFromThrowable( e, "Fehler beim Erzeugen der Seiten zur Modellbearbeitung" ) );
    }
    finally
    {
      monitor.done();
    }
  }

  private Arguments buildArguments( final ArgListType alt )
  {
    final Arguments map = new Arguments();

    final List<ArgType> arglist = alt.getArg();
    for( final Object element : arglist )
    {
      final ArgType arg = (ArgType) element;
      Object value = arg.getValue();
      if( value == null )
        value = buildArguments( arg );

      map.put( arg.getName(), value );
    }

    return map;
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  public boolean performFinish( )
  {
    final IWizardContainer container = getContainer();
    final Shell shell = container.getShell();

    if( doAskForSave() )
    {
      final MessageDialog msgDialog = new MessageDialog( shell, "Vorhersageassistent beenden", null, "Vorhersageassistent wird beendet, zuletzt geänderte Daten speichern?", MessageDialog.QUESTION, new String[] {
          IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL }, 0 );
      switch( msgDialog.open() )
      {
        case 0: // YES
          break; // just continue to save
        case 1: // NO
          return true;

        case 2: // Cancel
          return false;
      }
    }

    final WorkspaceModifyOperation operation = new WorkspaceModifyOperation( null )
    {
      @Override
      protected void execute( final IProgressMonitor monitor ) throws CoreException
      {
        monitor.beginTask( "Geänderte Daten werden gespeichert", 1000 );

        try
        {
          final IStatus status = saveAllPages( monitor );
          if( !status.isOK() )
            throw new CoreException( status );
        }
        finally
        {
          monitor.done();
        }
      }
    };

    final IStatus status = RunnableContextHelper.execute( container, false, false, operation );
    ErrorDialog.openError( shell, "Daten speichern", "Fehler beim Speichern der Daten", status );
    return status.isOK();
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#performCancel()
   */
  public boolean performCancel( )
  {
    if( doAskForSave() )
      return MessageDialog.openConfirm( getContainer().getShell(), "Abbruch", "Zuletzt geänderte Daten gehen verloren, möchten Sie wirklich Abbrechen?" );

    return true;
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#canFinish()
   */
  public boolean canFinish( )
  {
    // can allways finish
    // this is important, as we do not show the cancel button
    return true;
    // final IWizardPage currentPage = getContainer().getCurrentPage();
    // return ( currentPage instanceof IModelWizardPage );
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#dispose()
   */
  public void dispose( )
  {
    // notify pages
    for( int i = 0; i < m_pages.size(); i++ )
      m_pages.get( i ).dispose();
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#getPageCount()
   */
  public int getPageCount( )
  {
    return m_pages.size();
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#getStartingPage()
   */
  public IWizardPage getStartingPage( )
  {
    if( m_pages.size() == 0 )
      return null;
    return m_pages.get( 0 );
  }

  /**
   * @return true if can go to next page
   * @see org.eclipse.jface.wizard.IWizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
   */
  public boolean finishPage( final IWizardPage page )
  {
    final WorkspaceModifyOperation op = new WorkspaceModifyOperation( null )
    {
      @Override
      public void execute( final IProgressMonitor monitor ) throws CoreException
      {
        if( page == m_createCalcCasePage )
        {
          // vielleicht sollte das hier auch erst nach den Steuerparametern
          // passieren?
          m_createCalcCasePage.doNext( monitor );

          // REMARK: do not move the declaration away, as the currentCalcCase is initialized inside 'doNext'
          final IFolder currentCalcCase = m_createCalcCasePage.getCurrentCalcCase();

          m_controlPage.setUpdate( m_createCalcCasePage.shouldUpdate() );
          m_controlPage.setFolder( currentCalcCase );
        }
        else if( page == m_controlPage )
        {
          monitor.beginTask( "aktualisiere Zeitreihen", 3000 );

          final IFolder currentCalcCase = m_createCalcCasePage.getCurrentCalcCase();

          try
          {
            m_controlPage.saveChanges( currentCalcCase, new SubProgressMonitor( monitor, 1000 ) );

            final ModelNature nature = (ModelNature) currentCalcCase.getProject().getNature( ModelNature.ID );

            // szenario für currentCalcCase holen
            m_scenario = nature.getScenario( currentCalcCase );

            final IStatus result;
            if( m_controlPage.isUpdate() )
            {
              result = nature.updateCalcCase( currentCalcCase, new SubProgressMonitor( monitor, 1000 ) );

              // only update once, user has to check it again, to reload timeseries
              m_controlPage.setUpdate( false );
            }
            else
            {
              monitor.worked( 1000 );
              result = Status.OK_STATUS;
            }

            // nur beim ersten mal jetzt die Model-Pages hinzufügen
            if( getPageCount() == 2 )
              addModelPages( currentCalcCase, new SubProgressMonitor( monitor, 1000 ) );

            if( !result.isOK() )
              throw new CoreException( result );
          }
          finally
          {
            monitor.done();
          }
        }
        else if( page instanceof IModelWizardPage )
        {
          final IModelWizardPage modelPage = (IModelWizardPage) page;
          if( isSaveOnPageChange() )
          {
            final IStatus status = modelPage.saveData( false, monitor );
            if( !status.isOK() )
              throw new CoreException( status );
          }
        }
      }
    };

    try
    {
      getContainer().run( true, true, op );
    }
    catch( final InterruptedException e )
    {
      // canceled
      return false;
    }
    catch( final InvocationTargetException e )
    {
      e.printStackTrace();

      boolean ret = false;

      final Throwable te = e.getTargetException();
      final Shell shell = getContainer().getShell();
      if( te instanceof CoreException )
      {
        final IStatus status = ((CoreException) te).getStatus();

        // Got to next page if not a real error
        if( !status.matches( IStatus.ERROR ) )
          ret = true;

        // TODO: für Sachsen-Anhalt wollen wir nur einen Dialog für Fehler sehen
        // Sollte vermutlich konfigurierbar sein (machen wir im head)
        final int displayMask = /* IStatus.OK | IStatus.INFO | */IStatus.WARNING | IStatus.ERROR;
        StatusUtilities.openSpecialErrorDialog( shell, "Fehler", "Aktualisierung des Berechnungsfalls", status, displayMask, true );
      }
      else
      {
        // CoreExceptions are handled above, but unexpected runtime exceptions
        // and errors may still occur.
        MessageDialog.openError( shell, "Fehler", "Fehler beim Aufruf der nächsten Wizard-Seite: " + te.getLocalizedMessage() );
      }

      return ret;
    }

    return true;
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
   */
  public IWizardPage getNextPage( final IWizardPage page )
  {
    if( page == m_controlPage && getPageCount() == 2 )
      return page;

    final int index = m_pages.indexOf( page );
    if( index == m_pages.size() - 1 || index == -1 )
      // last page or page not found
      return null;

    return m_pages.get( index + 1 );
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#getPreviousPage(org.eclipse.jface.wizard.IWizardPage)
   */
  public IWizardPage getPreviousPage( final IWizardPage page )
  {
    final int index = m_pages.indexOf( page );
    if( index == 0 || index == -1 )
      // first page or page not found
      return null;

    // do not go back to first page -> the first page changes the current calcCase
    // but the already created model-wizard-pages do not know this and still show/edit
    // the old calc-case
    if( page == m_controlPage )
      return null;

    return m_pages.get( index - 1 );
  }

  /**
   * @see org.kalypso.contribs.eclipse.core.resources.IProjectProvider#getProject()
   */
  public IProject getProject( )
  {
    return m_project;
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#createPageControls(org.eclipse.swt.widgets.Composite)
   */
  public void createPageControls( final Composite pageContainer )
  {
    // nichts tun, no demand!
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#getContainer()
   */
  public IWizardContainer getContainer( )
  {
    return m_container;
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#getDefaultPageImage()
   */
  public Image getDefaultPageImage( )
  {
    return null;
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#getDialogSettings()
   */
  public IDialogSettings getDialogSettings( )
  {
    return m_dialogSettings;
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#getPage(java.lang.String)
   */
  public IWizardPage getPage( final String pageName )
  {
    for( final Object element : m_pages )
    {
      final IWizardPage page = (IWizardPage) element;
      if( pageName.equals( page.getName() ) )
        return page;
    }

    return null;
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#getPages()
   */
  public IWizardPage[] getPages( )
  {
    return m_pages.toArray( new IWizardPage[m_pages.size()] );
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#getTitleBarColor()
   */
  public RGB getTitleBarColor( )
  {
    if( m_container.getCurrentPage() == m_createCalcCasePage )
      return DEFAULT_TITLE_RGB;

    try
    {
      final String rgb = m_scenario.getProperty( IScenario.PROP_WIZARD_TITLE_RGB, null );
      if( rgb == null )
        return DEFAULT_TITLE_RGB;

      return RGBUtilities.parse( rgb, ";" );
    }
    catch( final NumberFormatException e )
    {
      return DEFAULT_TITLE_RGB;
    }
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#getWindowTitle()
   */
  public String getWindowTitle( )
  {
    final String msg = "Hochwasser Vorhersage für " + m_project.getName();
    final IFolder currentFolder = m_controlPage.getFolder();
    if( currentFolder == null )
      return msg;

    return msg + " - " + currentFolder.getName();
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#isHelpAvailable()
   */
  public boolean isHelpAvailable( )
  {
    return getHelpId() != null;
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#needsPreviousAndNextButtons()
   */
  public boolean needsPreviousAndNextButtons( )
  {
    return true;
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#needsProgressMonitor()
   */
  public boolean needsProgressMonitor( )
  {
    return true;
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#setContainer(org.eclipse.jface.wizard.IWizardContainer)
   */
  public void setContainer( final IWizardContainer wizardContainer )
  {
    m_container = wizardContainer;
  }

  public boolean isButtonsLocked( )
  {
    return m_buttonsLocked;
  }

  public void setButtonsLocked( final boolean buttonsLocked )
  {
    m_buttonsLocked = buttonsLocked;
    getContainer().updateButtons();
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.createchoices.IChoiceListener#onChoiceChanged(IAddCalcCaseChoice)
   */
  public void onChoiceChanged( final IAddCalcCaseChoice newChoice )
  {
    // kann weg?
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.view.IWizard2#hasCancelButton()
   */
  public boolean hasCancelButton( )
  {
    final String showCancelStr = m_arguments.getProperty( "showCancelButton", "false" );
    return Boolean.valueOf( showCancelStr ).booleanValue();
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.view.IWizard2#getInitialBrowserSize()
   */
  public int getInitialBrowserSize( )
  {
    return m_calcwizard.getBrowserInitialSize();
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.view.IWizard2#hasSaveButton()
   */
  public boolean hasSaveButton( )
  {
    final String showSaveStr = m_arguments.getProperty( "showSaveButton", "false" );
    return Boolean.valueOf( showSaveStr ).booleanValue();
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.view.IWizard2#doAskForSave()
   */
  public boolean doAskForSave( )
  {
    final String askForSaveStr = m_arguments.getProperty( "askForSave", "false" );
    return Boolean.valueOf( askForSaveStr ).booleanValue();
  }

  protected boolean isSaveOnPageChange( )
  {
    final String saveOnPageChangeStr = m_arguments.getProperty( "saveOnPageChange", "false" );
    return Boolean.valueOf( saveOnPageChangeStr ).booleanValue();
  }

  /**
   * Calls {@link IModelWizardPage#saveState(IProgressMonitor)}for all pages
   * 
   * @throws CoreException
   */
  public IStatus saveAllPages( final IProgressMonitor monitor ) 
  {
    final IWizardPage[] pages = getPages();
    final int pageCount = pages.length;

    monitor.beginTask( "Seiten werden gespeichert...", 1 * pageCount );

    final List<IStatus> statusList = new ArrayList<IStatus>( pageCount );
    try
    {
      for( int i = 0; i < pageCount; i++ )
      {
        final IWizardPage page = pages[i];
        if( page instanceof IModelWizardPage )
        {
          final StoreExceptionSafeRunnable runnable = new StoreExceptionSafeRunnable()
          {
            public void run( ) throws Exception
            {
              ((IModelWizardPage) page).saveState( new SubProgressMonitor( monitor, 1 ) );
            }
          };
          SafeRunnable.run( runnable );

          final Throwable exception = runnable.getException();
          if( exception != null )
            statusList.add( StatusUtilities.statusFromThrowable( exception ) );
        }
        else
          monitor.worked( 1 );
      }
    }
    finally
    {
      monitor.done();
    }

    return StatusUtilities.createStatus( statusList, "Fehler beim Speichern des Seitenzustands" );
  }

  /**
   * Calls {@link AbstractCalcWizardPage#selectionChanged(SelectionChangedEvent)}on all pages.
   * 
   * @param monitor
   */
  public IStatus refreshAllPages( final IProgressMonitor monitor )
  {
    final IWizardPage[] pages = getPages();
    final int pageCount = pages.length;

    monitor.beginTask( "Seiten werden wiederhergestellt", pageCount );

    final List<IStatus> statusList = new ArrayList<IStatus>( pageCount );
    for( int i = 0; i < pageCount; i++ )
    {
      final IWizardPage page = pages[i];
      if( page instanceof IModelWizardPage )
      {
        final StoreExceptionSafeRunnable runnable = new StoreExceptionSafeRunnable()
        {
          public void run( ) throws Exception
          {
            ((IModelWizardPage) page).restoreState( true );
          }
        };
        SafeRunnable.run( runnable );

        final Throwable exception = runnable.getException();
        if( exception != null )
          statusList.add( StatusUtilities.statusFromThrowable( exception ) );
      }
    }

    return StatusUtilities.createStatus( statusList, "Wiederherstellen der Wizard-Seiten" );
  }

  public ModelSynchronizer getModelSynchronizer( )
  {
    return m_synchronizer;
  }

  /**
   * Return the help context id to display, if the help button is pressed.
   * <p>
   * If the page has a context id, return it
   * </p>
   * <p>
   * Else, return the one of the calcWizard.xml
   * </p>
   */
  public String getHelpId( )
  {
    final IWizardPage currentPage = getContainer().getCurrentPage();

    final String pageHelpId;
    if( currentPage instanceof ICalcWizardPage )
      pageHelpId = ((ICalcWizardPage) currentPage).getHelpId();
    else
      pageHelpId = null;

    if( pageHelpId != null )
      return pageHelpId;

    return m_calcwizard == null ? null : m_calcwizard.getHelpId();
  }

  public Arguments getArgument( )
  {
    return m_arguments;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.IResetableWizard#showResetButton()
   */
  public boolean showResetButton( )
  {
    final IWizardPage page = getContainer().getCurrentPage();
    return page instanceof IResetablePage;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.IResetableWizard#canReset()
   */
  public boolean canReset( )
  {
    final IWizardPage currentPage = getContainer().getCurrentPage();

    return currentPage instanceof IResetablePage && ((IResetablePage) currentPage).canReset();
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.IResetableWizard#performReset()
   */
  public void performReset( )
  {
    final IResetablePage currentPage = (IResetablePage) getContainer().getCurrentPage();

    final UIJob job = new UIJob( "Seite zurücksetzen" )
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        try
        {
          return currentPage.performReset( monitor );
        }
        catch( final CoreException e )
        {
          return e.getStatus();
        }
      }
    };
    job.setUser( false );
    job.setSystem( true );
    job.schedule();
  }

}