package com.gdu.myapp.listener;

import com.gdu.myapp.mapper.UserMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class MyHttpSessionListener implements HttpSessionListener {
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        HttpSessionListener.super.sessionCreated(se);
    }

    //세션 만료 시 자동으로 동작
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {

        //HttpSession
        HttpSession session = se.getSession();

        //ApplicationContext
        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(session.getServletContext());

        //session ID
        String sessionId = session.getId();

        //getBean()
        //Service를 추천하나, Mapper 도 가능
        UserMapper userMapper = ctx.getBean("userMapper", UserMapper.class);

        //updateAccessHistory()
        userMapper.updateAccessHistory(sessionId);

        //확인메세지
        System.out.println("Session Update from : " + sessionId);
    }
}
