package org.cae.mail.entity;

public class MailMessage {

	private Integer type;
	private Mail mail;

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Mail getMail() {
		if(mail==null)
			return new Mail();
		return mail;
	}

	public void setMail(Mail mail) {
		this.mail = mail;
	}
}
