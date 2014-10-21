package hoten.serving;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MD5Hash {

    public String hash(byte[] bytes) {
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(bytes);
            byte[] messageDigest = algorithm.digest();
            StringBuilder hash = new StringBuilder();
            for (int i = 0; i < messageDigest.length; i++) {
                int num = 0xFF & messageDigest[i];
                String append = Integer.toHexString(num);
                if (append.length() == 1) {
                    append = "0" + append;
                }
                hash.append(append);
            }
            return hash.toString();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(MD5Hash.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
