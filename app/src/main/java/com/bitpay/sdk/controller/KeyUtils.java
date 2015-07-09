package com.bitpay.sdk.controller;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import com.google.bitcoin.core.Base58;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.ECKey.ECDSASignature;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.Utils;

public class KeyUtils {
    final private static char[] hexArray = "0123456789abcdef".toCharArray();
    final private static String PRIV_KEY_FILENAME = "bitpay_private.key";
    final private static String PUBL_KEY_FILENAME = "bitpay_public.key";

    public KeyUtils() {
    }

    public static boolean privateKeyExists(Context context) {
        return context.getFileStreamPath(PRIV_KEY_FILENAME).exists();
    }

    public static ECKey createEcKey() {
        //Default constructor uses SecureRandom numbers.
        return new ECKey();
    }

    public static ECKey createEcKeyFromHexString(String privateKey) {
        BigInteger privKey = new BigInteger(privateKey, 16);
        return new ECKey(privKey, null, true);
    }

    /**
     * Convenience method.
     */
    public static ECKey createEcKeyFromHexStringFile(String privKeyFile) throws IOException {
        String privateKey = getKeyStringFromFile(privKeyFile);
        return createEcKeyFromHexString(privateKey);
    }

    public static ECKey loadEcKey(Context context) throws IOException {
        byte[] privBytes = loadKeyFromFile(PRIV_KEY_FILENAME, context);
        byte[] publBytes = loadKeyFromFile(PUBL_KEY_FILENAME, context);
        ECKey key = new ECKey(privBytes, publBytes);
        return key;
    }

    private static byte[] loadKeyFromFile(String filename, Context context) throws IOException {
        FileInputStream fileInputStream;
        File file = context.getFileStreamPath(filename);
        byte[] bytes = new byte[(int) file.length()];
        fileInputStream = context.openFileInput(filename);
        fileInputStream.read(bytes, 0, (int) file.length());
        fileInputStream.close();
        return bytes;
    }

    public static String getKeyStringFromFile(String filename) throws IOException {
        BufferedReader br;
        br = new BufferedReader(new FileReader(filename));
        String line = br.readLine();
        br.close();
        return line;
    }

    public static void saveEcKey(ECKey ecKey, Context context) throws IOException {
        byte[] privBytes = ecKey.getPrivKeyBytes();
        byte[] publBytes = ecKey.getPubKey();
        FileOutputStream output = context.openFileOutput(PRIV_KEY_FILENAME, Context.MODE_PRIVATE);
        output.write(privBytes);
        output.close();
        output = context.openFileOutput(PUBL_KEY_FILENAME, Context.MODE_PRIVATE);
        output.write(publBytes);
        output.close();
    }

    public static String deriveSIN(ECKey ecKey) throws IllegalArgumentException {
        // Get sha256 hash and then the RIPEMD-160 hash of the public key (this call gets the result in one step).
        byte[] pubKeyHash = ecKey.getPubKeyHash();
        // Convert binary pubKeyHash, SINtype and version to Hex
        String version = "0F";
        String SINtype = "02";
        String pubKeyHashHex = bytesToHex(pubKeyHash);
        // Concatenate all three elements
        String preSIN = version + SINtype + pubKeyHashHex;
        // Convert the hex string back to binary and double sha256 hash it leaving in binary both times
        byte[] preSINbyte = hexToBytes(preSIN);
        byte[] hash2Bytes = Utils.doubleDigest(preSINbyte);
        // Convert back to hex and take first four bytes
        String hashString = bytesToHex(hash2Bytes);
        String first4Bytes = hashString.substring(0, 8);
        // Append first four bytes to fully appended SIN string
        String unencoded = preSIN + first4Bytes;
        byte[] unencodedBytes = new BigInteger(unencoded, 16).toByteArray();
        return Base58.encode(unencodedBytes);
    }

    public static String sign(ECKey key, String input) {
        byte[] data = input.getBytes();
        Sha256Hash hash = Sha256Hash.create(data);
        ECDSASignature sig = key.sign(hash, null);
        byte[] bytes = sig.encodeToDER();
        return bytesToHex(bytes);
    }

    private static int getHexVal(char hex) {
        int val = (int) hex;
        return val - (val < 58 ? 48 : (val < 97 ? 55 : 87));
    }

    public static byte[] hexToBytes(String hex) throws IllegalArgumentException {
        char[] hexArray = hex.toCharArray();
        if (hex.length() % 2 == 1) {
            throw new IllegalArgumentException("Error: The binary key cannot have an odd number of digits");
        }
        byte[] arr = new byte[hex.length() >> 1];
        for (int i = 0; i < hex.length() >> 1; ++i) {
            arr[i] = (byte) ((getHexVal(hexArray[i << 1]) << 4) + (getHexVal(hexArray[(i << 1) + 1])));
        }
        return arr;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    public static String exportPrivateKeyToHexa(ECKey ecKey) {
        return new String(ecKey.toASN1());
    }
}
