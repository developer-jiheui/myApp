package com.gdu.myapp.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.gdu.myapp.dto.UserDto;
import com.gdu.myapp.mapper.UserMapper;
import com.gdu.myapp.utils.MyJavaMailUtils;
import com.gdu.myapp.utils.MySecurityUtils;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final MyJavaMailUtils myJavaMailUtils;

    public UserServiceImpl(UserMapper userMapper, MyJavaMailUtils myJavaMailUtils) {
        super();
        this.userMapper = userMapper;
        this.myJavaMailUtils = myJavaMailUtils;
    }


    @Override
    public void signin(HttpServletRequest request, HttpServletResponse response) {

        try {

            // 입력한 아이디
            String email = request.getParameter("email");

            // 입력한 비밀번호 + SHA-256 방식의 암호화
            String pw = MySecurityUtils.getSha256(request.getParameter("pw"));

            // 접속 IP (접속 기록을 남길 때 필요한 정보)
            String ip = request.getRemoteAddr();

            // DB로 보낼 정보 (email/pw: USER_T , email/ip: ACCESS_HISTORY_T)
            Map<String, Object> params = Map.of("email", email
                    , "pw", pw
                    , "ip", ip);

            // email/pw 가 일치하는 회원 정보 가져오기
            UserDto user = userMapper.getUserByMap(params);

            // 일치하는 회원 있음 (Sign In 성공)
            if (user != null) {
                // 접속 기록 ACCESS_HISTORY_T 에 남기기
                userMapper.insertAccessHistory(params);
                // 회원 정보를 세션(브라우저 닫기 전까지 정보가 유지되는 공간, 기본 30분 정보 유지)에 보관하기
                request.getSession().setAttribute("user", user);
                // Sign In 후 페이지 이동
                response.sendRedirect(request.getParameter("url"));

                // 일치하는 회원 없음 (Sign In 실패)
            } else {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.println("<script>");
                out.println("alert('일치하는 회원 정보가 없습니다.')");
                out.println("location.href='" + request.getContextPath() + "/main.page'");
                out.println("</script>");
                out.flush();
                out.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public ResponseEntity<Map<String, Object>> checkEmail(Map<String, Object> params) {
        boolean enableEmail = userMapper.getUserByMap(params) == null
                && userMapper.getLeaveUserByMap(params) == null;
        return new ResponseEntity<>(Map.of("enableEmail", enableEmail)
                , HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Map<String, Object>> sendCode(Map<String, Object> params) {

        /*
         * 구글 앱 비밀번호 설정 방법
         * 1. 구글에 로그인한다.
         * 2. [계정] - [보안]
         * 3. [Google에 로그인하는 방법] - [2단계 인증]을 사용 설정한다.
         * 4. 검색란에 "앱 비밀번호"를 검색한다.
         * 5. 앱 이름을 "myapp"으로 작성하고 [만들기] 버튼을 클릭한다.
         * 6. 16자리 비밀번호가 나타나면 복사해서 사용한다. (비밀번호 사이 공백은 모두 제거한다.)
         */

        // 인증코드 생성
        String code = MySecurityUtils.getRandomString(6, true, true);

        // 개발할 때 인증코드 찍어보기
        System.out.println("인증코드 : " + code);

        // 메일 보내기
        myJavaMailUtils.sendMail((String) params.get("email")
                , "myapp 인증요청"
                , "<div>인증코드는 <strong>" + code + "</strong>입니다.");

        // 인증코드 입력화면으로 보내주는 값
        return new ResponseEntity<>(Map.of("code", code)
                , HttpStatus.OK);

    }


    @Override
    public void signup(HttpServletRequest request, HttpServletResponse response) {

        //전달된 파라미터
        String email = request.getParameter("email");
        String pw = MySecurityUtils.getSha256(request.getParameter("pw"));
        String name = MySecurityUtils.getPreventXss(request.getParameter("name"));
        //@TODO mobile '-'
        String mobile = request.getParameter("mobile");
        String gender = request.getParameter("gender");
        String event = request.getParameter("event");

        //Mapper로 보낼 USERDTO 만들기
        UserDto user = UserDto.builder()
                .email(email)
                .pw(pw)
                .name(name)
                .mobile(mobile)
                .gender(gender)
                .eventAgree(event == null ? 0 : 1)
                .userNo(0)
                .signupKind(0)
                .build();

        // INSERT
        int insertCount = userMapper.insertUser(user);

        //응답만들기 성공하면 sign in /main,.do 실패하면 뒤로가기
        try {

            if (insertCount > 0) {

                String ip = request.getRemoteAddr();

                // DB로 보낼 정보 (email/pw: USER_T , email/ip: ACCESS_HISTORY_T)
                Map<String, Object> params = Map.of("email", email
                        , "pw", pw
                        , "ip", ip);

                // email/pw 가 일치하는 회원 정보 가져오기
                UserDto singedInUser = userMapper.getUserByMap(params);

                // 일치하는 회원 있음 (Sign In 성공)
                if (user != null) {
                    // 접속 기록 ACCESS_HISTORY_T 에 남기기
                    userMapper.insertAccessHistory(params);
                    // 회원 정보를 세션(브라우저 닫기 전까지 정보가 유지되는 공간, 기본 30분 정보 유지)에 보관하기
                    request.getSession().setAttribute("user", singedInUser);
                    // Sign In 후 페이지 이동
                    response.sendRedirect(request.getContextPath() + "/main.page");
//                    response.setContentType("text/html; charset=UTF-8");
//                    PrintWriter out = response.getWriter();
//                    out.println("<script>");
//                    out.println("alert('아이디가 생성되었습니다')");
//                    out.println("location.href='" + request.getContextPath() + "/main.page'");
//                    out.println("</script>");
//                    out.flush();
//                    out.close();

                    // 일치하는 회원 없음 (Sign In 실패)
                } else {
                    response.setContentType("text/html; charset=UTF-8");
                    PrintWriter out = response.getWriter();
                    out.println("<script>");
                    out.println("alert('일치하는 회원 정보가 없습니다.')");
                    out.println("location.href='" + request.getContextPath() + "/main.page'");
                    out.println("</script>");
                    out.flush();
                    out.close();
                }
            } else {
                response.sendRedirect(request.getContextPath());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void leave(HttpServletRequest request, HttpServletResponse response) {

        try {
            HttpSession session = request.getSession();
            UserDto user = (UserDto) session.getAttribute("user");

            //세션만료로 유저가 세션에 없는경우
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/main.page");
            } else {

                //탈퇴 처리
                int deleteUserCount = userMapper.deleteUser(user.getUserNo());
                //int userNo = (int) session.getAttribute("userNo");

                //탈퇴이후 응답
                PrintWriter out = response.getWriter();
                response.setContentType("text/html; charset=UTF-8");
                out.println("<script>");

                if (deleteUserCount == 1) {
                    //세션에 저장된 모든 정보 초기화
                    session.invalidate();
                    out.println("alert('회원 탈퇴 되었습니다')");
                    out.println("location.href='" + request.getContextPath() + "/main.page'");
                } else {
                    out.println("alert('회원 탈퇴 되지 않았습니다')");
                    out.println("history.back();");
                }
                out.println("</script>");
                out.flush();
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void signout(HttpServletRequest request, HttpServletResponse response) {
        // TODO Auto-generated method stub

    }

}
