package gov.max.service.file.util;

import javax.crypto.NoSuchPaddingException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface EncryptionUtil {

    EncryptionKey generateKey() throws NoSuchAlgorithmException;
    EncryptionKey generateKey(byte[] keyData);
    InputStream encryptStream(InputStream inputStream, EncryptionKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException;
    InputStream decryptStream(InputStream inputStream, EncryptionKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException;
}
