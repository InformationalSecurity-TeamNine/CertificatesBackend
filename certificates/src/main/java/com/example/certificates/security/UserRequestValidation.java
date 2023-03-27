package com.example.certificates.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserRequestValidation {
    private final JwtTokenUtil jwtTokenUtil;


    @Autowired
    public UserRequestValidation(JwtTokenUtil jwtTokenUtil){
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public Long getIdFromToken(Map<String, String> headers){
        String token = headers.get("x-auth-token");
        return jwtTokenUtil.getId(token).longValue();
    }
    public  String getRoleFromToken(Map<String, String> headers){
        String token = headers.get("x-auth-token");
        List<HashMap<String, String>> role;
        role = jwtTokenUtil.getRole(token);

        for (String values: role.get(0).values()){
            return values;
        }
        return "";
    }

    public  boolean areIdsEqual(Map<String, String> headers, Long givenId){
        String token = headers.get("x-auth-token");
        Integer id = jwtTokenUtil.getId(token);
        return givenId.intValue() == id;
    }

    public Integer getUserId(Map<String, String> headers) {
        String token = headers.get("x-auth-token");
        return jwtTokenUtil.getId(token);
    }

    private  boolean isTokenExpired(String token){
        return jwtTokenUtil.isExpired(token);
    }
}
