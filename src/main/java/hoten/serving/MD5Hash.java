package hoten.serving;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MD5Hash {

    public byte[] hash(byte[] bytes) {
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(bytes);
            return algorithm.digest();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(MD5Hash.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
