package net.thecodemaster.sap.visitors;

import java.util.List;

import net.thecodemaster.sap.graph.CallGraph;
import net.thecodemaster.sap.loggers.PluginLogger;
import net.thecodemaster.sap.utils.Creator;
import net.thecodemaster.sap.utils.Timer;
import net.thecodemaster.sap.utils.UtilProjects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * @author Luciano Sampaio
 */
public class CallGraphVisitor implements IResourceVisitor, IResourceDeltaVisitor {

  /**
   * The resource types that should be trigger the call graph visitor.
   */
  private static List<String> resourceTypes;
  private List<IResource>     updatedResources;

  private CallGraph           callGraph;

  public CallGraphVisitor(CallGraph callGraph) {
    this.callGraph = callGraph;
    updatedResources = Creator.newList();
  }

  public List<IResource> run(IProject project) throws CoreException {
    project.accept(this);

    return updatedResources;
  }

  public List<IResource> run(IResourceDelta delta) throws CoreException {
    delta.accept(this);

    return updatedResources;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean visit(IResourceDelta delta) throws CoreException {
    IResource resource = delta.getResource();

    switch (delta.getKind()) {
      case IResourceDelta.REMOVED:
        // TODO - Handle removed files.
        break;
      case IResourceDelta.ADDED:
      case IResourceDelta.CHANGED:
        return visit(resource);
    }
    // Return true to continue visiting children.
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean visit(IResource resource) throws CoreException {
    if (isToPerformDetection(resource)) {
      ICompilationUnit cu = JavaCore.createCompilationUnitFrom((IFile) resource);

      if (cu.isStructureKnown()) {
        // Creates the AST for the ICompilationUnits.
        Timer timer = (new Timer("01.1.1 - Parsing: " + resource.getName())).start();
        CompilationUnit cUnit = parse(cu);
        PluginLogger.logInfo(timer.stop().toString());

        // Visit the compilation unit.
        timer = (new Timer("01.1.2 - Visiting: " + resource.getName())).start();

        // Remove the old branches of this resource.
        callGraph.removeFile(resource);

        // Add a new empty branch.
        callGraph.addFile(resource);
        CompilationUnitVisitor cuVisitor = new CompilationUnitVisitor(callGraph);
        cUnit.accept(cuVisitor);
        PluginLogger.logInfo(timer.stop().toString());

        // Add this resource to the list of updated resources.
        updatedResources.add(resource);
      }
    }
    // Return true to continue visiting children.
    return true;
  }

  /**
   * Check if the detection should be performed in this resource or not.
   * 
   * @param resource The resource that will be tested.
   * @return True if the detection should be performed in this resource, otherwise false.
   */
  private boolean isToPerformDetection(IResource resource) {
    if (resource instanceof IFile) {
      if (null == resourceTypes) {
        resourceTypes = UtilProjects.getResourceTypesToPerformDetection();
      }

      for (String resourceType : resourceTypes) {
        if (resource.getFileExtension().equalsIgnoreCase(resourceType)) {
          return true;
        }
      }
    }
    // If it reaches this point, it means that the detection should not be performed in this resource.
    return false;
  }

  /**
   * Reads a ICompilationUnit and creates the AST DOM for manipulating the Java source file.
   * 
   * @param unit
   * @return A compilation unit.
   */
  private CompilationUnit parse(ICompilationUnit unit) {
    ASTParser parser = ASTParser.newParser(AST.JLS4);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setSource(unit);
    parser.setResolveBindings(true);
    return (CompilationUnit) parser.createAST(null); // Parse.
  }

}
