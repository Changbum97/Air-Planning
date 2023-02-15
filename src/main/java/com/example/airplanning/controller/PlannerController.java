package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.planner.PlannerDetailResponse;
import com.example.airplanning.domain.dto.planner.PlannerEditRequest;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.entity.Region;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.LikeType;
import com.example.airplanning.service.LikeService;
import com.example.airplanning.service.PlannerService;
import com.example.airplanning.service.RegionService;
import com.example.airplanning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;

@Controller
@RequestMapping("/planners")
@RequiredArgsConstructor
public class PlannerController {

    private final PlannerService plannerService;
    private final LikeService likeService;
    private final UserService userService;
    private final RegionService regionService;

    @GetMapping("/{plannerId}")
    public String detail(@PathVariable Long plannerId, Model model, Principal principal) {
        PlannerDetailResponse response = plannerService.findById(plannerId);
        model.addAttribute("planner", response);

        if (principal != null) {
            model.addAttribute("checkLike", likeService.checkPlannerLike(plannerId, principal.getName()));

            // 로그인 유저가 플래너 본인이라면 수정 버튼 출력
            if(principal.getName().equals(response.getUserName())) {
                model.addAttribute("isWriter", true);
            }

        } else {
            model.addAttribute("checkLike", false);
        }
        return "users/planner";
    }

    @PostMapping("/{plannerId}/like")
    @ResponseBody
    public String changeLike(@PathVariable Long plannerId, Principal principal) {
        return likeService.changeLike(plannerId, principal.getName(), LikeType.PLANNER_LIKE);
    }

    // 플래너 지역, 설명 수정
    @GetMapping("/{plannerId}/edit")
    public String editPage(@PathVariable Long plannerId, Model model, Principal principal) {
        UserDto userDto = userService.findUser(principal.getName());
        PlannerDetailResponse response = plannerService.findById(plannerId);

        if (!userDto.getUserName().equals(response.getUserName())) {
            model.addAttribute("msg", "본인만 수정가능합니다.");
            model.addAttribute("nextPage", "/planners/" + plannerId);
            return "error/redirect";
        }

        List<Region> regions = regionService.findAll();
        HashSet<String> region1List = new HashSet<>();
        for (Region region : regions) {
            region1List.add(region.getRegion1());
        }

        model.addAttribute("region1List", region1List);
        model.addAttribute("regions", regions);

        model.addAttribute("plannerEditRequest", new PlannerEditRequest(response.getId(), response.getDescription()));
        return "users/plannerEdit";
    }

    @ResponseBody
    @PostMapping("/{plannerId}/edit")
    public Long edit(@PathVariable Long plannerId,PlannerEditRequest plannerEditRequest) {
        System.out.println("REgionId : " + plannerEditRequest.getRegionId());
        return plannerService.edit(plannerId, plannerEditRequest);
    }
}
