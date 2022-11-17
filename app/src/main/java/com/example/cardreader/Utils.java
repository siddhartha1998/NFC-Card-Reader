package com.example.cardreader;

public class Utils {

    public static byte[] hexToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    | Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    public static String byteArrayToHex(byte[] bytes) {

        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for(byte b: bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
