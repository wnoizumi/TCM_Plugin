package net.thecodemaster.sap.builders;

import java.util.Map;

import net.thecodemaster.sap.Manager;
import net.thecodemaster.sap.constants.Constants;
import net.thecodemaster.sap.jobs.ManagerJob;
import net.thecodemaster.sap.loggers.PluginLogger;
import net.thecodemaster.sap.ui.l10n.Messages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class Builder extends IncrementalProjectBuilder {

  /**
   * {@inheritDoc}
   */
  @Override
  protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
    throws CoreException {
    try {
      if (kind == FULL_BUILD) {
        fullBuild(monitor);
      }
      else if (kind == CLEAN_BUILD) {
        clean(monitor);
      }
      else {
        IResourceDelta delta = getDelta(getProject());
        if (null == delta) {
          fullBuild(monitor);
        }
        else {
          incrementalBuild(delta, monitor);
        }
      }
    }
    catch (CoreException e) {
      PluginLogger.logError(e);
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void clean(IProgressMonitor monitor) throws CoreException {
    // Delete markers set and files created.
    getProject().deleteMarkers(Constants.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
  }

  protected void fullBuild(final IProgressMonitor monitor) {
    ManagerJob job = new ManagerJob(Messages.Plugin.JOB, getManager(), getProject());
    job.run();
  }

  protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) {
    ManagerJob job = new ManagerJob(Messages.Plugin.JOB, getManager(), delta);
    job.run();
  }

  /**
   * @return An instance of the manager analyzer.
   */
  private Manager getManager() {
    return Manager.getInstance();
  }

}