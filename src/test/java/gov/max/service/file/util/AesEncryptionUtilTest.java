package gov.max.service.file.util;

import gov.max.service.file.util.AesEncryptionUtil;
import gov.max.service.file.util.EncryptionKey;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AesEncryptionUtilTest {

    @InjectMocks
    private AesEncryptionUtil encryptionUtil;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void generateKey() throws NoSuchAlgorithmException {
        // Prepare
        int keyLength = 16; // 128bit
        int ivLength = 16; // 128bit

        // Execute
        EncryptionKey key = encryptionUtil.generateKey();

        // Assert
        Assert.assertNotNull(key);

        Assert.assertEquals(keyLength + ivLength, key.getData().length);
    }

    @Test
    public void generateKeyFromData() throws NoSuchAlgorithmException {
        // Prepare
        byte[] data = new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};

        // Execute
        EncryptionKey key = encryptionUtil.generateKey(data);

        // Assert
        Assert.assertNotNull(key);
        Assert.assertArrayEquals(key.getData(), data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateKeyFromInvalidData() throws NoSuchAlgorithmException {
        // Prepare
        byte[] data = new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1};

        // Execute
        encryptionUtil.generateKey(data);
    }

    @Test
    public void encrypt() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        // Prepare
        String originalText = "Hello World";
        InputStream inputStream = IOUtils.toInputStream(originalText, Charset.forName("UTF-8"));
        EncryptionKey key = encryptionUtil.generateKey();

        // Execute
        InputStream encryptedStream = encryptionUtil.encryptStream(inputStream, key);

        // Assert
        String encryptedText = IOUtils.toString(encryptedStream, Charset.forName("UTF-8"));
        Assert.assertNotEquals(originalText, encryptedText);
    }

    @Test
    public void decrypt() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        // Prepare
        String originalText = "Hello World";
        InputStream inputStream = IOUtils.toInputStream(originalText, Charset.forName("UTF-8"));
        EncryptionKey key = encryptionUtil.generateKey();
        InputStream encryptedStream = encryptionUtil.encryptStream(inputStream, key);

        // Execute
        InputStream decryptedStream = encryptionUtil.decryptStream(encryptedStream, key);

        // Assert
        String decryptedText = IOUtils.toString(decryptedStream, Charset.forName("UTF-8"));
        Assert.assertEquals(originalText, decryptedText);
    }
}
