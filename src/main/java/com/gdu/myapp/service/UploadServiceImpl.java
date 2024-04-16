package com.gdu.myapp.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.gdu.myapp.dto.AttachDto;
import com.gdu.myapp.dto.UploadDto;
import com.gdu.myapp.dto.UserDto;
import com.gdu.myapp.mapper.UploadMapper;
import com.gdu.myapp.utils.MyFileUtils;
import com.gdu.myapp.utils.MyPageUtils;

import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;

@RequiredArgsConstructor
@Service
public class UploadServiceImpl implements UploadService {

  private final UploadMapper uploadMapper;
  private final MyPageUtils myPageUtils;
  private final MyFileUtils myFileUtils;

  @Override
  public boolean registerUpload(MultipartHttpServletRequest multipartRequest) {

    // UPLOAD_T 테이블에 추가하기
    String title = multipartRequest.getParameter("title");
    String contents = multipartRequest.getParameter("contents");
    int userNo = Integer.parseInt(multipartRequest.getParameter("userNo"));

    UserDto user = new UserDto();
    user.setUserNo(userNo);

    UploadDto upload = UploadDto.builder()
            .title(title)
            .contents(contents)
            .user(user)
            .uploadNo(0).attachCount(0).build();

    System.out.println("INSERT 이전 : " + upload.getUploadNo());  // uploadNo 없음
    int insertUploadCount = uploadMapper.insertUpload(upload);
    System.out.println("INSERT 이후 : " + upload.getUploadNo());  // uploadNo 있음 (<selectKey> 동작에 의해서)

    // 첨부 파일 처리하기
    List<MultipartFile> files = multipartRequest.getFiles("files");

    // 첨부 파일이 없는 경우 : [MultipartFile[field="files", filename=, contentType=application/octet-stream, size=0]]
    // 첨부 파일이 있는 경우 : [MultipartFile[field="files", filename=404.jpg, contentType=image/jpeg, size=63891]]
    // System.out.println(files);

    int insertAttachCount;
    if(files.get(0).getSize() == 0) {
      insertAttachCount = 1;  // 첨부가 없어도 files.size() 는 1 이다.
    } else {
      insertAttachCount = 0;
    }

    for (MultipartFile multipartFile : files) {

      if(multipartFile != null && !multipartFile.isEmpty()) {

        String uploadPath = myFileUtils.getUploadPath();
        System.out.println(uploadPath);

        File dir = new File(uploadPath);
//        System.out.println(dir.toString());
//        System.out.println(dir.getPath());
        if(!dir.exists()) {
          dir.mkdirs();
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String filesystemName = myFileUtils.getFilesystemName(originalFilename);
        File file = new File(dir, filesystemName);

        try {

          multipartFile.transferTo(file);

          // 썸네일 작성
          String contentType = Files.probeContentType(file.toPath());
          int hasThumbnail = contentType != null && contentType.startsWith("image") ? 1 : 0;

          // 이미지의 썸네일 만들기
          if(hasThumbnail == 1) {
            File thumbnail = new File(dir, "s_" + filesystemName);
            Thumbnails.of(file)             // 원본 이미지 파일
                      .size(96, 64)         // 가로 96px, 세로 64px
                      .toFile(thumbnail);   // 썸네일 이미지 파일
          }

          // ATTACH_T 테이블에 추가하기
          AttachDto attach = AttachDto.builder()
                  .uploadPath(uploadPath)
                  .filesystemName(filesystemName)
                  .originalFilename(originalFilename)
                  .hasThumbnail(hasThumbnail)
                  .uploadNo(upload.getUploadNo())
                  .attachNo(0).downloadCount(0).build();

          insertAttachCount += uploadMapper.insertAttach(attach);

        } catch (Exception e) {
          e.printStackTrace();
        }

      }  // if

    }  // for

    return (insertUploadCount == 1) && (insertAttachCount == files.size());

  }

  @Override
  public void loadUploadList(Model model) {

    /*
     * interface UploadService {
     *   void execute(Model model);
     * }
     *
     * class UploadRegisterService implements UploadService {
     *   @Override
     *   void execute(Model model) {
     *
     *   }
     * }
     *
     * class UploadListService implements UploadService {
     *   @Override
     *   void execute(Model model) {
     *
     *   }
     * }
     */

    Map<String, Object> modelMap = model.asMap();
    HttpServletRequest request = (HttpServletRequest) modelMap.get("request");

    int total = uploadMapper.getUploadCount();

    Optional<String> optDisplay = Optional.ofNullable(request.getParameter("display"));
    int display = Integer.parseInt(optDisplay.orElse("20"));

    Optional<String> optPage = Optional.ofNullable(request.getParameter("page"));
    int page = Integer.parseInt(optPage.orElse("1"));

    myPageUtils.setPaging(total, display, page);

    Optional<String> optSort = Optional.ofNullable(request.getParameter("sort"));
    String sort = optSort.orElse("DESC");

    Map<String, Object> map = Map.of("begin", myPageUtils.getBegin()
                                   , "end", myPageUtils.getEnd()
                                   , "sort", sort);

    /*
     * total = 100, display = 20
     *
     * page  beginNo
     * 1     100
     * 2     80
     * 3     60
     * 4     40
     * 5     20
     */
    model.addAttribute("beginNo", total - (page - 1) * display);
    model.addAttribute("uploadList", uploadMapper.getUploadList(map));
    model.addAttribute("paging", myPageUtils.getPaging(request.getContextPath() + "/upload/list.do", sort, display));
    model.addAttribute("display", display);
    model.addAttribute("sort", sort);
    model.addAttribute("page", page);
  }

  @Override
  public void loadUploadByNo(int uploadNo, Model model) {
    model.addAttribute("upload", uploadMapper.getUploadByNo(uploadNo));
    model.addAttribute("attachList", uploadMapper.getAttachList(uploadNo));
  }

  @Override
  public ResponseEntity<Resource> download(HttpServletRequest request) {
    // 첨부 파일 정보를 DB 에서 가져오기
    int attachNo = Integer.parseInt(request.getParameter("attachNo"));
    AttachDto attach = uploadMapper.getAttachByNo(attachNo);

    // 첨부 파일 정보를 File 객체로 만든 뒤 Resource 객체로 변환
    File file = new File(attach.getUploadPath(), attach.getFilesystemName());
    Resource resource = new FileSystemResource(file);

    // 첨부 파일이 없으면 다운로드 취소
    if(!resource.exists()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // DOWNLOAD_COUNT 증가
    uploadMapper.updateDownloadCount(attachNo);

    // 사용자가 다운로드 받을 파일명 결정 (originalFilename 을 브라우저에 따라 다르게 인코딩 처리)
    String originalFilename = attach.getOriginalFilename();
    String userAgent = request.getHeader("User-Agent");
    try {
      // IE
      if(userAgent.contains("Trident")) {
       originalFilename = URLEncoder.encode(originalFilename, "UTF-8").replace("+", " ");
      }
      // Edge
      else if(userAgent.contains("Edg")) {
        originalFilename = URLEncoder.encode(originalFilename, "UTF-8");
      }
      // Other
      else {
        originalFilename = new String(originalFilename.getBytes("UTF-8"), "ISO-8859-1");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // 다운로드용 응답 헤더 설정 (HTTP 참조)
    HttpHeaders responseHeader = new HttpHeaders();
    responseHeader.add("Content-Type", "application/octet-stream");
    responseHeader.add("Content-Disposition", "attachment; filename=" + originalFilename);
    responseHeader.add("Content-Length", file.length() + "");

    // 다운로드 진행
    return new ResponseEntity<Resource>(resource, responseHeader, HttpStatus.OK);

  }

 @Override
  public ResponseEntity<Resource> downloadAll(HttpServletRequest request) {

    // 다운로드 할 모든 첨부 파일들의 정보를 DB 에서 가져오기
    int uploadNo = Integer.parseInt(request.getParameter("uploadNo"));
    List<AttachDto> attachList = uploadMapper.getAttachList(uploadNo);

    // 첨부 파일이 없으면 종료
    if(attachList.isEmpty()) {
      return new ResponseEntity<Resource>(HttpStatus.NOT_FOUND);
    }

    // 임시 zip 파일 저장할 경로
    File tempDir = new File(myFileUtils.getTempPath());
    if(!tempDir.exists()) {
      tempDir.mkdirs();
      System.out.println("created dir download all");
    }

    // 임시 zip 파일 이름
    String tempFilename = myFileUtils.getTempFilename() + ".zip";

    // 임시 zip 파일 File 객체
    File tempFile = new File(tempDir, tempFilename);

    // 첨부 파일들을 하나씩 zip 파일로 모으기
    try {

      // ZipOutputStream 객체 생성
      ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(tempFile));

      for (AttachDto attach : attachList) {

        // zip 파일에 포함할 ZipEntry 객체 생성
        ZipEntry zipEntry = new ZipEntry(attach.getOriginalFilename());

        // zip 파일에 ZipEntry 객체 명단 추가 (파일의 이름만 등록한 상황)
        zout.putNextEntry(zipEntry);

        // 실제 첨부 파일을 zip 파일에 등록 (첨부 파일을 읽어서 zip 파일로 보냄)
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(attach.getUploadPath(), attach.getFilesystemName())));
        zout.write(in.readAllBytes());

        // 사용한 자원 정리
        in.close();
        zout.closeEntry();

        // DOWNLOAD_COUNT 증가
        uploadMapper.updateDownloadCount(attach.getAttachNo());

      }  // for

      // zout 자원 반납
      zout.close();

    } catch (Exception e) {
      e.printStackTrace();
    }

    // 다운로드 할 zip File 객체 -> Resource 객체
    Resource resource = new FileSystemResource(tempFile);

    // 다운로드용 응답 헤더 설정 (HTTP 참조)
    HttpHeaders responseHeader = new HttpHeaders();
    responseHeader.add("Content-Type", "application/octet-stream");
    responseHeader.add("Content-Disposition", "attachment; filename=" + tempFilename);
    responseHeader.add("Content-Length", tempFile.length() + "");

    // 다운로드 진행
    return new ResponseEntity<Resource>(resource, responseHeader, HttpStatus.OK);

  }

  @Override
  public void removeTempFiles() {
    // TEMP File 모두 제거하기
    File tempDir = new File(myFileUtils.getTempPath());
    System.out.println(myFileUtils.getTempPath());
    System.out.println(tempDir);
    File[] tempFiles = tempDir.listFiles();

    if(tempFiles != null) {
      for (int i = 0; i < tempFiles.length; i++) {
        //tempFiles[i].delete();
        System.out.println(tempFiles[i]);
        tempFiles[i].delete();
      }
    }


  }


}
