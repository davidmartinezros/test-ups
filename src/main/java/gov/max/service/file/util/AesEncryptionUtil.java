package gov.max.service.file.util;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AesEncryptionUtil implements EncryptionUtil {

    public static final String CIPHER = "AES";
    public static final String CIPHER_CONFIG = "AES/CBC/PKCS5Padding";
    public static final int KEY_LENGTH = 128;

    private SecureRandom secureRandom = new SecureRandom();

    @Override
    public EncryptionKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(CIPHER);
        keyGen.init(KEY_LENGTH);
        byte[] key = keyGen.generateKey().getEncoded();
        byte[] iv = new byte[KEY_LENGTH/8];
        secureRandom.nextBytes(iv);

        return new AesEncryptionKey(key, iv);
    }

    @Override
    public EncryptionKey generateKey(byte[] keyData) {
        return new AesEncryptionKey(keyData);
    }

    @Override
    public InputStream encryptStream(InputStream inputStream, EncryptionKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {
        AesEncryptionKey aesKey = (AesEncryptionKey)key;
        Cipher cipher = Cipher.getInstance(CIPHER_CONFIG);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey.key, CIPHER), new IvParameterSpec(aesKey.iv));

        return new CipherInputStream(inputStream, cipher);
    }

    @Override
    public InputStream decryptStream(InputStream inputStream, EncryptionKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {
        AesEncryptionKey aesKey = (AesEncryptionKey)key;
        Cipher cipher = Cipher.getInstance(CIPHER_CONFIG);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey.key, CIPHER), new IvParameterSpec(aesKey.iv));

        return new CipherInputStream(inputStream, cipher);
    }

    private class AesEncryptionKey implements EncryptionKey {
        public byte[] key;
        public byte[] iv;

        public AesEncryptionKey(byte[] key, byte[] iv) {
            this.key = key;
            this.iv = iv;
        }

        public AesEncryptionKey(byte[] data) {
            this.key = new byte[KEY_LENGTH/8];
            this.iv = new byte[KEY_LENGTH/8];

            if(data.length != key.length + iv.length) {
                throw new IllegalArgumentException();
            } else {
                System.arraycopy(data, 0, key, 0, key.length);
                System.arraycopy(data, key.length, iv, 0, iv.length);
            }
        }

        @Override
        public byte[] getData() {
            byte[] data = new byte[key.length+iv.length];
            System.arraycopy(key, 0, data, 0, key.length);
            System.arraycopy(iv, 0, data, key.length, iv.length);

            return data;
        }
    }
}
