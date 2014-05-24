package net.thecodemaster.evd.ui.view;

import net.thecodemaster.evd.Activator;
import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.ui.l10n.Message;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

class ViewLabelProvider implements ITableLabelProvider {

	private String getTypeVulnerabilityName(int typeVulnerability) {
		switch (typeVulnerability) {
			case Constant.VERIFIER_ID_COMMAND_INJECTION:
				return Message.Plugin.VERIFIER_NAME_COMMAND_INJECTION;
			case Constant.VERIFIER_ID_COOKIE_POISONING:
				return Message.Plugin.VERIFIER_NAME_COOKIE_POISONING;
			case Constant.VERIFIER_ID_CROSS_SITE_SCRIPTING:
				return Message.Plugin.VERIFIER_NAME_CROSS_SITE_SCRIPTING;
			case Constant.VERIFIER_ID_PATH_TRAVERSAL:
				return Message.Plugin.VERIFIER_NAME_PATH_TRAVERSAL;
			case Constant.VERIFIER_ID_SECURITY_MISCONFIGURATION:
				return Message.Plugin.VERIFIER_NAME_SECURITY_MISCONFIGURATION;
			case Constant.VERIFIER_ID_SQL_INJECTION:
				return Message.Plugin.VERIFIER_NAME_SQL_INJECTION;
			case Constant.VERIFIER_ID_UNVALIDATED_REDIRECTING:
				return Message.Plugin.VERIFIER_NAME_UNVALIDATED_REDIRECTING;
			default:
				return null;
		}
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		switch (columnIndex) {
			case 3:
				return Activator.getImageDescriptor(Constant.Icons.SECURITY_VULNERABILITY).createImage();
			default:
				return null;
		}
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		ViewDataModel vdm = (ViewDataModel) element;

		switch (columnIndex) {
			case 0:
				return vdm.getResource().getName();
			case 1:
				return String.format("%d", vdm.getLineNumber());
			case 2:
				return getTypeVulnerabilityName(vdm.getTypeVulnerability());
			case 3:
				return vdm.getMessage();
			case 4:
				return vdm.getFullPath();
			default:
				return null;
		}
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}
}
