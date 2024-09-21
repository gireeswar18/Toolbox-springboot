package com.giree.toolbox.controller;

import com.giree.toolbox.service.ToolboxService;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
public class Controller {

    @Autowired
    private ToolboxService service;

    @GetMapping("/qr-code-generator")
    public ResponseEntity<?> qrcodeGenerator(@RequestParam String link) throws WriterException, IOException {

        byte[] imageByte = service.qrcodeGenerator(link);

        if (imageByte == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        HttpHeaders headers = new HttpHeaders();

        headers.add("Response", "Successfully converted!");
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(imageByte.length);

        return new ResponseEntity<>(imageByte, headers, HttpStatus.OK);
    }


    @PostMapping("/image-converter")
    public ResponseEntity<?> imageConverter(@RequestParam("file") MultipartFile file,
                                            @RequestParam("format") String format) {

        if (file.isEmpty()) {
            return new ResponseEntity<>("No file uploaded", HttpStatus.BAD_REQUEST);
        }

        format = format.toLowerCase();
        if (!format.equals("jpeg") && !format.equals("png") && !format.equals("pdf")) {
            return new ResponseEntity<>("Unsupported format", HttpStatus.BAD_REQUEST);
        }

        byte[] res = service.imgConverter(file, format);

        if (res == null) {
            return new ResponseEntity<>("Check file format!", HttpStatus.BAD_REQUEST);
        }

        ByteArrayResource converted = new ByteArrayResource(res);
        HttpHeaders headers = new HttpHeaders();

        headers.add("Response", "Successfully converted!");

        if (format.equals("jpeg"))
            headers.setContentType(MediaType.IMAGE_JPEG);

        else if (format.equals("png"))
            headers.setContentType(MediaType.IMAGE_PNG);

        else
            headers.setContentType(MediaType.APPLICATION_PDF);

        headers.setContentLength(res.length);
        headers.setContentDispositionFormData("attachment", "converted." + format);

        return new ResponseEntity<>(converted, headers, HttpStatus.OK);
    }

    @PostMapping("/pdf-to-word")
    public ResponseEntity<?> pdfToWord(@RequestParam("file") MultipartFile file) {

        String name = file.getOriginalFilename();

        if (name.charAt(name.length() - 1) != 'f')
            return new ResponseEntity<>("Unsupported format!", HttpStatus.BAD_REQUEST);

        String fileName = name.substring(0, name.length() - 5) + ".docx";

        byte[] res = service.pdfToWord(file);

        if (res == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ByteArrayResource pdf = new ByteArrayResource(res);

        HttpHeaders headers = new HttpHeaders();

        headers.add("Response", "Successfully converted!");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(res.length);
        headers.setContentDispositionFormData("attachment", fileName);

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @PostMapping("/word-to-pdf")
    public ResponseEntity<?> wordToPdf(@RequestParam("file") MultipartFile inputFile) throws IOException {

        String name = inputFile.getOriginalFilename();

        if (name.charAt(name.length() - 1) != 'x')
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        String fileName = name.substring(0, name.length() - 5) + ".pdf";

        byte[] res = service.wordToPdf(inputFile);

        if (res == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ByteArrayResource pdf = new ByteArrayResource(res);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Response", "Successfully converted!");
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentLength(res.length);
        headers.setContentDispositionFormData("attachment", fileName);

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);

    }

    @PostMapping("/img-compressor")
    public ResponseEntity<?> imageCompressor(@RequestParam("file") MultipartFile file, int quality) {

        String filename = file.getOriginalFilename();

        if (file.isEmpty()) {
            return new ResponseEntity<>("File is Empty!", HttpStatus.BAD_REQUEST);
        }

        if (!filename.contains(".jpeg") && !filename.contains(".png")) {
            return new ResponseEntity<>("Unsupported File format", HttpStatus.BAD_REQUEST);
        }
        String format = "";

        byte[] res = service.imgCompressor(file);

        if (res == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ByteArrayResource resource = new ByteArrayResource(res);

        HttpHeaders headers = new HttpHeaders();

        headers.add("Response", "Successfully converted!");
        headers.setContentLength(res.length);

        if (filename.contains(".jpeg")) {
            headers.setContentType(MediaType.IMAGE_JPEG);
            format = ".jpeg";
        }
        else if (filename.contains(".png")) {
            headers.setContentType(MediaType.IMAGE_PNG);
            format = ".png";
        }
        headers.setContentDispositionFormData("attachment", "compressed" + format);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
