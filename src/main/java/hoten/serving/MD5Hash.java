package hoten.serving;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MD5Hash {
    
    public static void main(String[] args) throws UnsupportedEncodingException {
        MD5Hash h = new MD5Hash();
        byte[] result = h.hash("Test".getBytes());
        System.out.println(Arrays.toString(result));
    }

    public byte[] hash(byte[] bytes) {
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(bytes);
            byte[] messageDigest = algorithm.digest();
            return messageDigest;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(MD5Hash.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
