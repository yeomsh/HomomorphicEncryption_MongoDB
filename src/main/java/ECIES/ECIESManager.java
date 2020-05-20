package ECIES;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.bouncycastle.util.encoders.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.security.SecureRandom;

/**
 *
 * @author sjdonado
 */
public class ECIESManager extends javax.swing.JFrame {

    private final ECIES ecies;
    EllipticCurve ellipticCurve;
    byte[] recipientPrivateKey;

    byte[] IV = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    public ECIESManager() {
        this.ecies = new ECIES();
        this.ellipticCurve = new EllipticCurve(ecies);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField recepientPublicKeyText;
    private javax.swing.JTextArea recipientCipherTextArea;
    private javax.swing.JButton recipientDecryptButton;
    private javax.swing.JTextArea recipientDecryptionResultTextArea;
    private javax.swing.JButton recipientGenerateKeysButton;
    private javax.swing.JTextArea senderCipherTextArea;
    private javax.swing.JButton senderEncryptButton;
    private javax.swing.JTextArea senderPlainTextArea;
    private javax.swing.JTextField senderRecipientPublicKeyEditText;
    // End of variables declaration//GEN-END:variables

    //랜덤한 IV생성
    public static byte[] makeIV(){
        byte[] IV = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(IV);
        return IV;
    }

    public JSONObject decryptCipherContract(byte[] cipher, byte[] recipientPrivateKey, byte[] IV) throws Exception {
        byte[] cipherText = cipher;
        byte[] receiverR = new byte[ecies.getKeySize()];
        System.arraycopy(cipherText, 0, receiverR, 0, ecies.getKeySize());

        byte[] receiverChiperText = new byte[cipherText.length - (receiverR.length + ecies.getKeySize())];
        System.arraycopy(cipherText, receiverR.length + ecies.getKeySize(), receiverChiperText, 0, cipherText.length - (receiverR.length + ecies.getKeySize()));

        byte[] receiverTag = new byte[ecies.getKeySize()];
        System.arraycopy(cipherText, ecies.getKeySize(), receiverTag, 0, ecies.getKeySize());

        byte[] decryptionPoint = ellipticCurve.decryptionPoint(receiverR, recipientPrivateKey);
        byte[] plainText = ecies.decrypt(decryptionPoint, IV, receiverChiperText, receiverTag);
        String contractString = new String(plainText);
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(contractString);
    }


    //암호문 encrypt : 받는 사람의 공개키를 이용해서
    public byte[] senderEncrypt(String publicKey, String plainText, byte[] IV) {
        byte[] recipientPublicKey = Base64.decode(publicKey);
        byte[] r = ecies.getRandomNumber(ecies.getKeySize());
        byte[] R = ellipticCurve.generateR(r);
        byte[] encryptionPoint = ellipticCurve.encryptionPoint(r, recipientPublicKey);
        byte[] chiperText = ecies.encrypt(encryptionPoint, IV, plainText.getBytes());

        byte[] result = new byte[R.length + chiperText.length];
        System.arraycopy(R, 0, result, 0, R.length);
        System.arraycopy(chiperText, 0, result, R.length, chiperText.length);

        return result;
    }

}
