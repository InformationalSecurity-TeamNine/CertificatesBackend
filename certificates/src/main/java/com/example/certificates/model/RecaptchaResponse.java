package com.example.certificates.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecaptchaResponse {

    private boolean success;
    private String challenge_ts;
    private String hostname;
}
