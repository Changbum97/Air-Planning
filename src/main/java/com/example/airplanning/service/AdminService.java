package com.example.airplanning.service;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.AlarmType;
import com.example.airplanning.domain.enum_class.UserRole;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.BoardRepository;
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

    private final BoardRepository boardRepository;

    @Transactional
    public UserDto changeRank(String userName, String role, Long boardId) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        user.changeRank(role);
        User changedUser = userRepository.save(user);

        alarmService.send(user, AlarmType.CHANGE_ROLE_ALARM, "/users/mypage/"+user.getId(), role);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(()->new AppException(ErrorCode.BOARD_NOT_FOUND));

        if (board.getUser().getId() != user.getId()){
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        boardRepository.delete(board);

        if (role.equals(UserRole.BLACKLIST.name())) {
            List<UserDetail> userDetails = sessionRegistry.getAllPrincipals()
                    .stream().map(o ->(UserDetail) o).collect(Collectors.toList());

            for (UserDetail userDetail : userDetails) {
                if (userDetail.getId() == user.getId()) {
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
