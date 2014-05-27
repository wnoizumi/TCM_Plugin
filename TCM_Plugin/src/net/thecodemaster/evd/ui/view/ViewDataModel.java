package net.thecodemaster.evd.ui.view;

import java.util.List;

import net.thecodemaster.evd.helper.Creator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.Expression;

/**
 * Each instance of this class represents a single line into the Security Vulnerability View.
 * 
 * @author Luciano Sampaio
 */
public class ViewDataModel {

	private int												typeVulnerability;
	private IResource									resource;
	private Expression								expr;
	private String										message;
	private String										fullPath;
	private int												lineNumber;
	private IMarker										marker;

	/**
	 * A vulnerability might have several possible vulnerable paths, so this list contains all these paths.
	 */
	private final List<ViewDataModel>	children;

	public ViewDataModel() {
		children = Creator.newList();
	}

	public int getTypeVulnerability() {
		return typeVulnerability;
	}

	public void setTypeVulnerability(int typeVulnerability) {
		this.typeVulnerability = typeVulnerability;
	}

	public IResource getResource() {
		return resource;
	}

	public void setResource(IResource resource) {
		this.resource = resource;
	}

	public Expression getExpr() {
		return expr;
	}

	public void setExpr(Expression expr) {
		this.expr = expr;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public void addChildren(ViewDataModel vdm) {
		children.add(vdm);
	}

	public List<ViewDataModel> getChildren() {
		return children;
	}

	public IMarker getMarker() {
		return marker;
	}

	public void setMarker(IMarker marker) {
		this.marker = marker;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result *= prime + getTypeVulnerability();
		result *= prime + getLineNumber();
		result *= prime + getMessage().hashCode();
		result *= prime + getResource().hashCode();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (null == obj) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		ViewDataModel other = (ViewDataModel) obj;
		if (getTypeVulnerability() != other.getTypeVulnerability()) {
			return false;
		}
		if (getLineNumber() != other.getLineNumber()) {
			return false;
		}
		if (!getMessage().equals(other.getMessage())) {
			return false;
		}
		if (!getResource().equals(other.getResource())) {
			return false;
		}

		return true;
	}

	public ViewDataModel findByMarker(IMarker marker) {
		if ((null != getMarker()) && (getMarker().equals(marker))) {
			return this;
		}

		for (ViewDataModel vdm : getChildren()) {
			ViewDataModel current = vdm.findByMarker(marker);
			if (null != current) {
				return current;
			}
		}

		return null;
	}

}
