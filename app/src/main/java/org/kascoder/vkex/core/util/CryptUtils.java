package org.kascoder.vkex.core.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.MessageDigest;

import lombok.experimental.UtilityClass;
import org.apache.commons.codec.binary.Base64;

@UtilityClass
public class CryptUtils {
    private final String INITIAL_VECTOR = "0135798642951260";

    private String md5(final String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes());
        BigInteger number = new BigInteger(1, messageDigest);
        return String.format("%032x", number);
    }

    private Cipher initCipher(final int mode, final String secretKey) throws Exception {
        SecretKey secret =  new SecretKeySpec(md5(secretKey).getBytes("UTF8"), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec initialVector = new IvParameterSpec(INITIAL_VECTOR.getBytes());
        cipher.init(mode, secret, initialVector);
        return cipher;
    }

    public String encrypt(final String dataToEncrypt, final String secretKey) {
        if (dataToEncrypt == null || secretKey == null) {
            return null;
        }
        String encryptedData = null;
        try {
            Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedByteArray = cipher.doFinal(dataToEncrypt.getBytes("UTF8"));
            encryptedData = (new Base64()).encodeToString(encryptedByteArray);
        } catch (Exception e) {
            System.err.println("Problem encrypting the data");
            e.printStackTrace();
        }
        return encryptedData;
    }

    public String decrypt(final String encryptedData, final String secretKey) {
        if (encryptedData == null || secretKey == null) {
            return null;
        }
        String decryptedData = null;
        try {
            Cipher cipher = initCipher(Cipher.DECRYPT_MODE, secretKey);
            byte[] encryptedByteArray = (new Base64()).decode(encryptedData);
            byte[] decryptedByteArray = cipher.doFinal(encryptedByteArray);
            decryptedData = new String(decryptedByteArray, "UTF8");
        } catch (Exception e) {
            System.err.println("Problem decrypting the data");
            e.printStackTrace();
        }
        return decryptedData;
    }
}
