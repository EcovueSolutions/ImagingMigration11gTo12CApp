package com.ecovues;


//import javax.servlet.annotation.WebServlet;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

public class CheckForValidSession extends HttpServlet {
    private static final Logger logger = Logger.getLogger(CheckForValidSession.class);
 
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException,
                                                               IOException {
        HttpSession theSession = request.getSession(false);
        Cookie[] requestCookies = request.getCookies();
        /*String loginType="FORM";
 
            for(Cookie c : requestCookies){
                if(c.getName().equals("OAMAuthnHintCookie"))
                    loginType="CLIENT";
 
            }*/
        Cookie[] cookies = request.getCookies();
        String sessionValue="";
        if (cookies != null) {
         for (Cookie cookie : cookies) {
           if (cookie.getName().equals("JSESSIONID")) {
             //do something
             //value can be retrieved using #cookie.getValue()
               logger.info("JSESSIONID: "+cookie.getValue());
                sessionValue = cookie.getValue();
            }
          }
        }
//        }
//        catch(NullPointerException e) 
//                { 
//                    response.sendRedirect(request.getContextPath() + "/signin.html");
//                    return;
//                } 
//        response.sendRedirect(request.getContextPath() + "/signin.html");
        if(sessionValue ==null){
            try{
            theSession.invalidate();
            }
            catch(NullPointerException e) 
                    { 
                        response.sendRedirect(request.getContextPath() + "/signin.html");
                        return;
                    } 
            response.sendRedirect(request.getContextPath() + "/signin.html");
        }
        return;
 
        /* if(loginType.equals("FORM"))
            {
                theSession.invalidate();
                response.sendRedirect(request.getContextPath() + "/signin.html");
                return;
            }
 
            else
            {
 
                theSession.invalidate();
            response.sendRedirect("http://oam-eco-com:14100/oam/server/logout");
          // response.sendRedirect(request.getContextPath() + "/signin.html");
                return;
            }*/
    }
 
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException,
                                                              IOException {
        HttpSession theSession = request.getSession(false);
        Cookie[] requestCookies = request.getCookies();
        /* String loginType="FORM";
 
            for(Cookie c : requestCookies){
                if(c.getName().equals("OAMAuthnHintCookie"))
                    loginType="CLIENT";
 
            }*/
        Cookie[] cookies = request.getCookies();
        String sessionValue="";
        if (cookies != null) {
         for (Cookie cookie : cookies) {
           if (cookie.getName().equals("JSESSIONID")) {
             //do something
             //value can be retrieved using #cookie.getValue()
               logger.info("JSESSIONID: "+cookie.getValue());
                sessionValue = cookie.getValue();
            }
          }
        }
        //        }
        //        catch(NullPointerException e)
        //                {
        //                    response.sendRedirect(request.getContextPath() + "/signin.html");
        //                    return;
        //                }
        //        response.sendRedirect(request.getContextPath() + "/signin.html");
        if(sessionValue ==null){
            try{
            theSession.invalidate();
            }
            catch(NullPointerException e) 
                    { 
                        response.sendRedirect(request.getContextPath() + "/signin.html");
                        return;
                    } 
            response.sendRedirect(request.getContextPath() + "/signin.html");
        }
      //  return;
//        Cookie c1 = new Cookie("JSESSIONID", null);
//        Cookie c2 = new Cookie("_WL_AUTHCOOKIE_JSESSIONID", null);
//        c1.setPath("/");
//        c2.setPath("/");
//        response.addCookie(c1);
//        response.addCookie(c2);
//        //   request.getSession().invalidate();
//        try{
//        theSession.invalidate();
//        }
//        catch(NullPointerException e) 
//                { 
//                    System.out.print("Caught NullPointerException"); 
//                    response.sendRedirect(request.getContextPath() + "/signin.html");
//                    return;
//                } 
//        response.sendRedirect(request.getContextPath() + "/signin.html");
        return;
        /*if(loginType.equals("FORM")) {
                theSession.invalidate();
            response.sendRedirect(request.getContextPath() + "/signin.html");
                return;
            }
                else
                {
                 response.sendRedirect("http://oam-eco-com:14100/oam/server/logout");
               // response.sendRedirect("http://eco.imaging.com:7777/dashboard");
                    return;
                }*/
    }
 
}