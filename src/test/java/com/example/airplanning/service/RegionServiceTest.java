package com.example.airplanning.service;

import com.example.airplanning.domain.entity.Region;
import com.example.airplanning.domain.enum_class.LikeType;
import com.example.airplanning.repository.RegionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegionServiceTest {

    RegionService regionService;
    RegionRepository regionRepository = mock(RegionRepository.class);

    @BeforeEach
    void setUp() {
        regionService = new RegionService(regionRepository);
    }

    @Test
    @DisplayName("Region FindAll 성공 Test")
    void regionFindAllSuccess() {
        Region region1 = Region.builder().id(1L).region1("경기도").region2("수원시").build();
        Region region2 = Region.builder().id(2L).region1("경기도").region2("안양시").build();

        List<Region> regions = new ArrayList<>();
        regions.add(region1);
        regions.add(region2);

        when(regionRepository.findAll()).thenReturn(regions);

        List<Region> result = assertDoesNotThrow(() -> regionService.findAll());
        assertEquals(result, regions);
    }

}