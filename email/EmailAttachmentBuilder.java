package com.go2group.synapse.email;

public abstract class EmailAttachmentBuilder<T extends EmailAttachmentParam> {
  public EmailAttachmentBuilder() {}
  
  public Object buildAttachment(T emailAttachmentParam) { return getAttachment(emailAttachmentParam); }
  
  public abstract Object getAttachment(T paramT);
}
