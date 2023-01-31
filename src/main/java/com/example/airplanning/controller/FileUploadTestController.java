package com.example.airplanning.controller;

import com.example.airplanning.domain.Response;
import com.example.airplanning.service.S3FileUploadTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class FileUploadTestController {

    private final S3FileUploadTestService s3FileUploadTestService;

    @PostMapping
    public Response<String> uploadFile(@RequestPart("file") MultipartFile file) throws IOException {
        String url = s3FileUploadTestService.uploadFile(file);
        return Response.success(url);
    }
}
