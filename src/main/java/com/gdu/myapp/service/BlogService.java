package com.gdu.myapp.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface BlogService {

    ResponseEntity<Map<String,Object>> summernoteImageUpload(MultipartFile mutilpartFile);
    int registerBlog(HttpServletRequest request);


}
