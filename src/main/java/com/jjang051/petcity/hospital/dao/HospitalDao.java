package com.jjang051.petcity.hospital.dao;

import com.jjang051.petcity.animal.dto.AnimalTypeDto;
import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalReviewDto;
import com.jjang051.petcity.hospital.dto.HospitalSubAnimalDto;
import com.jjang051.petcity.hospital.dto.MedicalServiceDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HospitalDao {

    List<HospitalDto> findHospitalList(
            @Param("animalId") Integer animalId,
            @Param("subAnimalId") Integer subAnimalId,
            @Param("serviceIds") List<Integer> serviceIds,
            @Param("districts") List <String> districts,
            @Param("keyword") String keyword,
            @Param("openStatus") String openStatus,
            @Param("sort") String sort,
            @Param("userLat") Double userLat,
            @Param("userLng") Double userLng,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    int countHospitalList(
            @Param("animalId") Integer animalId,
            @Param("subAnimalId") Integer subAnimalId,
            @Param("serviceIds") List<Integer> serviceIds,
            @Param("districts") List <String> districts,
            @Param("keyword") String keyword,
            @Param("openStatus") String openStatus
    );

    HospitalDto findHospitalById(@Param("hospitalId") int hospitalId,
                                 @Param("userLat") Double userLat,
                                 @Param("userLng") Double userLng);

    List<String> findDistrictList();
    List<AnimalTypeDto> findAnimalTypeList();
    List<HospitalSubAnimalDto> findSubAnimalTypeList();
    List<MedicalServiceDto> findMedicalServiceList();

    // 🌟 찜하기(즐겨찾기) 기능
    int checkZzim(@Param("hospitalId") int hospitalId, @Param("memberId") int memberId);
    void insertZzim(@Param("hospitalId") int hospitalId, @Param("memberId") int memberId);
    void deleteZzim(@Param("hospitalId") int hospitalId, @Param("memberId") int memberId);
    List<Integer> findMyZzimList(int memberId);

    // 🌟 추천(좋아요) 기능
    int checkLike(@Param("hospitalId") int hospitalId, @Param("memberId") int memberId);
    void insertLike(@Param("hospitalId") int hospitalId, @Param("memberId") int memberId);
    void deleteLike(@Param("hospitalId") int hospitalId, @Param("memberId") int memberId);
    List<Integer> findMyLikeList(int memberId);

    // 🌟 리뷰 기능
    void insertReview(HospitalReviewDto reviewDto);
    List<HospitalReviewDto> findReviewListByHospitalId(int hospitalId);
}