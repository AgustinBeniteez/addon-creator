package com.agustinbenitez.addoncreator.utils;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import java.io.*;

public class TgaImageLoader {

    public static WritableImage loadTga(File file) throws IOException {
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            return read(is);
        }
    }

    private static WritableImage read(InputStream is) throws IOException {
        // Read Header (18 bytes)
        byte[] header = new byte[18];
        int bytesRead = 0;
        while (bytesRead < 18) {
            int count = is.read(header, bytesRead, 18 - bytesRead);
            if (count == -1)
                throw new IOException("Invalid TGA file: Header too short");
            bytesRead += count;
        }

        int idLen = header[0] & 0xFF;
        int colorMapType = header[1] & 0xFF;
        int imageType = header[2] & 0xFF;

        // Skip ID if present
        if (idLen > 0)
            is.skip(idLen);

        // Skip Color Map if present (we don't support color mapped TGA for now)
        if (colorMapType == 1) {
            int cmLength = (header[5] & 0xFF) | ((header[6] & 0xFF) << 8);
            int cmEntrySize = header[7] & 0xFF;
            int cmBytes = (cmLength * cmEntrySize + 7) / 8;
            is.skip(cmBytes);
        }

        int width = (header[12] & 0xFF) | ((header[13] & 0xFF) << 8);
        int height = (header[14] & 0xFF) | ((header[15] & 0xFF) << 8);
        int bpp = header[16] & 0xFF;
        int descriptor = header[17] & 0xFF;

        boolean topToBottom = (descriptor & 0x20) != 0;

        // Supported types: 2 (Uncompressed RGB), 10 (RLE RGB), 3 (Uncompressed Gray)
        if (imageType != 2 && imageType != 10 && imageType != 3) {
            throw new IOException("Unsupported TGA Image Type: " + imageType);
        }

        if (width <= 0 || height <= 0)
            throw new IOException("Invalid dimensions: " + width + "x" + height);

        WritableImage image = new WritableImage(width, height);
        PixelWriter pw = image.getPixelWriter();

        int bytesPerPixel = bpp / 8;
        if (bytesPerPixel < 1 || bytesPerPixel > 4)
            throw new IOException("Unsupported depth: " + bpp);

        // Read pixel data
        int totalPixels = width * height;
        // Check for integer overflow in array size
        if (totalPixels > 100_000_000)
            throw new IOException("Image too large");

        int[] pixels = new int[totalPixels];

        if (imageType == 2 || imageType == 3) {
            // Uncompressed
            byte[] buf = new byte[totalPixels * bytesPerPixel];
            int readTotal = 0;
            while (readTotal < buf.length) {
                int r = is.read(buf, readTotal, buf.length - readTotal);
                if (r == -1)
                    break;
                readTotal += r;
            }

            for (int i = 0; i < totalPixels; i++) {
                int offset = i * bytesPerPixel;
                // Safety check
                if (offset + bytesPerPixel <= buf.length) {
                    pixels[i] = readPixel(buf, offset, bpp);
                }
            }
        } else if (imageType == 10) {
            // RLE Compressed
            int pixelIdx = 0;
            byte[] pixelBuf = new byte[bytesPerPixel];

            while (pixelIdx < totalPixels) {
                int packetHeader = is.read();
                if (packetHeader == -1)
                    break;

                boolean isRunLen = (packetHeader & 0x80) != 0;
                int count = (packetHeader & 0x7F) + 1;

                if (isRunLen) {
                    // RLE packet: read next pixel and repeat 'count' times
                    int read = 0;
                    while (read < bytesPerPixel) {
                        int r = is.read(pixelBuf, read, bytesPerPixel - read);
                        if (r == -1)
                            break;
                        read += r;
                    }

                    int color = readPixel(pixelBuf, 0, bpp);
                    for (int j = 0; j < count && pixelIdx < totalPixels; j++) {
                        pixels[pixelIdx++] = color;
                    }
                } else {
                    // Raw packet: read 'count' pixels
                    for (int j = 0; j < count && pixelIdx < totalPixels; j++) {
                        int read = 0;
                        while (read < bytesPerPixel) {
                            int r = is.read(pixelBuf, read, bytesPerPixel - read);
                            if (r == -1)
                                break;
                            read += r;
                        }
                        pixels[pixelIdx++] = readPixel(pixelBuf, 0, bpp);
                    }
                }
            }
        }

        // Write to WritableImage handling orientation
        if (topToBottom) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int idx = y * width + x;
                    if (idx < pixels.length) {
                        pw.setArgb(x, y, pixels[idx]);
                    }
                }
            }
        } else {
            // Bottom-Up
            for (int y = 0; y < height; y++) {
                int targetY = height - 1 - y;
                for (int x = 0; x < width; x++) {
                    int idx = y * width + x;
                    if (idx < pixels.length) {
                        pw.setArgb(x, targetY, pixels[idx]);
                    }
                }
            }
        }

        return image;
    }

    private static int readPixel(byte[] buf, int offset, int bpp) {
        if (bpp == 8) { // Grayscale
            int v = buf[offset] & 0xFF;
            return (0xFF << 24) | (v << 16) | (v << 8) | v;
        }
        if (bpp == 24) {
            // BGR
            int b = buf[offset] & 0xFF;
            int g = buf[offset + 1] & 0xFF;
            int r = buf[offset + 2] & 0xFF;
            return (0xFF << 24) | (r << 16) | (g << 8) | b;
        }
        if (bpp == 32) {
            // BGRA
            int b = buf[offset] & 0xFF;
            int g = buf[offset + 1] & 0xFF;
            int r = buf[offset + 2] & 0xFF;
            int a = buf[offset + 3] & 0xFF;
            return (a << 24) | (r << 16) | (g << 8) | b;
        }
        return 0xFF000000; // Black
    }
}
