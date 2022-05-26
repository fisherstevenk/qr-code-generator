package com.swiftcryptollc.apps.qrcodegnerator;

/**
 *
 * @author Steven K Fisher <fisherstevenk@gmail.com>
 */
public class QRCodeApp {

    public static void main(String[] args) {
        QRCodeGenerator gen = new QRCodeGenerator();
        try {
            gen.generateQRCodeImage("https://freeloaderz.io", "/tmp/freeloaderz-plain-qrc.png");
            System.out.println("Finished plain QRC");
        } catch (Exception e) {
            System.out.println("Exception with plain QRC! [" + e.getMessage() + "]");
        }
        try {
            gen.generateQRCodeImage("/tmp/Freeloaderz-white.jpg", "https://freeloaderz.io", "/tmp/freeloaderz-logo-qrc.png", true);
        } catch (Exception e) {
            System.out.println("Exception with logo QRC! [" + e.getMessage() + "]");
        }
        try {
            gen.generateQRCodeImage("/tmp/Freeloaderz.jpg", "https://freeloaderz.io", "/tmp/freeloaderz-logo-black-qrc.png", false);
        } catch (Exception e) {
            System.out.println("Exception with black logo QRC! [" + e.getMessage() + "]");
        }

    }
}
