package com.example.airplanning.controller;

import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.dto.board.*;
import com.example.airplanning.domain.entity.Region;
import com.example.airplanning.domain.enum_class.Category;
import com.example.airplanning.domain.enum_class.LikeType;
import com.example.airplanning.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;


@Controller
@RequiredArgsConstructor
@RequestMapping("/boards")
@Slf4j
public class BoardController {

    private final BoardService boardService;
    private final PlannerService plannerService;
    private final LikeService likeService;
    private final RegionService regionService;

    // 게시판 리스트 페이지
    @GetMapping("/{category}/list")
    public String listBoardPage(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                            Model model, @PathVariable String category,
                            @RequestParam(required = false) String searchType,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(required = false) String region1,
                            @RequestParam(required = false) Long regionId,
                            @AuthenticationPrincipal UserDetail userDetail) {

        Category enumCategory;
        if (category.equals("free")) enumCategory = Category.FREE;
        else if (category.equals("rankup")) enumCategory = Category.RANK_UP;
        else if (category.equals("report")) enumCategory = Category.REPORT;
        else if (category.equals("portfolio")) enumCategory = Category.PORTFOLIO;
        else {
            model.addAttribute("msg", "잘못된 접근입니다.");
            model.addAttribute("nextUrl", "/");
            return "error/redirect";
        }

        Page<BoardListResponse> boardPage;

        if (enumCategory.equals(Category.PORTFOLIO)) {
            boardPage = boardService.portfolioList(pageable, searchType, keyword, region1, regionId);
            model.addAttribute("list", boardPage);

            List<Region> regions = regionService.findAll();
            HashSet<String> region1List = new HashSet<>();
            for (Region region : regions) {
                region1List.add(region.getRegion1());
            }

            model.addAttribute("region1List", region1List);
            model.addAttribute("regions", regions);
            model.addAttribute("portfolioSearchRequest", new PortfolioSearchRequest(searchType, keyword, region1, regionId));

            return "boards/portfolioList";

        } else {
            boardPage = boardService.boardList(pageable, searchType, keyword, enumCategory);
            model.addAttribute("list", boardPage);
            model.addAttribute("boardSearchRequest", new BoardSearchRequest(searchType, keyword));

            if (enumCategory.equals(Category.FREE)) {
                return "boards/freeList";
            } else if (enumCategory.equals(Category.REPORT)) {
                return "boards/reportList";
            } else if (enumCategory.equals(Category.RANK_UP)) {
                if (userDetail != null) {
                    model.addAttribute("userRole", userDetail.getRole());
                }
                return "boards/rankUpList";
            }
        }

        return "/";
    }

    // 게시판 글 작성 페이지
    @GetMapping("/{category}/write")
    public String writeBoardPage(@PathVariable String category, Model model, Principal principal) {

        model.addAttribute("boardCreateRequest", new BoardCreateRequest());

        if (category.equals("rankup")) {
            List<Region> regions = regionService.findAll();
            HashSet<String> region1List = new HashSet<>();
            for (Region region : regions) {
                region1List.add(region.getRegion1());
            }

            model.addAttribute("region1List", region1List);
            model.addAttribute("regions", regions);
            return "boards/rankUpWrite";

        } else {
            if (category.equals("free")) {
                return "boards/freeWrite";
            } else if (category.equals("report")) {
                return "boards/reportWrite";
            } else if (category.equals("portfolio")) {
                model.addAttribute("planner", plannerService.findByUser(principal.getName()));
                return "boards/portfolioWrite";
            } else {
                model.addAttribute("msg", "잘못된 접근입니다.");
                model.addAttribute("nextUrl", "/");
                return "error/redirect";
            }
        }

    }

    // 게시판 글 조회 페이지
    @GetMapping("/{category}/{boardId}")
    public String detailBoard(@PathVariable Long boardId, @PathVariable String category, Model model, Principal principal,
                              HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal UserDetail userDetail){

        Category enumCategory;
        if (category.equals("free")) enumCategory = Category.FREE;
        else if (category.equals("report")) enumCategory = Category.REPORT;
        else if (category.equals("rankup")) enumCategory = Category.RANK_UP;
        else if (category.equals("portfolio")) enumCategory = Category.PORTFOLIO;
        else {
            model.addAttribute("msg", "잘못된 접근입니다.");
            model.addAttribute("nextUrl", "/");
            return "error/redirect";
        }

        if (enumCategory.equals(Category.FREE) || enumCategory.equals(Category.PORTFOLIO)) {

            // 조회수 관련 로직
            Cookie oldCookie = null;
            Cookie[] cookies = request.getCookies();
            Boolean addView = true;
            if(cookies != null) {
                for(Cookie cookie : cookies) {
                    if(cookie.getName().equals("boardView")) {
                        oldCookie = cookie;
                        break;
                    }
                }
            }
            if(oldCookie != null && oldCookie.getValue().equals(boardId.toString())) {
                addView = false;
            } else {
                Cookie newCookie = new Cookie("boardView", boardId.toString());
                newCookie.setMaxAge(60 * 60);   // 한 시간
                response.addCookie(newCookie);
            }

            BoardDto boardDto = boardService.detail(boardId, addView, enumCategory);
            model.addAttribute("board", boardDto);

            // 댓글 관련 => ajax를 통해 화면에서 불러옴

            if (enumCategory.equals(Category.FREE)) {
                // 좋아요 관련
                if (principal != null) {
                    model.addAttribute("checkLike", likeService.checkLike(boardId, principal.getName()));

                    // 로그인 유저가 글 작성자라면 수정, 삭제 버튼 출력
                    if(principal.getName().equals(boardDto.getUserName())) {
                        model.addAttribute("isWriter", true);
                    }
                } else {
                    model.addAttribute("checkLike", false);
                }

                return "boards/freeDetail";
            } else {
                model.addAttribute("planner", plannerService.findByUser(boardDto.getUserName()));
                return "boards/portfolioDetail";
            }

        } else {

            BoardDto boardDto = boardService.detail(boardId, false, enumCategory);

            // 작성자 본인이거나 ADMIN이면 출력
            if (principal.getName().equals(boardDto.getUserName()) || userDetail.getRole().equals("ADMIN")) {
                model.addAttribute("board", boardDto);
                model.addAttribute("userName", principal.getName());

                if (enumCategory.equals(Category.REPORT)) {
                    return "boards/reportDetail";
                } else {
                    return "boards/rankUpDetail";
                }
            } else {
                model.addAttribute("msg", "작성자만 조회 가능합니다.");
                model.addAttribute("nextPage", "/boards/" + category + "/list");
                return "error/redirect";
            }
        }
    }

    // 게시판 글 수정 페이지
    @GetMapping("/{category}/{boardId}/update")
    public String updateBoardPage(@PathVariable String category, @PathVariable Long boardId, Model model, Principal principal){
        Category enumCategory;
        if (category.equals("free")) enumCategory = Category.FREE;
        else if (category.equals("report")) enumCategory = Category.REPORT;
        else if (category.equals("rankup")) enumCategory = Category.RANK_UP;
        else if (category.equals("portfolio")) enumCategory = Category.PORTFOLIO;
        else {
            model.addAttribute("msg", "잘못된 접근입니다.");
            model.addAttribute("nextUrl", "/");
            return "error/redirect";
        }

        BoardDto boardDto = boardService.detail(boardId, false, enumCategory);
        model.addAttribute(new BoardUpdateRequest(boardDto.getTitle(), boardDto.getContent(), boardDto.getImage()));

        if (enumCategory.equals(Category.FREE)) return "boards/freeUpdate";
        else if (enumCategory.equals(Category.REPORT)) return "boards/reportUpdate";
        else if (enumCategory.equals(Category.RANK_UP)) {
            List<Region> regions = regionService.findAll();
            HashSet<String> region1List = new HashSet<>();
            for (Region region : regions) {
                region1List.add(region.getRegion1());
            }

            model.addAttribute("region1List", region1List);
            model.addAttribute("regions", regions);
            return "boards/rankUpUpdate";
        }
        else if (enumCategory.equals(Category.PORTFOLIO)) {
            model.addAttribute("planner", plannerService.findByUser(principal.getName()));
            return "boards/portfolioUpdate";
        }

        return "/";
    }

    // 좋아요
    @PostMapping("/{boardId}/like")
    @ResponseBody
    public String changeLike(@PathVariable Long boardId, Principal principal) {
        return likeService.changeLike(boardId, principal.getName(), LikeType.BOARD_LIKE);
    }

}
