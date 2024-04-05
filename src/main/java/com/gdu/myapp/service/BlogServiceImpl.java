package com.gdu.myapp.service;

import com.gdu.myapp.mapper.BlogMapper;
import com.gdu.myapp.utils.MyFileUtils;
import com.gdu.myapp.utils.MyPageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService{
    private final BlogMapper blogMapper;
    private final MyPageUtils myPageUtils;
    private final MyFileUtils myFileUtils;

    @Override
    public ResponseEntity<Map<String, Object>> summernoteImageUpload(MultipartFile mutilpartFile) {


        return null;
    }

    @Override
    public int registerBlog(HttpServletRequest request) {
        return 0;
    }
}
