package com.USWCicrcleLink.server.global.util.validator;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FileSignatureValidator {

    // 파일 시그니처 정의
    private static final Map<String, String> FILE_SIGNATURES = new HashMap<>();

    static {
        FILE_SIGNATURES.put("jpg", "FFD8FF"); // jpg
        FILE_SIGNATURES.put("jpeg", "FFD8FF"); // JPEG
        FILE_SIGNATURES.put("png", "89504E47"); // PNG
    }

    public static String getFileSignature(InputStream inputStream) throws IOException {
        byte[] fileHeader = new byte[4];
        inputStream.read(fileHeader, 0, 4);
        return bytesToHex(fileHeader);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static boolean isValidFileType(InputStream inputStream, String expectedExtension) throws IOException {
        String fileSignature = getFileSignature(inputStream);
        String expectedSignature = FILE_SIGNATURES.get(expectedExtension.toLowerCase());

        return expectedSignature != null && fileSignature.startsWith(expectedSignature);
    }
}

