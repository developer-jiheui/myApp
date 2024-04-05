package com.gdu.myapp.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class BlogServiceImpl implements BlogService{
    @Override
    public ResponseEntity<Map<String, Object>> summernoteImageUpload(MultipartFile mutilpartFile) {
        return null;
    }

    @Override
    public int registerBlog(HttpServletRequest request) {
        return 0;
    }
}
