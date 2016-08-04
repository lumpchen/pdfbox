package org.apache.pdfbox.accessibility;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.pdfbox.accessibility.check.CheckException;
import org.apache.pdfbox.accessibility.check.CheckProcess;
import org.apache.pdfbox.accessibility.check.MetadataCheckProcess;

public class UACheckConfiguration {

	public static final String META_DATA_PROCESS = "metadata-process";

	private final Map<String, Class<? extends CheckProcess>> processes = new LinkedHashMap<String, Class<? extends CheckProcess>>();

	public static UACheckConfiguration createPdfUA1Configuration() {
		UACheckConfiguration conf = new UACheckConfiguration();

		conf.replaceProcess(META_DATA_PROCESS, MetadataCheckProcess.class);

		return conf;
	}

	public void replaceProcess(String processName, Class<? extends CheckProcess> process) {
		if (process == null) {
			removeProcess(processName);
		} else {
			this.processes.put(processName, process);
		}
	}

	public void removeProcess(String processName) {
		this.processes.remove(processName);
	}
	
	public Collection<String> getProcessNames() {
        return this.processes.keySet();
    }

	public CheckProcess getProcessInstance(String name) throws CheckException {
		if (this.processes.containsKey(name)) {
			Class<? extends CheckProcess> process = this.processes.get(name);
			try {
				return process.newInstance();
			} catch (InstantiationException e) {
				throw new CheckException(e);
			} catch (IllegalAccessException e) {
				throw new CheckException(e);
			}
		}
		throw new CheckException(name + " class is missing!");
	}
	
}
