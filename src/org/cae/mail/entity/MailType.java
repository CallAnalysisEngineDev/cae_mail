package org.cae.mail.entity;

public enum MailType {
	NORMAL_ADVICE;
	public static boolean contains(String type){    
        for(MailType mailType : MailType.values()){    
            if(mailType.name().equals(type)){    
                return true;    
            }    
        }    
        return false;    
    }    
}
