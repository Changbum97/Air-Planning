package com.example.airplanning.service;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.AlarmType;
import com.example.airplanning.domain.enum_class.UserRole;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final SessionRegistry sessionRegistry;

    private final AlarmService alarmService;

    @Transactional
    public UserDto changeRank(Long id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        user.changeRank(role);
        User changedUser = userRepository.save(user);

        alarmService.send(user, AlarmType.CHANGE_ROLE_ALARM, "/users/mypage/"+user.getId(), role);

        if (role.equals(UserRole.BLACKLIST.name())) {
            List<UserDetail> userDetails = sessionRegistry.getAllPrincipals()
                    .stream().map(o ->(UserDetail) o).collect(Collectors.toList());

            for (UserDetail userDetail : userDetails) {
                if (userDetail.getId() == id) {
                    List<SessionInformation> sessionList = sessionRegistry.getAllSessions(userDetail, false);
                    for (SessionInformation session : sessionList) {
                        session.expireNow();
                    }
                }
            }
        }
        return UserDto.of(changedUser);
    }
}
