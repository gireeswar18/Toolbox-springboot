package com.giree.toolbox.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import com.googlecode.pngtastic.core.PngImage;
import com.googlecode.pngtastic.core.PngOptimizer;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.spire.doc.FileFormat;
import com.spire.pdf.PdfDocument;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

@Service
public class ToolboxService {

    public byte[] qrcodeGenerator(String link) {

        try {

            BitMatrix matrix = new MultiFormatWriter().encode(link, BarcodeFormat.QR_CODE, 500, 500);

            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            boolean res = ImageIO.write(image, "jpg", baos);

            if (!res) return null;

            System.out.println("Conversion completed successfully!");

            return baos.toByteArray();

        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }
    }


    public byte[] imgConverter(MultipartFile file, String format) {

        try {

            InputStream inputStream = file.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            if (format.equals("pdf")) {
                Document document = new Document();
                PdfWriter.getInstance(document, baos);
                document.open();

                Image image = Image.getInstance(file.getBytes());

                float margin = 20f;

                // Calculate the width and height to fit the image with margins
                float width = document.getPageSize().getWidth() - 2 * margin;
                float height = document.getPageSize().getHeight() - 2 * margin;

                // Resize the image to fit within the calculated width and height
                image.scaleToFit(width, height);

                // Set the alignment and margins
                image.setAlignment(Image.ALIGN_CENTER);
                image.setAbsolutePosition(
                        (document.getPageSize().getWidth() - image.getScaledWidth()) / 2,
                        (document.getPageSize().getHeight() - image.getScaledHeight()) / 2
                );

                document.add(image);
                document.close();
                return baos.toByteArray();
            }

            BufferedImage image = ImageIO.read(inputStream);

            if (image == null) {
                return null;
            }

            if ("jpeg".equalsIgnoreCase(format)) {
                BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = rgbImage.createGraphics();
                g2d.drawImage(image, 0, 0, null);
                g2d.dispose();
                image = rgbImage;
            }
            boolean s = ImageIO.write(image, format, baos);

            if (!s) return null;

            System.out.println("Conversion completed successfully!");
            return baos.toByteArray();

        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }

    }


    public byte[] wordToPdf(MultipartFile inputFile) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {

            InputStream ip = inputFile.getInputStream();
            com.spire.doc.Document document = new com.spire.doc.Document(ip);

            document.saveToStream(baos, FileFormat.PDF);

            document.close();

        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }

        return baos.toByteArray();
    }

    public byte[] pdfToWord(MultipartFile file) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            InputStream ip = file.getInputStream();
            PdfDocument pdfDocument = new PdfDocument(ip);

            pdfDocument.saveToStream(baos, com.spire.pdf.FileFormat.DOCX);

            pdfDocument.close();

        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }

        return baos.toByteArray();
    }

    public byte[] imgCompressor(MultipartFile file) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {

            if (file.getOriginalFilename().contains(".png")) {
                return pngCompressor(file);
            }

            InputStream ip = file.getInputStream();
            Thumbnails.of(ip)
                    .scale(1)
                    .outputQuality(.50)
                    .toOutputStream(baos);

        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }

        return baos.toByteArray();
    }

    private static byte[] pngCompressor(MultipartFile file) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {

            PngImage ip = new PngImage(file.getInputStream());
            PngOptimizer pngOptimizer = new PngOptimizer();

            PngImage op = pngOptimizer.optimize(ip);

            op.writeDataOutputStream(baos);

        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }

        return baos.toByteArray();
    }
}
