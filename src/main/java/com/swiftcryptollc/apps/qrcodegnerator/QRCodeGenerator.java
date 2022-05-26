package com.swiftcryptollc.apps.qrcodegnerator;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Hashtable;
import javax.imageio.ImageIO;

/**
 *
 * @author Steven K Fisher <fisherstevenk@gmail.com>
 */
public class QRCodeGenerator {

    private final int qrCodeSize = 450;

    public QRCodeGenerator() {
    }

    public boolean generateQRCodeImage(String url, String fullFileName) throws Exception {
        return generateQRCodeImage(null, url, fullFileName, false);
    }

    public boolean generateQRCodeImage(String logoFile, String url, String fullFileName, boolean printUrl) throws Exception {
        if (!fullFileName.toLowerCase().endsWith(".png")) {
            throw new IllegalArgumentException("Provide a file name with a '.png' ending");
        }
        boolean success = false;
        // Correction level - HIGH - more chances to recover message
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap
                = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        // Generate QR-code
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(url,
                BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);
        Path path = FileSystems.getDefault().getPath(fullFileName);
        if (logoFile == null) {
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
            success = true;
        } else {
            // Start work with picture
            int matrixWidth = bitMatrix.getWidth();
            BufferedImage image = new BufferedImage(matrixWidth, matrixWidth,
                    BufferedImage.TYPE_INT_RGB);
            image.createGraphics();
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
            graphics.setFont(font);
            graphics.setColor(Color.white);
            graphics.fillRect(0, 0, matrixWidth, matrixWidth);
            Color mainColor = new Color(0, 0, 0);
            graphics.setColor(mainColor);
            if (printUrl) {
                // Write message under the QR-code
                FontMetrics metrics = graphics.getFontMetrics(font);
                // Determine the X coordinate for the text
                int x = (image.getWidth() - metrics.stringWidth(url)) / 2;
                int y = image.getHeight() - graphics.getFont().getSize();
                graphics.drawString(url, x, y);
            }
            //Write Bit Matrix as image
            for (int i = 0; i < matrixWidth; i++) {
                for (int j = 0; j < matrixWidth; j++) {
                    if (bitMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }

            // Add logo to QR code
            BufferedImage logo = ImageIO.read(new File(logoFile));

            //scale logo image and insert it to center of QR-code
            double scale = calcScaleRate(image, logo);
            if (scale < 1) {
                logo = getScaledImage(logo,
                        (int) (logo.getWidth() * scale),
                        (int) (logo.getHeight() * scale));
            }
            graphics.drawImage(logo,
                    image.getWidth() / 2 - logo.getWidth() / 2,
                    image.getHeight() / 2 - logo.getHeight() / 2,
                    image.getWidth() / 2 + logo.getWidth() / 2,
                    image.getHeight() / 2 + logo.getHeight() / 2,
                    0, 0, logo.getWidth(), logo.getHeight(), null);

            // Check correctness of QR-code
            try {
                if (isQRCodeCorrect(url, image)) {
                    ImageIO.write(image, "PNG", new File(fullFileName));
                    System.out.println("Your QR-code was succesfully generated.");
                    success = true;
                } else {
                    System.out.println("Sorry, your logo broke the QR-code. Try a smaller logo");
                    ImageIO.write(image, "PNG", new File(fullFileName.replaceAll(".png", "-broken.png")));
                }
            } catch (Exception ex) {
                throw ex;
            }
        }
        return success;
    }

    /**
     * Calc scale rate of logo. It is 30% of QR-code size
     *
     * @param image
     * @param logo
     * @return
     */
    private double calcScaleRate(BufferedImage image, BufferedImage logo) {
        double scaleRate = .5 / (logo.getWidth() / image.getWidth());
        if (scaleRate > 1) {
            scaleRate = 1;
        }
        return scaleRate;
    }

    /**
     * Check is QR-code correct
     *
     * @param content
     * @param image
     * @return
     */
    private boolean isQRCodeCorrect(String content, BufferedImage image) throws NotFoundException {
        boolean result = false;
        Result qrResult = decode(image);

        if (qrResult != null && content != null && content.equals(qrResult.getText())) {
            result = true;
        }
        return result;
    }

    /**
     * Decode QR-code.
     *
     * @param image
     * @return
     */
    private Result decode(BufferedImage image) throws NotFoundException {
        if (image == null) {
            System.out.println("Can't decode a NULL image!");
            return null;
        }
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap, Collections.EMPTY_MAP);
            return result;
        } catch (Exception ex) {
            System.out.println("Exception occured while decoding! [" + ex.getMessage() + "]");
        }
        return null;
    }

    /**
     * Scale image to required size
     *
     * @param image
     * @param width
     * @param height
     * @return
     * @throws IOException
     */
    private BufferedImage getScaledImage(BufferedImage image, int width, int height) throws IOException {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        double scaleX = (double) width / imageWidth;
        double scaleY = (double) height / imageHeight;
        AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
        AffineTransformOp bilinearScaleOp = new AffineTransformOp(
                scaleTransform, AffineTransformOp.TYPE_BILINEAR);

        return bilinearScaleOp.filter(
                image,
                new BufferedImage(width, height, image.getType()));
    }

}
