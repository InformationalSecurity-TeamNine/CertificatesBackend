package com.example.certificates.security;
import com.example.certificates.model.Token;
import com.example.certificates.service.interfaces.IUserService;
import com.google.gson.Gson;
import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;

public class RequestLoggingInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = request.getHeader("X-Auth-Token");
        if(token == null){
            logger.info("Neulogovan/neregistrovan korisnik je izvrsio : {} zahtev sa endpointa {}", request.getMethod(), request.getRequestURI());
            return true;
        }

        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        Gson g = new Gson();
        Token tolkien = g.fromJson(payload, Token.class);

        logger.info("Korisnik sa ID: {}, je izvrsio : {} zahtev sa endpointa {}", tolkien.getId(), request.getMethod(), request.getRequestURI());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // This method is called after the request has been processed by the controller
        // You can add additional logging or processing if needed
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // This method is called after the response has been sent to the client
        // You can add additional logging or processing if needed
    }
}
