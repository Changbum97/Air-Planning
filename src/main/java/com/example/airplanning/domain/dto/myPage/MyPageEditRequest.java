package com.example.airplanning.domain.dto.myPage;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MyPageEditRequest {

    private String password;
    private String nickname;

}
