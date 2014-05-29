package net.thecodemaster.evd.verifier;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.graph.Parameter;
import net.thecodemaster.evd.graph.VariableBindingManager;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.marker.annotation.AnnotationManager;
import net.thecodemaster.evd.point.EntryPoint;
import net.thecodemaster.evd.point.ExitPoint;
import net.thecodemaster.evd.reporter.Reporter;
import net.thecodemaster.evd.ui.l10n.Message;
import net.thecodemaster.evd.xmlloader.LoaderExitPoint;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * The verifier is the class that actually knows how to find the vulnerability and the one that performs this
 * verification. Each verifier can reimplement/override methods of add new behavior to them.
 * 
 * @author Luciano Sampaio
 */
public abstract class Verifier {

	/**
	 * The name of the current verifier.
	 */
	private final String						name;
	/**
	 * The id of the current verifier.
	 */
	private final int								id;
	/**
	 * The current resource that is being analyzed.
	 */
	private IResource								currentResource;
	/**
	 * This object contains all the methods, variables and their interactions, on the project that is being analyzed.
	 */
	private CallGraph								callGraph;
	/**
	 * The object that know how and where to report the found vulnerabilities.
	 */
	private Reporter								reporter;
	/**
	 * List with all the ExitPoints of this verifier.
	 */
	private List<ExitPoint>					exitPoints;
	/**
	 * List with all the EntryPoints (shared among other instances of the verifiers).
	 */
	private static List<EntryPoint>	entryPoints;

	/**
	 * @param name
	 *          The name of the verifier.
	 * @param id
	 *          The id of the verifier.
	 * @param listEntryPoints
	 *          List with all the EntryPoints methods.
	 */
	public Verifier(String name, int id, List<EntryPoint> listEntryPoints) {
		this.name = name;
		this.id = id;

		entryPoints = listEntryPoints;
	}

	public String getName() {
		return name;
	}

	private int getId() {
		return id;
	}

	private void setCurrentResource(IResource currentResource) {
		this.currentResource = currentResource;
	}

	private IResource getCurrentResource() {
		return currentResource;
	}

	protected CallGraph getCallGraph() {
		return callGraph;
	}

	protected Reporter getReporter() {
		return reporter;
	}

	protected List<ExitPoint> getExitPoints() {
		if (null == exitPoints) {
			// Loads all the ExitPoints of this verifier.
			loadExitPoints();
		}

		return exitPoints;
	}

	protected void loadExitPoints() {
		exitPoints = (new LoaderExitPoint(getId())).load();
	}

	protected static List<EntryPoint> getEntryPoints() {
		if (null == entryPoints) {
			entryPoints = Creator.newList();
		}

		return entryPoints;
	}

	/**
	 * Notifies that a subtask of the main task is beginning.
	 * 
	 * @param taskName
	 *          The text that will be displayed to the user.
	 */
	protected void setSubTask(String taskName) {
		if ((null != getReporter()) && (null != getReporter().getProgressMonitor())) {
			getReporter().getProgressMonitor().subTask(taskName);
		}
	}

	protected String getMessageEntryPoint(String value) {
		return String.format(Message.VerifierSecurityVulnerability.ENTRY_POINT_METHOD, value);
	}

	protected void reportVulnerability(DataFlow dataFlow) {
		getReporter().addProblem(getId(), getCurrentResource(), dataFlow);
	}

	protected boolean hasReachedMaximumDepth(int depth) {
		return Constant.MAXIMUM_VERIFICATION_DEPTH == depth;
	}

	/**
	 * The public run method that will be invoked by the Analyzer.
	 * 
	 * @param resources
	 * @param callGraph
	 * @param reporter
	 */
	public void run(List<IResource> resources, CallGraph callGraph, Reporter reporter) {
		this.callGraph = callGraph;
		this.reporter = reporter;

		setSubTask(getName());

		// Perform the verifications on the resources.
		// 01 - Run the vulnerability detection on all the provided resources.
		for (IResource resource : resources) {
			run(resource);
		}
	}

	/**
	 * Iterate over all the method declarations found in the current resource.
	 * 
	 * @param resource
	 */
	protected void run(IResource resource) {
		// We need this information when we are going to display the vulnerabilities.
		setCurrentResource(resource);

		// 02 - Get the list of methods in the current resource.
		Map<MethodDeclaration, List<Expression>> methods = getCallGraph().getMethods(resource);

		// 03 - Get all the method invocations of each method declaration.
		for (MethodDeclaration methodDeclaration : methods.keySet()) {
			run(methodDeclaration);
		}
	}

	/**
	 * Run the vulnerability detection on the current method declaration.
	 * 
	 * @param methodDeclaration
	 */
	protected void run(MethodDeclaration methodDeclaration) {
		// The depth control the investigation mechanism to avoid infinitive loops.
		int depth = 0;
		checkBlock(depth, methodDeclaration.getBody());
	}

	protected void checkBlock(int depth, Block block) {
		if (null != block) {
			List<?> statements = block.statements();
			for (Object object : statements) {
				checkStatement(depth, (Statement) object);
			}
		}
	}

	protected void checkStatement(int depth, Statement statement) {
		// 01 - To avoid infinitive loop, this check is necessary.
		if (hasReachedMaximumDepth(depth)) {
			return;
		}

		switch (statement.getNodeType()) {
			case ASTNode.BLOCK: // 08
				checkBlock(depth, (Block) statement);
				break;
			case ASTNode.DO_STATEMENT: // 19
				checkDoStatement(depth, (DoStatement) statement);
				break;
			case ASTNode.EXPRESSION_STATEMENT: // 21
				checkExpressionStatement(depth, (ExpressionStatement) statement);
				break;
			case ASTNode.FOR_STATEMENT: // 24
				checkForStatement(depth, (ForStatement) statement);
				break;
			case ASTNode.IF_STATEMENT: // 25
				checkIfStatement(depth, (IfStatement) statement);
				break;
			case ASTNode.RETURN_STATEMENT: // 41
				checkReturnStatement(depth, (ReturnStatement) statement);
				break;
			case ASTNode.SWITCH_STATEMENT: // 50
				checkSwitchStatement(depth, (SwitchStatement) statement);
				break;
			case ASTNode.TRY_STATEMENT: // 54
				checkTryStatement(depth, (TryStatement) statement);
				break;
			case ASTNode.VARIABLE_DECLARATION_STATEMENT: // 60
				checkVariableDeclarationStatementStatement(depth, (VariableDeclarationStatement) statement);
				break;
			case ASTNode.WHILE_STATEMENT: // 61
				checkWhileStatement(depth, (WhileStatement) statement);
				break;
			default:
				PluginLogger.logError("checkStatement Default Node Type: " + statement.getNodeType() + " - " + statement, null);
		}
	}

	/**
	 * Checks for the Statements.
	 */
	private void checkDoStatement(int depth, DoStatement statement) {
		checkStatement(depth, statement.getBody());
	}

	private void checkExpressionStatement(int depth, ExpressionStatement expression) {
		checkExpression(depth, expression.getExpression());
	}

	private void checkForStatement(int depth, ForStatement statement) {
		checkStatement(depth, statement.getBody());
	}

	private void checkIfStatement(int depth, IfStatement statement) {
		checkStatement(depth, statement.getThenStatement());
		checkStatement(depth, statement.getElseStatement());
	}

	private void checkReturnStatement(int depth, ReturnStatement statement) {
		checkExpression(depth, statement.getExpression());
	}

	private void checkSwitchStatement(int depth, SwitchStatement statement) {
		List<?> switchStatements = statement.statements();
		for (Object switchCases : switchStatements) {
			checkStatement(depth, (Statement) switchCases);
		}
	}

	private void checkTryStatement(int depth, TryStatement statement) {
		checkStatement(depth, statement.getBody());

		List<?> listCatches = statement.catchClauses();
		for (Object catchClause : listCatches) {
			checkStatement(depth, ((CatchClause) catchClause).getBody());
		}

		checkStatement(depth, statement.getFinally());
	}

	protected void checkVariableDeclarationStatementStatement(int depth, VariableDeclarationStatement statement) {
		List<?> fragments = statement.fragments();
		for (Iterator<?> iter = fragments.iterator(); iter.hasNext();) {
			// VariableDeclarationFragment: is the plain variable declaration part.
			// Example: "int x=0, y=0;" contains two VariableDeclarationFragments, "x=0" and "y=0"
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();

			// (fragment.getName(), fragment.getInitializer());
		}
	}

	protected void checkWhileStatement(int depth, WhileStatement statement) {
		checkStatement(depth, statement.getBody());
	}

	protected void checkExpression(int depth, Expression expression) {
		// 01 - To avoid infinitive loop, this check is necessary.
		if (hasReachedMaximumDepth(depth)) {
			return;
		}

		// 06 - We need to check the type of the parameter and deal with it accordingly to its type.
		switch (expression.getNodeType()) {
			case ASTNode.ARRAY_INITIALIZER: // 04
				// checkArrayInitializer(depth, expression);
				break;
			case ASTNode.ASSIGNMENT: // 07
				break;
			case ASTNode.CAST_EXPRESSION: // 11
				// checkCastExpression(depth, expression);
				break;
			case ASTNode.CLASS_INSTANCE_CREATION: // 14
				// checkClassInstanceCreation(depth, expression);
				break;
			case ASTNode.CONDITIONAL_EXPRESSION: // 16
				// checkConditionExpression(depth, expression);
				break;
			case ASTNode.INFIX_EXPRESSION: // 27
				// checkInfixExpression(depth, expression);
				break;
			case ASTNode.METHOD_INVOCATION: // 32
				checkMethodInvocation(depth, expression);
				break;
			case ASTNode.PARENTHESIZED_EXPRESSION: // 36
				// checkParenthesizedExpression(depth, expression);
				break;
			case ASTNode.PREFIX_EXPRESSION: // 38
				// checkPrefixExpression(depth, expression);
				break;
			case ASTNode.QUALIFIED_NAME: // 40
				// checkQualifiedName(depth, expression);
				break;
			case ASTNode.SIMPLE_NAME: // 42
				// checkSimpleName(depth, expression);
				break;
			case ASTNode.CHARACTER_LITERAL: // 13
			case ASTNode.NULL_LITERAL: // 33
			case ASTNode.NUMBER_LITERAL: // 34
			case ASTNode.STRING_LITERAL: // 45
				// checkLiteral(depth, expression);
				break;
			default:
				PluginLogger.logError("checkExpression Default Node Type: " + expression.getNodeType() + " - " + expression,
						null);
		}
	}

	/**
	 * Checks for the Expressions.
	 */
	protected void checkMethodInvocation(int depth, Expression expression) {
		// There are 2 cases: When we have the source code of this method and when we do not.
		MethodDeclaration methodDeclaration = getCallGraph().getMethod(getCurrentResource(), expression);

		if (null != methodDeclaration) {
			// 01 - We have the source code.

		} else {
			// 02 - We do not have the source code.
			// Now we have to investigate if the element who is invoking this method is vulnerable or not.
			Expression objectName = ((MethodInvocation) expression).getExpression();
			if (isVulnerable(objectName)) {
				// We found a vulnerability.
			}
		}
	}

	protected boolean isVulnerable(Expression expression) {
		return false;
	}

	/**
	 * OLD PART
	 */
	protected void performVerification(IResource resource) {
		// 01 - Get the list of methods in the current resource.
		Map<MethodDeclaration, List<Expression>> methods = getCallGraph().getMethods(resource);

		if (null != methods) {
			// 02 - Get all the method invocations of each method declaration.
			for (List<Expression> invocations : methods.values()) {

				// 03 - Iterate over all method invocations to verify if it is a ExitPoint.
				for (Expression method : invocations) {
					ExitPoint exitPoint = getExitPointIfMethodIsOne(method);

					if (null != exitPoint) {
						// 04 - Some methods will need to have access to the resource that is currently being analyzed.
						// but we do not want to pass it to all these methods as a parameter.
						setCurrentResource(resource);

						// 05 - This is an ExitPoint method and it needs to be verified.
						performVerification(method, exitPoint);
					}
				}
			}
		}
	}

	protected void performVerification(Expression method, ExitPoint exitPoint) {
		// 01 - Get the parameters (received) from the current method.
		List<Expression> receivedParameters = BindingResolver.getParameters(method);

		// 02 - Get the expected parameters of the ExitPoint method.
		Map<Parameter, List<Integer>> expectedParameters = exitPoint.getParameters();

		int index = 0;
		int depth = 0;
		for (List<Integer> rules : expectedParameters.values()) {
			// If the rules are null, it means the expected parameter can be anything. (We do not care for it).
			if (null != rules) {
				Expression expr = receivedParameters.get(index);
				DataFlow df = new DataFlow();

				checkExpression(df, rules, depth, expr);
				if (df.isVulnerable()) {
					reportVulnerability(df);
				}
			}
			index++;
		}
	}

	/**
	 * @param method
	 * @return An ExitPoint object if this node belongs to the list, otherwise null.
	 */
	protected ExitPoint getExitPointIfMethodIsOne(Expression method) {
		for (ExitPoint currentExitPoint : getExitPoints()) {
			if (BindingResolver.methodsHaveSameNameAndPackage(currentExitPoint, method)) {
				// 01 - Get the expected arguments of this method.
				Map<Parameter, List<Integer>> expectedParameters = currentExitPoint.getParameters();

				// 02 - Get the received parameters of the current method.
				List<Expression> receivedParameters = BindingResolver.getParameters(method);

				// 03 - It is necessary to check the number of parameters and its types
				// because it may exist methods with the same names but different parameters.
				if (expectedParameters.size() == receivedParameters.size()) {
					boolean isMethodAnExitPoint = true;
					int index = 0;
					for (Parameter expectedParameter : expectedParameters.keySet()) {
						ITypeBinding typeBinding = receivedParameters.get(index++).resolveTypeBinding();

						// Verify if all the parameters are the ones expected. However, there is a case
						// where an Object is expected, and any type is accepted.
						if (!BindingResolver.parametersHaveSameType(expectedParameter.getType(), typeBinding)) {
							isMethodAnExitPoint = false;
							break;
						}
					}

					if (isMethodAnExitPoint) {
						return currentExitPoint;
					}
				}
			}
		}

		return null;
	}

	protected boolean isMethodAnEntryPoint(Expression method) {
		for (EntryPoint currentEntryPoint : getEntryPoints()) {
			if (BindingResolver.methodsHaveSameNameAndPackage(currentEntryPoint, method)) {
				// 01 - Get the expected arguments of this method.
				List<String> expectedParameters = currentEntryPoint.getParameters();

				// 02 - Get the received parameters of the current method.
				List<Expression> receivedParameters = BindingResolver.getParameters(method);

				// 03 - It is necessary to check the number of parameters and its types
				// because it may exist methods with the same names but different parameters.
				if (expectedParameters.size() == receivedParameters.size()) {
					boolean isMethodAnEntryPoint = true;
					int index = 0;
					for (String expectedParameter : expectedParameters) {
						ITypeBinding typeBinding = receivedParameters.get(index++).resolveTypeBinding();

						// Verify if all the parameters are the ones expected.
						if (!BindingResolver.parametersHaveSameType(expectedParameter, typeBinding)) {
							isMethodAnEntryPoint = false;
							break;
						}
					}

					if (isMethodAnEntryPoint) {
						return true;
					}
				}
			}
		}

		return false;
	}

	protected boolean isMethodASanitizationPoint(Expression method) {
		return false;
	}

	protected void checkExpression(DataFlow df, List<Integer> rules, int depth, Expression expr) {
		// 01 - If the parameter matches the rules (Easy case), the parameter is okay, otherwise we need to check for more
		// things.
		if (!matchRules(rules, expr)) {

			// 02 -Add the current element to the data flow.
			df = df.addNodeToPath(expr);

			// 03 - To avoid infinitive loop, this check is necessary.
			if (hasReachedMaximumDepth(depth)) {
				// Informs that we can no longer investigate because it looks like we are in an infinitive loop.
				df.isInfinitiveLoop(expr);

				return;
			}

			// 04 - Check if there is an annotation, in case there is, we should BELIEVE it is not vulnerable.
			if (!hasAnnotationAtPosition(expr)) {

				// 05 - We are going to investigate 1 layer deeper, so we increment the depth.
				depth++;

				// 06 - We need to check the type of the parameter and deal with it accordingly to its type.
				switch (expr.getNodeType()) {
					case ASTNode.STRING_LITERAL:
					case ASTNode.CHARACTER_LITERAL:
					case ASTNode.NUMBER_LITERAL:
					case ASTNode.NULL_LITERAL:
						checkLiteral(df, expr);
						break;
					case ASTNode.INFIX_EXPRESSION:
						checkInfixExpression(df, rules, depth, expr);
						break;
					case ASTNode.PREFIX_EXPRESSION:
						checkPrefixExpression(df, rules, depth, expr);
						break;
					case ASTNode.CONDITIONAL_EXPRESSION:
						checkConditionExpression(df, rules, depth, expr);
						break;
					case ASTNode.ASSIGNMENT:
						checkAssignment(df, rules, depth, expr);
						break;
					case ASTNode.SIMPLE_NAME:
						checkSimpleName(df, rules, depth, expr);
						break;
					case ASTNode.QUALIFIED_NAME:
						checkQualifiedName(df, rules, depth, expr);
						break;
					case ASTNode.METHOD_INVOCATION:
						checkMethodInvocation(df, rules, depth, expr);
						break;
					case ASTNode.CAST_EXPRESSION:
						checkCastExpression(df, rules, depth, expr);
						break;
					case ASTNode.CLASS_INSTANCE_CREATION:
						checkClassInstanceCreation(df, rules, depth, expr);
						break;
					case ASTNode.ARRAY_INITIALIZER:
						checkArrayInitializer(df, rules, depth, expr);
						break;
					case ASTNode.PARENTHESIZED_EXPRESSION:
						checkParenthesizedExpression(df, rules, depth, expr);
						break;
					default:
						PluginLogger.logError("Default Node Type: " + expr.getNodeType() + " - " + expr, null);
				}
			}
		}
	}

	protected boolean hasAnnotationAtPosition(Expression expr) {
		return AnnotationManager.hasAnnotationAtPosition(expr);
	}

	protected boolean matchRules(List<Integer> rules, Expression parameter) {
		if (null == parameter) {
			// There is nothing we can do to verify it.
			return true;
		}

		// -1 Anything is valid.
		// 0 Only sanitized values are valid.
		// 1 LITERAL and sanitized values are valid.
		for (Integer astNodeValue : rules) {
			if (astNodeValue == Constant.LITERAL) {
				switch (parameter.getNodeType()) {
					case ASTNode.STRING_LITERAL:
					case ASTNode.CHARACTER_LITERAL:
					case ASTNode.NUMBER_LITERAL:
					case ASTNode.NULL_LITERAL:
						return true;
				}
			} else if (astNodeValue == parameter.getNodeType()) {
				return true;
			}
		}

		return false;
	}

	protected void checkLiteral(DataFlow df, Expression expr) {
	}

	protected void checkInfixExpression(DataFlow df, List<Integer> rules, int depth, Expression expr) {
		InfixExpression parameter = (InfixExpression) expr;

		// 01 - Get the elements from the operation.
		Expression leftOperand = parameter.getLeftOperand();
		Expression rightOperand = parameter.getRightOperand();
		List<Expression> extendedOperands = BindingResolver.getParameters(parameter);

		// 02 - Check each element.
		checkExpression(df, rules, depth, leftOperand);
		checkExpression(df, rules, depth, rightOperand);

		for (Expression expression : extendedOperands) {
			checkExpression(df, rules, depth, expression);
		}
	}

	protected void checkPrefixExpression(DataFlow df, List<Integer> rules, int depth, Expression expr) {
		PrefixExpression parameter = (PrefixExpression) expr;
		// 01 - Get the elements from the operation.
		Expression operand = parameter.getOperand();

		// 02 - Check each element.
		checkExpression(df, rules, depth, operand);
	}

	protected void checkConditionExpression(DataFlow df, List<Integer> rules, int depth, Expression expr) {
		ConditionalExpression parameter = (ConditionalExpression) expr;

		// 01 - Get the elements from the operation.
		Expression thenExpression = parameter.getThenExpression();
		Expression elseExpression = parameter.getElseExpression();

		// 02 - Check each element.
		checkExpression(df, rules, depth, thenExpression);
		checkExpression(df, rules, depth, elseExpression);
	}

	protected void checkAssignment(DataFlow df, List<Integer> rules, int depth, Expression expr) {
		Assignment assignment = (Assignment) expr;

		// 01 - Get the elements from the operation.
		Expression leftHandSide = assignment.getLeftHandSide();
		Expression rightHandSide = assignment.getRightHandSide();

		// 02 - Check each element.
		checkExpression(df, rules, depth, leftHandSide);
		checkExpression(df, rules, depth, rightHandSide);
	}

	protected void checkSimpleName(DataFlow df, List<Integer> rules, int depth, Expression expr) {
		SimpleName simpleName = (SimpleName) expr;

		// 01 - Try to retrieve the variable from the list of variables.
		VariableBindingManager manager = getCallGraph().getVariableBinding(simpleName);
		if (null != manager) {

			// 02 - This is the case where we have to go deeper into the variable's path.
			Expression initializer = manager.getInitializer();
			checkExpression(df, rules, depth, initializer);
		} else {
			// This is the case where the variable is an argument of the method.
			// 04 - Get the method signature that is using this parameter.
			MethodDeclaration methodDeclaration = BindingResolver.getParentMethodDeclaration(simpleName);

			// 05 - Get the index position where this parameter appear.
			int parameterIndex = BindingResolver.getParameterIndex(methodDeclaration, simpleName);
			if (parameterIndex >= 0) {
				// 06 - Get the list of methods that invokes this method.
				Map<MethodDeclaration, List<Expression>> invokers = getCallGraph().getInvokers(methodDeclaration);

				if (null != invokers) {
					// 07 - Iterate over all the methods that invokes this method.
					for (List<Expression> currentInvocations : invokers.values()) {

						// 08 - Care only about the invocations to this method.
						for (Expression expression : currentInvocations) {
							if (BindingResolver.areMethodsEqual(methodDeclaration, expression)) {
								// 09 - Get the parameter at the index position.
								Expression parameter = BindingResolver.getParameterAtIndex(expression, parameterIndex);

								// 10 - Run detection on this parameter.
								checkExpression(df, rules, depth, parameter);
							}
						}

					}
				}
			}

		}
	}

	protected void checkQualifiedName(DataFlow df, List<Integer> rules, int depth, Expression expr) {
		Expression expression = ((QualifiedName) expr).getName();

		checkExpression(df, rules, depth, expression);
	}

	protected void checkMethodInvocation(DataFlow df, List<Integer> rules, int depth, Expression expr) {
		// 01 - Check if this method is a Sanitization-Point.
		if (isMethodASanitizationPoint(expr)) {
			// If a sanitization method is being invoked, then we do not have a vulnerability.
			return;
		}

		// 02 - Check if this method is a Entry-Point.
		if (isMethodAnEntryPoint(expr)) {
			String message = getMessageEntryPoint(BindingResolver.getFullName(expr));

			// We found a invocation to a entry point method.
			df.isVulnerable(Constant.Vulnerability.ENTRY_POINT, message);
			return;
		}

		// 03 - Follow the data flow of this method and try to identify what is the return from it.

		// Get the implementation of this method. If the return is NULL it means this is a library that the developer
		// does not own the source code.
		MethodDeclaration methodDeclaration = getCallGraph().getMethod(getCurrentResource(), expr);

		if (null != methodDeclaration) {
			checkBlock(df, rules, depth, methodDeclaration.getBody());
		} else {
			// TODO - Special cases:
			// "url".toString(); variable.toLowerCase();
			// MethodInvocation methodInvocation = (MethodInvocation) expr;
			// Expression optionalExpression = methodInvocation.getExpression();
			//
			// if (null != optionalExpression) {
			// checkExpression(vp.(optionalExpression), rules, optionalExpression, depth);
			// } else {
			df.isVulnerable(Constant.Vulnerability.UNKNOWN, "We fear what we do not understand!");
			System.out.println("Method:" + expr);
			// }
		}
	}

	protected void checkCastExpression(DataFlow df, List<Integer> rules, int depth, Expression expr) {
		Expression expression = ((CastExpression) expr).getExpression();

		checkExpression(df, rules, depth, expression);
	}

	protected void checkClassInstanceCreation(DataFlow df, List<Integer> rules, int depth, Expression expr) {
		List<Expression> parameters = BindingResolver.getParameters(expr);
		for (Expression parameter : parameters) {
			checkExpression(df, rules, depth, parameter);
		}
	}

	protected void checkArrayInitializer(DataFlow df, List<Integer> rules, int depth, Expression expr) {
		List<Expression> parameters = BindingResolver.getParameters(expr);
		for (Expression parameter : parameters) {
			checkExpression(df, rules, depth, parameter);
		}
	}

	protected void checkParenthesizedExpression(DataFlow df, List<Integer> rules, int depth, Expression expr) {
		Expression expression = ((ParenthesizedExpression) expr).getExpression();

		checkExpression(df, rules, depth, expression);
	}

	protected void checkBlock(DataFlow df, List<Integer> rules, int depth, Block block) {
		List<?> statements = block.statements();
		for (Object object : statements) {
			checkStatement(df, rules, depth, (Statement) object);
		}
	}

	protected void checkStatement(DataFlow df, List<Integer> rules, int depth, Statement statement) {
		if (statement.getNodeType() == ASTNode.RETURN_STATEMENT) {
			Expression expr = ((ReturnStatement) statement).getExpression();
			checkExpression(df, rules, depth, expr);
		} else if (hasReachedMaximumDepth(depth)) {
			// To avoid infinitive loop, this check is necessary.
			// Informs that we can no longer investigate because it looks like we are in an infinitive loop.
			df.isInfinitiveLoop(statement);

			return;
		} else {
			switch (statement.getNodeType()) {
				case ASTNode.FOR_STATEMENT:
					checkIfBlockOrStatement(df, rules, depth, ((ForStatement) statement).getBody());
					break;
				case ASTNode.WHILE_STATEMENT:
					checkIfBlockOrStatement(df, rules, depth, ((WhileStatement) statement).getBody());
					break;
				case ASTNode.DO_STATEMENT:
					checkIfBlockOrStatement(df, rules, depth, ((DoStatement) statement).getBody());
					break;
				case ASTNode.IF_STATEMENT:
					IfStatement is = (IfStatement) statement;

					checkIfBlockOrStatement(df, rules, depth, is.getThenStatement());
					checkIfBlockOrStatement(df, rules, depth, is.getElseStatement());
					break;
				case ASTNode.SWITCH_STATEMENT:
					SwitchStatement switchStatement = (SwitchStatement) statement;

					List<?> switchStatements = switchStatement.statements();
					for (Object switchCases : switchStatements) {
						checkIfBlockOrStatement(df, rules, depth, (Statement) switchCases);
					}
					break;
				case ASTNode.TRY_STATEMENT:
					TryStatement tryStatement = (TryStatement) statement;

					checkIfBlockOrStatement(df, rules, depth, tryStatement.getBody());

					List<?> listCatches = tryStatement.catchClauses();
					for (Object catchClause : listCatches) {
						checkIfBlockOrStatement(df, rules, depth, ((CatchClause) catchClause).getBody());
					}

					checkIfBlockOrStatement(df, rules, depth, tryStatement.getFinally());
					break;
			}
		}
	}

	protected void checkIfBlockOrStatement(DataFlow df, List<Integer> rules, int depth, Statement statement) {
		if (null == statement) {
			return;
		}

		switch (statement.getNodeType()) {
			case ASTNode.BLOCK:
				checkBlock(df, rules, ++depth, (Block) statement);
				break;
			default:
				checkStatement(df, rules, ++depth, statement);
				break;
		}
	}

}
