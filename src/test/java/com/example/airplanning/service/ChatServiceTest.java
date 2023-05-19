package com.example.airplanning.service;

import com.example.airplanning.domain.dto.chat.ChatMessageDto;
import com.example.airplanning.domain.dto.chat.ChatRoomDto;
import com.example.airplanning.domain.dto.chat.CreateChatRoomRequest;
import com.example.airplanning.domain.entity.*;
import com.example.airplanning.domain.enum_class.AlarmType;
import com.example.airplanning.domain.enum_class.UserRole;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.AlarmRepository;
import com.example.airplanning.repository.ChatMessageRepository;
import com.example.airplanning.repository.ChatRoomRepository;
import com.example.airplanning.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    ChatService chatService;

    SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
    ChatRoomRepository chatRoomRepository = mock(ChatRoomRepository.class);
    ChatMessageRepository chatMessageRepository = mock(ChatMessageRepository.class);
    AlarmRepository alarmRepository = mock(AlarmRepository.class);
    AlarmService alarmService = mock(AlarmService.class);
    UserRepository userRepository = mock(UserRepository.class);

    static ChatRoom chatRoom1;
    static ChatMessage chatMessage1, chatMessage2;
    static User user1, user2;
    static LocalDateTime now;
    static final Pageable pageable = PageRequest.of(0, 10);;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(template, chatRoomRepository, chatMessageRepository, alarmRepository, alarmService, userRepository);

        now = LocalDateTime.now();
        user1 = User.builder().id(1L).userName("user1").nickname("nick1").role(UserRole.USER).build();
        user2 = User.builder().id(2L).userName("user2").nickname("nick2").role(UserRole.USER).build();
        chatRoom1 = ChatRoom.builder().id(1L).user1Id(1L).user2Id(2L).lastMessageId(1L).build();
        chatMessage1 = ChatMessage.builder().id(1L).chatRoom(chatRoom1).isRead(false).message("메세지 내용1").writerId(1L).build();
        chatMessage2 = ChatMessage.builder().id(2L).chatRoom(chatRoom1).isRead(false).message("메세지 내용2").writerId(2L).build();
        ReflectionTestUtils.setField(chatRoom1, BaseEntity.class, "updatedAt", now, LocalDateTime.class);
        ReflectionTestUtils.setField(chatMessage1, BaseEntity.class, "createdAt", now, LocalDateTime.class);
        ReflectionTestUtils.setField(chatMessage2, BaseEntity.class, "createdAt", now, LocalDateTime.class);
    }

    @Test
    @DisplayName("메세지 전송 성공 Test 1 - user1이 user2에게 메세지 전송 + user2에게 알람이 없는 경우")
    void sendChatMessageSuccess1() {
        ChatMessageDto dto = ChatMessageDto.builder().messageType("TALK").createdAt(now).id(1L).message("메세지 내용1").writerId(1L).isRead(false).roomId(1L).build();

        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom1));
        when(chatMessageRepository.save(any())).thenReturn(chatMessage1);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(alarmRepository.findByUserAndAlarmType(user2, AlarmType.CHATTING_ALARM)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> chatService.sendChatMessage(dto));

        verify(alarmService).send(any(), any(), any(), any());
        verify(template).convertAndSend((String) any(), (Object) any());
    }

    @Test
    @DisplayName("메세지 전송 성공 Test 2 - user2가 user1에게 메세지 전송 + user1에게 알람이 없는 경우")
    void sendChatMessageSuccess2() {
        ChatMessageDto dto = ChatMessageDto.builder().messageType("TALK").createdAt(now).id(2L).message("메세지 내용2").writerId(2L).isRead(false).roomId(1L).build();

        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom1));
        when(chatMessageRepository.save(any())).thenReturn(chatMessage2);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(alarmRepository.findByUserAndAlarmType(user1, AlarmType.CHATTING_ALARM)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> chatService.sendChatMessage(dto));

        verify(alarmService).send(any(), any(), any(), any());
        verify(template).convertAndSend((String) any(), (Object) any());
    }

    @Test
    @DisplayName("메세지 전송 성공 Test 3 - user1이 user2에게 메세지 전송 + user2에게 알람이 있는 경우")
    void sendChatMessageSuccess3() {
        ChatMessageDto dto = ChatMessageDto.builder().messageType("TALK").createdAt(now).id(1L).message("메세지 내용1").writerId(1L).isRead(false).roomId(1L).build();
        Alarm alarm1 = Alarm.builder().id(1L).user(user2).alarmType(AlarmType.CHATTING_ALARM).build();

        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom1));
        when(chatMessageRepository.save(any())).thenReturn(chatMessage1);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(alarmRepository.findByUserAndAlarmType(user2, AlarmType.CHATTING_ALARM)).thenReturn(Optional.of(alarm1));

        assertDoesNotThrow(() -> chatService.sendChatMessage(dto));

        verify(alarmService).send(any(), any(), any(), any());
        verify(template).convertAndSend((String) any(), (Object) any());
    }

    @Test
    @DisplayName("메세지 전송 성공 Test 4 - user2가 user1에게 메세지 전송 + user1에게 알람이 있는 경우")
    void sendChatMessageSuccess4() {
        ChatMessageDto dto = ChatMessageDto.builder().messageType("TALK").createdAt(now).id(2L).message("메세지 내용2").writerId(2L).isRead(false).roomId(1L).build();
        Alarm alarm1 = Alarm.builder().id(1L).user(user1).alarmType(AlarmType.CHATTING_ALARM).build();

        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom1));
        when(chatMessageRepository.save(any())).thenReturn(chatMessage2);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(alarmRepository.findByUserAndAlarmType(user1, AlarmType.CHATTING_ALARM)).thenReturn(Optional.of(alarm1));

        assertDoesNotThrow(() -> chatService.sendChatMessage(dto));

        verify(alarmService).send(any(), any(), any(), any());
        verify(template).convertAndSend((String) any(), (Object) any());
    }

    @Test
    @DisplayName("메세지 전송 성공 Test 5 - 메세지가 입장 메세지인 경우")
    void sendChatMessageSuccess5() {
        ChatMessageDto dto = ChatMessageDto.builder().messageType("ENTER").createdAt(now).id(1L).writerId(1L).isRead(false).roomId(1L).build();

        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(chatMessage1);

        when(chatMessageRepository.findByChatRoomIdAndWriterIdNotAndIsRead(1L, 1L, false))
                .thenReturn(chatMessages);

        assertDoesNotThrow(() -> chatService.sendChatMessage(dto));

        verify(chatMessageRepository).save(any());
        verify(template).convertAndSend((String) any(), (Object) any());
    }

    @Test
    @DisplayName("메세지 전송 성공 Test 6 - 메세지가 읽었다는 메세지인 경우")
    void sendChatMessageSuccess6() {
        ChatMessageDto dto = ChatMessageDto.builder().messageType("READ").createdAt(now).targetMessageId(1L).build();

        when(chatMessageRepository.findById(1L)).thenReturn(Optional.of(chatMessage1));

        assertDoesNotThrow(() -> chatService.sendChatMessage(dto));

        verify(chatMessageRepository).save(any());
        verify(template).convertAndSend((String) any(), (Object) any());
    }

    @Test
    @DisplayName("메세지 전송 실패 Test 1 - user1이 user2에게 메세지 전송 + user2에 해당하는 유저가 없는 경우")
    void sendChatMessageFail1() {
        ChatMessageDto dto = ChatMessageDto.builder().messageType("TALK").createdAt(now).id(1L).message("메세지 내용1").writerId(1L).isRead(false).roomId(1L).build();

        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom1));
        when(chatMessageRepository.save(any())).thenReturn(chatMessage1);
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> chatService.sendChatMessage(dto));
        assertEquals(ErrorCode.USER_NOT_FOUNDED, e.getErrorCode());
    }

    @Test
    @DisplayName("메세지 전송 실패 Test 2 - user2가 user1에게 메세지 전송 + user1에 해당하는 유저가 없는 경우")
    void sendChatMessageFail2() {
        ChatMessageDto dto = ChatMessageDto.builder().messageType("TALK").createdAt(now).id(1L).message("메세지 내용1").writerId(2L).isRead(false).roomId(1L).build();

        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom1));
        when(chatMessageRepository.save(any())).thenReturn(chatMessage2);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());


        AppException e = assertThrows(AppException.class, () -> chatService.sendChatMessage(dto));
        assertEquals(ErrorCode.USER_NOT_FOUNDED, e.getErrorCode());
    }

    @Test
    @DisplayName("채팅방 생성 성공 Test 1")
    void createChatRoomSuccess1() {
        CreateChatRoomRequest req = new CreateChatRoomRequest(1L ,2L);
        ChatMessage enterMessage = ChatMessage.builder().id(1L).message("채팅방이 개설되었습니다.").isRead(false).writerId(1L).chatRoom(chatRoom1).build();

        when(chatRoomRepository.findByUser1IdAndUser2Id(1L, 2L)).thenReturn(Optional.empty());
        when(chatRoomRepository.save(any())).thenReturn(chatRoom1);
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(chatMessageRepository.save(any())).thenReturn(enterMessage);

        Long savedChatRoomId = assertDoesNotThrow(() -> chatService.createChatRoom(req, "user1"));
        assertEquals(1L, savedChatRoomId);

        verify(chatMessageRepository).save(any());
    }

    @Test
    @DisplayName("채팅방 생성 실패 Test 1 - 이미 유저간의 채팅방이 있는 경우")
    void createChatRoomFail1() {
        CreateChatRoomRequest req = new CreateChatRoomRequest(2L ,1L);

        when(chatRoomRepository.findByUser1IdAndUser2Id(1L, 2L)).thenReturn(Optional.of(chatRoom1));

        Long chatRoomId = assertDoesNotThrow(() -> chatService.createChatRoom(req, "user1"));
        assertEquals(1L, chatRoomId);
    }

    @Test
    @DisplayName("채팅방 생성 실패 Test 2 - 유저가 존재하지 않는 경우")
    void createChatRoomFail2() {
        CreateChatRoomRequest req = new CreateChatRoomRequest(1L ,2L);

        when(chatRoomRepository.findByUser1IdAndUser2Id(1L, 2L)).thenReturn(Optional.empty());
        when(chatRoomRepository.save(any())).thenReturn(chatRoom1);
        when(userRepository.findByUserName("user1")).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> chatService.createChatRoom(req, "user1"));
        assertEquals(ErrorCode.USER_NOT_FOUNDED, e.getErrorCode());
    }

    @Test
    @DisplayName("유저가 속한 채팅방 모두 찾기 성공 Test 1 - 마지막 메세지 작성 유저가 찾은 경우 + 마지막 메세지를 상대가 읽지 않은 경우")
    void findMyRoomsSuccess1() {

        List<ChatRoom> chatRooms = new ArrayList<>();
        chatRooms.add(chatRoom1);
        Page<ChatRoom> chatRoomPage = new PageImpl<>(chatRooms, pageable, 1);

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(chatRoomRepository.findByUser1IdOrUser2Id(1L, 1L, pageable)).thenReturn(chatRoomPage);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(chatMessageRepository.findById(1L)).thenReturn(Optional.of(chatMessage1));

        List<ChatRoomDto> result = assertDoesNotThrow(() -> chatService.findMyRooms("user1", pageable));
        assertEquals(1, result.size());

        ChatRoomDto resultDto = result.get(0);
        assertEquals(1L, resultDto.getId());
        assertEquals("nick2", resultDto.getOtherUserNickname());
        assertEquals(false, resultDto.getHasNew());
    }

    @Test
    @DisplayName("유저가 속한 채팅방 모두 찾기 성공 Test 2 - 마지막 메세지 작성 유저가 찾은 경우 + 마지막 메세지를 상대가 읽은 경우")
    void findMyRoomsSuccess2() {

        List<ChatRoom> chatRooms = new ArrayList<>();
        chatRooms.add(chatRoom1);
        Page<ChatRoom> chatRoomPage = new PageImpl<>(chatRooms, pageable, 1);

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(chatRoomRepository.findByUser1IdOrUser2Id(1L, 1L, pageable)).thenReturn(chatRoomPage);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        chatMessage1.read();
        when(chatMessageRepository.findById(1L)).thenReturn(Optional.of(chatMessage1));

        List<ChatRoomDto> result = assertDoesNotThrow(() -> chatService.findMyRooms("user1", pageable));
        assertEquals(1, result.size());

        ChatRoomDto resultDto = result.get(0);
        assertEquals(1L, resultDto.getId());
        assertEquals("nick2", resultDto.getOtherUserNickname());
        assertEquals(false, resultDto.getHasNew());
    }

    @Test
    @DisplayName("유저가 속한 채팅방 모두 찾기 성공 Test 3 - 마지막 메세지 상대 유저가 찾은 경우 + 마지막 메세지를 상대가 읽지 않은 경우")
    void findMyRoomsSuccess3() {

        List<ChatRoom> chatRooms = new ArrayList<>();
        chatRooms.add(chatRoom1);
        Page<ChatRoom> chatRoomPage = new PageImpl<>(chatRooms, pageable, 1);

        when(userRepository.findByUserName("user2")).thenReturn(Optional.of(user2));
        when(chatRoomRepository.findByUser1IdOrUser2Id(2L, 2L, pageable)).thenReturn(chatRoomPage);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(chatMessageRepository.findById(1L)).thenReturn(Optional.of(chatMessage1));

        List<ChatRoomDto> result = assertDoesNotThrow(() -> chatService.findMyRooms("user2", pageable));
        assertEquals(1, result.size());

        ChatRoomDto resultDto = result.get(0);
        assertEquals(1L, resultDto.getId());
        assertEquals("nick1", resultDto.getOtherUserNickname());
        assertEquals(true, resultDto.getHasNew());
    }

    @Test
    @DisplayName("유저가 속한 채팅방 모두 찾기 성공 Test 4 - 마지막 메세지 상대 유저가 찾은 경우 + 마지막 메세지를 상대가 읽은 경우")
    void findMyRoomsSuccess4() {

        List<ChatRoom> chatRooms = new ArrayList<>();
        chatRooms.add(chatRoom1);
        Page<ChatRoom> chatRoomPage = new PageImpl<>(chatRooms, pageable, 1);

        when(userRepository.findByUserName("user2")).thenReturn(Optional.of(user2));
        when(chatRoomRepository.findByUser1IdOrUser2Id(2L, 2L, pageable)).thenReturn(chatRoomPage);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        chatMessage1.read();
        when(chatMessageRepository.findById(1L)).thenReturn(Optional.of(chatMessage1));

        List<ChatRoomDto> result = assertDoesNotThrow(() -> chatService.findMyRooms("user2", pageable));
        assertEquals(1, result.size());

        ChatRoomDto resultDto = result.get(0);
        assertEquals(1L, resultDto.getId());
        assertEquals("nick1", resultDto.getOtherUserNickname());
        assertEquals(false, resultDto.getHasNew());
    }

    @Test
    @DisplayName("유저가 속한 채팅방 모두 찾기 실패 Test 1 - 유저가 존재하지 않는 경우")
    void findMyRoomsFail1() {
        when(userRepository.findByUserName("user1")).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> chatService.findMyRooms("user1", pageable));
        assertEquals(ErrorCode.USER_NOT_FOUNDED, e.getErrorCode());
    }

    @Test
    @DisplayName("유저가 속한 채팅방 모두 찾기 실패 Test 2 - 상대 유저가 존재하지 않는 경우 1")
    void findMyRoomsFail2() {

        List<ChatRoom> chatRooms = new ArrayList<>();
        chatRooms.add(chatRoom1);
        Page<ChatRoom> chatRoomPage = new PageImpl<>(chatRooms, pageable, 1);

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(chatRoomRepository.findByUser1IdOrUser2Id(1L, 1L, pageable)).thenReturn(chatRoomPage);
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> chatService.findMyRooms("user1", pageable));
        assertEquals(ErrorCode.USER_NOT_FOUNDED, e.getErrorCode());
    }

    @Test
    @DisplayName("유저가 속한 채팅방 모두 찾기 실패 Test 3 - 상대 유저가 존재하지 않는 경우 2")
    void findMyRoomsFail3() {

        List<ChatRoom> chatRooms = new ArrayList<>();
        chatRooms.add(chatRoom1);
        Page<ChatRoom> chatRoomPage = new PageImpl<>(chatRooms, pageable, 1);

        when(userRepository.findByUserName("user2")).thenReturn(Optional.of(user2));
        when(chatRoomRepository.findByUser1IdOrUser2Id(2L, 2L, pageable)).thenReturn(chatRoomPage);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> chatService.findMyRooms("user2", pageable));
        assertEquals(ErrorCode.USER_NOT_FOUNDED, e.getErrorCode());
    }

    @Test
    @DisplayName("유저가 속한 채팅방 모두 찾기 실패 Test 4 - 채팅방의 마지막 메세지가 존재하지 않는 경우")
    void findMyRoomsFail4() {

        List<ChatRoom> chatRooms = new ArrayList<>();
        chatRooms.add(chatRoom1);
        Page<ChatRoom> chatRoomPage = new PageImpl<>(chatRooms, pageable, 1);

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(user1));
        when(chatRoomRepository.findByUser1IdOrUser2Id(1L, 1L, pageable)).thenReturn(chatRoomPage);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(chatMessageRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> chatService.findMyRooms("user1", pageable));
        assertEquals(ErrorCode.CHAT_MESSAGE_NOT_FOUNDED, e.getErrorCode());
    }

    @Test
    @DisplayName("채팅방 조회 성공 Test")
    void findRoomSuccess() {
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom1));

        ChatRoom chatRoom = assertDoesNotThrow(() -> chatService.findRoomById(1L));
        assertEquals(chatRoom1, chatRoom);
    }

    @Test
    @DisplayName("채팅방 조회 실패 Test - 채팅방이 존재하지 않는 경우")
    void findRoomFail() {
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> chatService.findRoomById(1L));
        assertEquals(ErrorCode.CHAT_ROOM_NOT_FOUNDED, e.getErrorCode());
    }

    @Test
    @DisplayName("읽지 않은 메세지 리스트 조회 성공 Test")
    void findNotReadMessagesSuccess() {
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(chatMessage1);

        when(chatMessageRepository.findByChatRoomIdAndWriterIdNotAndIsRead(1L, 1L, false))
                .thenReturn(chatMessages);

        List<ChatMessage> result = chatService.findNotReadMessages(1L, 1L);
        assertEquals(chatMessages, result);
    }

    @Test
    @DisplayName("다음 메세지 리스트 조회 성공 Test")
    void findMoreMessagesSuccess() {
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(chatMessage1);
        Page<ChatMessage> chatMessagePage = new PageImpl<>(chatMessages, pageable, 1);

        when(chatMessageRepository.findByChatRoomIdAndIdLessThan(1L, 2L, pageable))
                .thenReturn(chatMessagePage);

        Page<ChatMessage> result = chatService.findMoreMessages(1L,2L, pageable);
        assertEquals(chatMessagePage, result);
    }

    @Test
    @DisplayName("채팅방 삭제 성공 Test")
    void deleteChatRoomSuccess() {
        chatService.deleteRoom(1L);
        verify(chatRoomRepository).deleteById(1L);
    }
}