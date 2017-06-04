package org.cae.mail.controller;

import org.cae.mail.entity.MailMessage;

public interface IMailController {

	void sendMailController(MailMessage mailMessage);
}
