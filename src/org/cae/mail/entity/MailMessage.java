package org.cae.mail.entity;

public class MailMessage {

	private Enum<MailType> type;
	private Mail mail;

	public Enum<MailType> getType() {
		return type;
	}

	public void setType(Enum<MailType> type) {
		this.type = type;
	}

	public Mail getMail() {
		return mail;
	}

	public void setMail(Mail mail) {
		this.mail = mail;
	}
}
