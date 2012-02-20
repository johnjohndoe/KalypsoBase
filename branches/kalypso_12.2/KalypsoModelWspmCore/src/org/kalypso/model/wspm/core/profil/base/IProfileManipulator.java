package org.kalypso.model.wspm.core.profil.base;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilChange;

public interface IProfileManipulator
{
  IProfilChange[] performProfileManipulation( IProfil profile, IProgressMonitor monitor ) throws CoreException;
}