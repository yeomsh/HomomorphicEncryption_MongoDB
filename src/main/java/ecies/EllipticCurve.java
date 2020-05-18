package ecies;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import djb.Curve25519;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import util.KeyGenerator;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

/**
 *
 * @author sjdonado
 */
public class EllipticCurve {
    ECIES ecies;

    public EllipticCurve(ECIES ecies) {
        this.ecies = ecies;
    }

    /**
     *
     * @return byte[][]{ byte[] privateKey, byte[] publicKey }
     */

    public byte[][] generateKeyPair() throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeySpecException {
        //KeyGenerator KG = new KeyGenerator();
        Security.addProvider(new BouncyCastleProvider());
        //File keyFile = new File("ECIESprivate.pem");
        byte[] privateKey, publicKey;
        //if (!keyFile.exists()) {
            System.out.println("keyfile isn't exist");
            privateKey = ecies.getRandomNumber(ecies.getKeySize());
            publicKey = new byte[ecies.getKeySize()];
            Curve25519.keygen(publicKey, null, privateKey);
            //KG.writeECIESKey(privateKey,publicKey);
        //}
//        else {
//            privateKey = KG.readECIESPrivateKeyFromPemFile("ECIESprivate.pem");
//            publicKey = KG.readECIESPublicKeyFromPemFile("ECIESpublic.pem");
//        }

        return new byte[][]{ privateKey, publicKey };
    }

    /**
     *
     * @param r byte[]
     * @return byte[]
     */
    public byte[] generateR(byte[] r) {
        byte[] R = new byte[r.length];
        Curve25519.curve(R, r, null);
        return R;
    }

    /**
     *
     * @param r byte[]
     * @param publicKey byte[]
     * @return byte[]
     */
    public byte[] encryptionPoint(byte[] r, byte[] publicKey) {
        byte[] Z = new byte[ecies.getKeySize()];
        Curve25519.curve(Z, r, publicKey);
        return Z;
    }

    /**
     *
     * @param R byte[]
     * @param privateKey byte[]
     * @return byte[]
     */
    public byte[] decryptionPoint(byte[] R, byte[] privateKey) {
        byte[] Z = new byte[ecies.getKeySize()];
        Curve25519.curve(Z, privateKey, R);
        return Z;
    }
}
