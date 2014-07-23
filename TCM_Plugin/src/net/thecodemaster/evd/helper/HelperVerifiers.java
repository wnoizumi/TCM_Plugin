package net.thecodemaster.evd.helper;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.logger.PluginLogger;
import net.thecodemaster.evd.ui.l10n.Message;

/**
 * @author Luciano Sampaio
 */
public abstract class HelperVerifiers {

	public static String getFilePath(int fileId) {
		switch (fileId) {
			case Constant.VERIFIER_ID_COMMAND_INJECTION:
				return Constant.File.FILE_EXIT_POINT_COMMAND_INJECTION;
			case Constant.VERIFIER_ID_COOKIE_POISONING:
				return Constant.File.FILE_EXIT_POINT_COOKIE_POISONING;
			case Constant.VERIFIER_ID_CROSS_SITE_SCRIPTING:
				return Constant.File.FILE_EXIT_POINT_CROSS_SITE_SCRIPTING;
			case Constant.VERIFIER_ID_HTTP_RESPONSE_SPLITTING:
				return Constant.File.FILE_EXIT_POINT_HTTP_RESPONSE_SPLITTING;
			case Constant.VERIFIER_ID_LOG_FORGING:
				return Constant.File.FILE_EXIT_POINT_LOG_FORGING;
			case Constant.VERIFIER_ID_PATH_TRAVERSAL:
				return Constant.File.FILE_EXIT_POINT_PATH_TRAVERSAL;
			case Constant.VERIFIER_ID_REFLECTION_INJECTION:
				return Constant.File.FILE_EXIT_POINT_REFLECTION_INJECTION;
			case Constant.VERIFIER_ID_SECURITY_MISCONFIGURATION:
				return Constant.File.FILE_EXIT_POINT_SECURITY_MISCONFIGURATION;
			case Constant.VERIFIER_ID_SQL_INJECTION:
				return Constant.File.FILE_EXIT_POINT_SQL_INJECTION;
			default:
				String errorMessage = String.format(Message.Error.FILE_PATH_ID_NOT_FOUND, fileId);
				PluginLogger.logError(errorMessage, null);
				return null;
		}
	}

	public static String getTypeVulnerabilityName(int typeVulnerability) {
		switch (typeVulnerability) {
		// Main verifier vulnerabilities.
			case Constant.VERIFIER_ID_COMMAND_INJECTION:
				return Message.Plugin.VERIFIER_NAME_COMMAND_INJECTION;
			case Constant.VERIFIER_ID_COOKIE_POISONING:
				return Message.Plugin.VERIFIER_NAME_COOKIE_POISONING;
			case Constant.VERIFIER_ID_CROSS_SITE_SCRIPTING:
				return Message.Plugin.VERIFIER_NAME_CROSS_SITE_SCRIPTING;
			case Constant.VERIFIER_ID_HTTP_RESPONSE_SPLITTING:
				return Message.Plugin.VERIFIER_NAME_HTTP_RESPONSE_SPLITTING;
			case Constant.VERIFIER_ID_LOG_FORGING:
				return Message.Plugin.VERIFIER_NAME_LOG_FORGING;
			case Constant.VERIFIER_ID_PATH_TRAVERSAL:
				return Message.Plugin.VERIFIER_NAME_PATH_TRAVERSAL;
			case Constant.VERIFIER_ID_REFLECTION_INJECTION:
				return Message.Plugin.VERIFIER_NAME_REFLECTION_INJECTION;
			case Constant.VERIFIER_ID_SECURITY_MISCONFIGURATION:
				return Message.Plugin.VERIFIER_NAME_SECURITY_MISCONFIGURATION;
			case Constant.VERIFIER_ID_SQL_INJECTION:
				return Message.Plugin.VERIFIER_NAME_SQL_INJECTION;

				// Sub-Vulnerabilities' types.
			case Constant.Vulnerability.ENTRY_POINT:
				return Message.VerifierSecurityVulnerability.ENTRY_POINT;
			case Constant.Vulnerability.UNKNOWN:
				return Message.VerifierSecurityVulnerability.UNKNOWN;
			case Constant.Vulnerability.SECURITY_MISCONFIGURATION_HARD_CODED_CONTENT:
				return Message.VerifierSecurityVulnerability.SECURITY_MISCONFIGURATION_HARD_CODED_CONTENT;
			case Constant.Vulnerability.SQL_INJECTION_STRING_CONCATENATION:
				return Message.VerifierSecurityVulnerability.SQL_INJECTION_STRING_CONCATENATION;

			default:
				String errorMessage = String.format(Message.Error.TYPE_VULNERABILITY_NOT_FOUND, typeVulnerability);
				PluginLogger.logError(errorMessage, null);
				return null;
		}
	}
}
