package com.example.airplanning.controller.api;


import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.Response;
import com.example.airplanning.domain.dto.comment.*;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.service.CommentService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentRestController {

    private final CommentService commentService;

    @PostMapping
    @ApiOperation(value = "댓글 작성", notes = "로그인 한 유저만 댓글을 작성 가능합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "commentType", value = "댓글 타입. board, review 중 하나를 입력", defaultValue = "None"),
            @ApiImplicitParam(name = "content", value = "댓글 내용", defaultValue = "None"),
            @ApiImplicitParam(name = "postId", value = "댓글이 달릴 글 번호", defaultValue = "None")})
    public Response<CommentResponse> create (@ModelAttribute CommentCreateRequest request, @ApiIgnore @AuthenticationPrincipal UserDetail userDetail){
        CommentResponse createdComment = null;

        if (userDetail == null) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        } else {
            createdComment = commentService.createComment(request, userDetail.getId());
        }
        return Response.success(createdComment);
    }

    @PostMapping("/coco")
    @ApiOperation(value = "대댓글 작성", notes = "로그인 한 유저만 댓글을 작성 가능합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "commentType", value = "댓글 타입. board, review 중 하나를 입력", defaultValue = "None"),
            @ApiImplicitParam(name = "content", value = "댓글 내용", defaultValue = "None"),
            @ApiImplicitParam(name = "postId", value = "댓글이 달릴 글 번호", defaultValue = "None"),
            @ApiImplicitParam(name = "parentId", value = "대댓글이 달릴 댓글 번호", defaultValue = "None")})
    public Response<CommentResponse> createCoco (@ModelAttribute CoCommentCreateRequest request, @ApiIgnore @AuthenticationPrincipal UserDetail userDetail) {
        CommentResponse coco = commentService.createCoComment(request, userDetail.getId());
        return Response.success(coco);
    }

    @GetMapping("/{postId}/{postType}")
    @ApiOperation(value = "댓글 목록 조회", notes = "해당 글에 작성된 모든 댓글을 확인합니다. 누구나 조회 가능합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "postType", value = "글 타입. board, review 중 하나를 입력", defaultValue = "None"),
            @ApiImplicitParam(name = "postId", value = "글 번호", defaultValue = "None")})
    public Response<Page<CommentResponse>> readList (@PathVariable Long postId, @PathVariable String postType, @ApiIgnore @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CommentResponse> commentPage = commentService.readComment(postId, postType, pageable);
        return Response.success(commentPage);
    }

    @PutMapping
    @ApiOperation(value = "댓글 수정", notes = "해당 댓글을 작성한 유저만 수정할 수 있습니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "commentType", value = "댓글 타입. board, review 중 하나를 입력", defaultValue = "None"),
            @ApiImplicitParam(name = "content", value = "수정할 댓글 내용", defaultValue = "None"),
            @ApiImplicitParam(name = "postId", value = "댓글이 달린 글 번호", defaultValue = "None"),
            @ApiImplicitParam(name = "targetCommentId", value = "수정할 댓글 번호", defaultValue = "None")})
    public Response<CommentResponse> update (@ModelAttribute CommentUpdateRequest request, @ApiIgnore @AuthenticationPrincipal UserDetail userDetail) {
        CommentResponse updatedComment = commentService.updateComment(request, userDetail.getId());
        return Response.success(updatedComment);
    }

    @DeleteMapping
    @ApiOperation(value = "댓글 삭제", notes = "해당 댓글을 작성한 유저만 삭제할 수 있습니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "targetCommentId", value = "삭제할 댓글 번호", defaultValue = "None"),
            @ApiImplicitParam(name = "postId", value = "글 번호", defaultValue = "None")})
    public Response<String> delete (@ModelAttribute CommentDeleteRequest request, @ApiIgnore @AuthenticationPrincipal UserDetail userDetail) {
        Long parentId = commentService.deleteComment(request, userDetail.getId());
        commentService.deleteParent(parentId);
        return Response.success("댓글이 삭제되었습니다.");
    }

}
