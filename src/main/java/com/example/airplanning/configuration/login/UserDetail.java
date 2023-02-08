package com.example.airplanning.configuration.login;

import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;


// 인증 관련 사용자 정보를 담은 Dto
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class UserDetail implements UserDetails, OAuth2User {
    private Long id;
    private String userName;    // 로그인에 사용할 ID
    private String password;    // 비밀번호
    private String role;      // 권한 (USER, ADMIN, BLACKLIST, PLANNER)

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    // 권한부여
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        //authorities.add(new SimpleGrantedAuthority(role));

        if (this.role.equals(UserRole.USER.name())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        } else if (this.role.equals(UserRole.ADMIN.name())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else if (this.role.equals(UserRole.PLANNER.name())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_PLANNER"));
        } else if (this.role.equals(UserRole.BLACKLIST.name())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_BLACKLIST"));
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    // 계정 만료 여부
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 겨정 잠금 여부
    @Override
    public boolean isAccountNonLocked() {
        if (this.role.equals("BLACKLIST")) {
            return false;
        } else {
            return true;
        }
    }

    // 비밀번호 만료 여부
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 사용 가능 여부
    @Override
    public boolean isEnabled() {
        return true;
    }

    public static UserDetail of(User user) {
        return UserDetail.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .password(user.getPassword())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public String getName() {
        return null;
    }
}
