package org.cae.mail.entity;

public class MailMessage {

	private MailType type;
	private Mail mail;

	public MailType getType() {
		return type;
	}

	public void setType(MailType type) {
		this.type = type;
	}

	public Mail getMail() {
		return mail;
	}

	public void setMail(Mail mail) {
		this.mail = mail;
	}
}
