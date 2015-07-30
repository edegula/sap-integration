package com.adapter.usermodule;

import java.rmi.RemoteException;
import java.util.Enumeration;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.aii.af.lib.mp.module.ModuleException;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.MessagePropertyKey;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditAccess;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogStatus;

/**
 * 
 */
public class GetDynamicConfigurationBean implements SessionBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2715668259544405736L;
	private static final String C_MODE_DC2MD = "DynamicConfiguration_to_ModuleData";
	private static final String C_MODE_MD2DC = "ModuleData_to_DynamicConfiguration";
	private static final String C_PARAM_MODE = "mode";
	private static final String C_KEY_SEPARATOR = "#";
	MessageKey amk = null;
	AuditAccess audit = null;

	public ModuleData process(ModuleContext moduleContext,
			ModuleData inputModuleData) throws ModuleException {
		String mode = "";
		Message msg;

		try {
			msg = (Message) inputModuleData.getPrincipalData();
			amk = new MessageKey(msg.getMessageId(), msg.getMessageDirection());
			addInfo("GetDynamicConfigurationBean begin...");
			try {
				mode = moduleContext.getContextData(C_PARAM_MODE);
				if (mode != null && !mode.equals(C_MODE_DC2MD)
						&& !mode.equals(C_MODE_MD2DC)) {
					mode = C_MODE_DC2MD;
				}
			} catch (Exception e) {
				mode = C_MODE_DC2MD;
			}
			if (mode.equals(C_MODE_DC2MD)) {
				addInfo("GetDynamicConfigurationBean: mode - " + C_MODE_DC2MD);
				dcToMD(inputModuleData);
			} else if (mode.equals(C_MODE_MD2DC)) {
				addInfo("GetDynamicConfigurationBean: mode - " + C_MODE_MD2DC);
				mdToDC(inputModuleData);
			}
			addInfo("GetDynamicConfigurationBean end...");
		} catch (Exception e) {
			throw new ModuleException(
					"GetDynamicConfigurationBean: Exception found: "
							+ e.getMessage(), e);
		}

		return inputModuleData;
	}

	@SuppressWarnings("unchecked")
	private void mdToDC(ModuleData inputModuleData) throws ModuleException {
		Message msg = null;
		MessagePropertyKey key;
		String propertyName = "";
		String propertyNamespace = "";
		String supplementalDataName;
		String supplementalData;
		String[] str;
		try {
			msg = (Message) inputModuleData.getPrincipalData();
			Enumeration<String> supplementalDataNamesList = inputModuleData
					.getSupplementalDataNames();
			while (supplementalDataNamesList.hasMoreElements()) {
				supplementalDataName = supplementalDataNamesList.nextElement();
				supplementalData = (String) inputModuleData
						.getSupplementalData(supplementalDataName);
				str = supplementalDataName.split(C_KEY_SEPARATOR);
				if (str.length > 1) {
					propertyName = str[0];
					propertyNamespace = str[1];
					key = new MessagePropertyKey(propertyName,
							propertyNamespace);
					msg.setMessageProperty(key, supplementalData);
				}
			}
			inputModuleData.setPrincipalData(msg);
		} catch (Exception e) {
			throw new ModuleException(e.getMessage(), e);
		}
	}

	private void dcToMD(ModuleData inputModuleData) throws ModuleException {
		Message msg = null;
		String propertyName = "";
		String propertyNamespace = "";
		String supplementalDataName;
		String supplementalData;
		try {
			msg = (Message) inputModuleData.getPrincipalData();
			for (MessagePropertyKey key : msg.getMessagePropertyKeys()) {
				propertyName = key.getPropertyName();
				propertyNamespace = key.getPropertyNamespace();
				supplementalDataName = propertyName + C_KEY_SEPARATOR
						+ propertyNamespace;
				supplementalData = msg.getMessageProperty(key);
				inputModuleData.setSupplementalData(supplementalDataName,
						supplementalData);
			}
		} catch (Exception e) {
			throw new ModuleException(e.getMessage(), e);
		}
	}

	private void addInfo(String msg) throws Exception {
		try {
			audit.addAuditLogEntry(amk, AuditLogStatus.SUCCESS, msg);
		} catch (Exception e) {
			throw new Exception(e.getMessage(), e);
		}
	}

	private void addWarning(String msg) throws Exception {
		try {
			audit.addAuditLogEntry(amk, AuditLogStatus.WARNING, msg);
		} catch (Exception e) {
			throw new Exception(e.getMessage(), e);
		}
	}

	private void addError(String msg) throws Exception {
		try {
			audit.addAuditLogEntry(amk, AuditLogStatus.ERROR, msg);
		} catch (Exception e) {
			throw new Exception(e.getMessage(), e);
		}
	}
	public void ejbActivate() throws EJBException, RemoteException {
	}

	public void ejbPassivate() throws EJBException, RemoteException {
	}

	public void ejbRemove() throws EJBException, RemoteException {
	}

	public void setSessionContext(SessionContext arg0) throws EJBException,
	RemoteException {
	}

	public void ejbCreate() throws javax.ejb.CreateException {
	}
}
