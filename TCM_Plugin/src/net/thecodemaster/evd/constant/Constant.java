package net.thecodemaster.evd.constant;

import net.thecodemaster.evd.Activator;

/**
 * This class contains constants used by the application.
 * 
 * @author Luciano Sampaio
 */
public abstract class Constant {

	public static final boolean	IS_DEBUGGING													= true;
	public static final String	JDT_NATURE														= "org.eclipse.jdt.core.javanature";
	public static final String	NATURE_ID															= Activator.PLUGIN_ID + ".TCM_EVD_NATURE";
	public static final String	BUILDER_ID														= Activator.PLUGIN_ID + ".TCM_EVD_BUILDER";
	public static final String	MARKER_ID															= Activator.PLUGIN_ID + ".TCM_EVD_MARKER";
	public static final String	VIEW_ID																= Activator.PLUGIN_ID + ".TCM_EVD_VIEW";

	public static final int			VERIFIER_ID_COOKIE_POISONING					= 1;
	public static final int			VERIFIER_ID_CROSS_SITE_SCRIPTING			= 2;
	public static final int			VERIFIER_ID_SECURITY_MISCONFIGURATION	= 3;
	public static final int			VERIFIER_ID_SQL_INJECTION							= 4;
	public static final int			VERIFIER_ID_UNVALIDATED_REDIRECTING		= 5;

	public static final String	SEPARATOR															= ";";
	public static final String	RESOURCE_TYPE_TO_PERFORM_DETECTION		= "java" + SEPARATOR + "jsp";
	public static final int			MAXIMUM_DEPTH													= 10;

	public static final String	OBJECT																= "java.lang.Object";
	public static final int			LITERAL																= 1;

	public abstract class Package {
		public static final String	UI						= "net.thecodemaster.evd.ui";
		public static final String	L10N_MESSAGES	= UI + ".l10n.message";
	}

	public abstract class Folder {
		public static final String	ICON						= "icon/";
		public static final String	KNOWLEDGE_BASE	= "knowledge_base/";
		public static final String	ENTRY_POINT			= KNOWLEDGE_BASE + "entry_point/";
		public static final String	EXIT_POINT			= KNOWLEDGE_BASE + "exit_point/";
	}

	public abstract class File {
		public static final String	FILE_ENTRY_POINT													= Folder.ENTRY_POINT + "entry_point.xml";
		public static final String	FILE_EXIT_POINT_COOKIE_POISONING					= Folder.EXIT_POINT + "cookie_poisoning.xml";
		public static final String	FILE_EXIT_POINT_CROSS_SITE_SCRIPTING			= Folder.EXIT_POINT
																																							+ "cross_site_scripting.xml";
		public static final String	FILE_EXIT_POINT_SECURITY_MISCONFIGURATION	= Folder.EXIT_POINT
																																							+ "security_misconfiguration.xml";
		public static final String	FILE_EXIT_POINT_SQL_INJECTION							= Folder.EXIT_POINT + "sql_injection.xml";
		public static final String	FILE_EXIT_POINT_UNVALIDATED_REDIRECTING		= Folder.EXIT_POINT
																																							+ "unvalidated_redirecting.xml";
	}

	public abstract class Icons {
		public static final String	SECURITY_VULNERABILITY	= Folder.ICON + "SecurityVulnerability.png";
	}

	public abstract class PrefPageSecurityVulnerability {
		public static final String	FIELD_COOKIE_POISONING					= Activator.PLUGIN_ID + ".CookiePoisoning";
		public static final String	FIELD_CROSS_SITE_SCRIPTING			= Activator.PLUGIN_ID + ".CrossSiteScripting";
		public static final String	FIELD_SECURITY_MISCONFIGURATION	= Activator.PLUGIN_ID + ".SecurityMisconfiguration";
		public static final String	FIELD_SQL_INJECTION							= Activator.PLUGIN_ID + ".SQLInjection";
		public static final String	FIELD_UNVALIDATED_REDIRECTING		= Activator.PLUGIN_ID + ".UnvalidatedRedirecting";

		public static final String	FIELD_MONITORED_PROJECTS				= Activator.PLUGIN_ID + ".MonitoredProjects";
	}

	public abstract class PrefPageSettings {
		public static final String	ID_PAGE											= Package.UI + ".PREFERENCE.PAGE.SETTINGS";

		public static final String	FIELD_RUN_MODE							= Activator.PLUGIN_ID + ".RunMode";

		public static final String	FIELD_OUTPUT_PROBLEMS_VIEW	= Activator.PLUGIN_ID + ".ProblemsView";
		public static final String	FIELD_OUTPUT_TEXT_FILE			= Activator.PLUGIN_ID + ".TextFile";
		public static final String	FIELD_OUTPUT_XML_FILE				= Activator.PLUGIN_ID + ".XmlFile";
	}

	public abstract class Marker {
		public static final String	TYPE_SECURITY_VULNERABILITY	= "TCM_EVD_TYPE_SECURITY_VULNERABILITY";
	}

	public abstract class XMLLoader {
		public static final String	TAG_ENTRY_POINT				= "entrypoint";
		public static final String	TAG_EXIT_POINT				= "exitpoint";
		public static final String	TAG_QUALIFIED_NAME		= "qualifiedname";
		public static final String	TAG_METHOD_NAME				= "methodname";
		public static final String	TAG_PARAMETERS				= "parameters";
		public static final String	TAG_PARAMETERS_TYPE		= "type";
		public static final String	TAG_PARAMETERS_RULES	= "rules";
	}

}
