package com.example.gigconnect.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Autowired
    private Cloudinary cloudinary;

    @PostMapping("/upload")
    public ResponseEntity<Map> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return new ResponseEntity<>(uploadResult, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of("message", "File upload failed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}