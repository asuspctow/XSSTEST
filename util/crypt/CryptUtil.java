package com.go2group.synapse.util.crypt;

import java.security.Key;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.log4j.Logger;

public class CryptUtil
{
  private static final Logger log = Logger.getLogger(CryptUtil.class);
  
  private static final Key aesKey = new SecretKeySpec("9199947956431431".getBytes(), "AES");
  
  public CryptUtil() {}
  
  public static String crypt(String text) { try { Cipher cipher = Cipher.getInstance("AES");
      
      cipher.init(1, aesKey);
      byte[] encrypted = cipher.doFinal(text.getBytes());
      
      return Base64.getEncoder().encodeToString(encrypted);
    } catch (Exception e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage()); }
    return "";
  }
  
  public static String decrypt(String cryptedText)
  {
    try {
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(2, aesKey);
      return new String(cipher.doFinal(Base64.getDecoder().decode(cryptedText)));
    } catch (Exception e) {
      log.debug(e.getMessage(), e);
      log.error(e.getMessage()); }
    return "";
  }
}
