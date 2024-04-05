package com.gdu.myapp.controller;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gdu.myapp.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gdu.myapp.service.UserService;

@RequestMapping("/user")
@Controller
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    super();
    this.userService = userService;
  }
  
  @GetMapping("/signin.page")
  public String signinPage(HttpServletRequest request
                         , Model model) {
    
    // Sign In 페이지 이전의 주소가 저장되어 있는 Request Header 의 referer
    String referer = request.getHeader("referer");
    
    // referer 로 돌아가면 안 되는 예외 상황 (아이디/비밀번호 찾기 화면, 가입 화면 등)
    String[] excludeUrls = {"/findId.page", "/findPw.page", "/signup.page"};
    
    // Sign In 이후 이동할 url
    String url = referer;
    if(referer != null) {
      for(String excludeUrl : excludeUrls) {
        if(referer.contains(excludeUrl)) {
          url = request.getContextPath() + "/main.page";
          break;
        }
      }
    } else {
      url = request.getContextPath() + "/main.page";
    }
    
    // Sign In 페이지로 url 넘겨 주기
    model.addAttribute("url",  url);

    /********* 네이버 로그인 1 **********/
    String redirectUri = "http://localhost:8888" + request.getContextPath() + "/user/naver/getAccessToken.do";
    //네이버 개발자 센터에 이렇게 적혀있음
    String state = new BigInteger(130,new SecureRandom()).toString();//위조 방지


    StringBuilder builder = new StringBuilder();
    builder.append("https://nid.naver.com/oauth2.0/authorize");
    builder.append("?response_type=code");
    builder.append("&client_id=h7HITARz4dvCYr6okwC7");
    builder.append("&redirect_uri=" +redirectUri);
    builder.append("&state=" + state);

    model.addAttribute("naverLoginUrl",builder.toString());



    return "user/signin";
    
  }
  
  @PostMapping("/signin.do")
  public void signin(HttpServletRequest request, HttpServletResponse response) {
    userService.signin(request, response);
  }
  
  @GetMapping("/signup.page")
  public String signupPage() {
    return "user/signup";
  }
  
  @PostMapping(value="/checkEmail.do", produces="application/json")
  public ResponseEntity<Map<String, Object>> checkEmail(@RequestBody Map<String, Object> params) {
    return userService.checkEmail(params);
  }
  
  @PostMapping(value="/sendCode.do", produces="application/json")
  public ResponseEntity<Map<String, Object>> sendCode(@RequestBody Map<String, Object> params) {
    return userService.sendCode(params);
  }
  
  @PostMapping("/signup.do")
  public void signUp(HttpServletRequest request, HttpServletResponse response){
    userService.signup(request, response);
  }

  @GetMapping("/naver/getAccessToken.do")
  public String getAccessToken(HttpServletRequest request){
    String accessToken = userService.getNaverLoginAccessToken(request);
    return "redirect:/user/naver/getProfile.do?accessToken=" + accessToken;
  }

  @GetMapping("/naver/getProfile.do")
  public String getProfile(HttpServletRequest request, HttpServletResponse response, Model model){
    UserDto naverUser = userService.getNaverLoginProfile(request.getParameter("accessToken"));
    String path = null;


    // 프로필이 DB에 있는지 확인 (있으면 Sign in, 없으면 sign out)
    if(userService.hasUser(naverUser)){
      //Sign in
      userService.naverSignin(request,response,naverUser);

      path= "redirect:/main.page";

    }else{
      path="r";

    }

    return path;
  }
    @GetMapping("/signout.do")
  public void signout(HttpServletRequest request, HttpServletResponse response) {
    userService.signout(request, response);
  }

   @GetMapping("/leave.do")
  public void leave(HttpServletRequest request, HttpServletResponse response) {
    userService.leave(request, response);
  }
  /*
  @GetMapping("/leave.do")
  public void leave(HttpSession session, HttpServletResponse response) {
    UserDto user = (UserDto) session.getAttribute("user");
  }
  @GetMapping("/leave.do")
  public void leave(@SessionAttribute(name="user") UserDto user, HttpServletResponse response) {
  }
  */
  
}
