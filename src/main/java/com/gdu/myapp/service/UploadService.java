package com.gdu.myapp.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

public interface UploadService {
    public boolean registerUpload(MultipartHttpServletRequest multipartRequest);

    public void loadUploadList(Model model);

    void loadUploadByNo(int uploadNo, Model model);

    ResponseEntity<Resource> download(HttpServletRequest request);

    ResponseEntity<Resource> downloadAll(HttpServletRequest request);

    void removeTempFiles();

}
