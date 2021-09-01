package com.oracle.coherence.spring.configuration.session;

public class ClientSessionConfigurationBean extends SessionConfigurationBean {
	public ClientSessionConfigurationBean() {
		super.setType(SessionType.CLIENT);
	}
}
