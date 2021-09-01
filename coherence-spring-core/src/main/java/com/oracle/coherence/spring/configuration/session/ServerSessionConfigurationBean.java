package com.oracle.coherence.spring.configuration.session;

public class ServerSessionConfigurationBean extends SessionConfigurationBean {
	public ServerSessionConfigurationBean() {
		super.setType(SessionType.SERVER);
	}
}
