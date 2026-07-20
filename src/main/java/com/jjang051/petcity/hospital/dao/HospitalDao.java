package com.jjang051.petcity.hospital.dao;

import com.jjang051.petcity.animal.dto.AnimalTypeDto;
import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalSubAnimalDto;
import com.jjang051.petcity.hospital.dto.HospitalReviewDto;
import com.jjang051.petcity.hospital.dto.MedicalServiceDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HospitalDao {
    List<HospitalDto> findHospitalList(
            @Param("page") int page,
            @Param("offset") int offset,
            @Param("limit") int limit,
            @Param("animalId") Integer animalId,
            @Param("subAnimalId") Integer subAnimalId,
            @Param("subjects") List<String> subjects, // 🌟 진료과목 파라미터 추가
            @Param("serviceIds") List<Integer> serviceIds,
            @Param("districts") List<String> districts,
            @Param("keyword") String keyword,
            @Param("openStatus") String openStatus,
            @Param("sort") String sort,
            @Param("userLat") Double userLat,
            @Param("userLng") Double userLng
    );

    int countHospitalList(
            @Param("animalId") Integer animalId,
            @Param("subAnimalId") Integer subAnimalId,
            @Param("subjects") List<String> subjects, // 🌟 진료과목 파라미터 추가
            @Param("serviceIds") List<Integer> serviceIds,
            @Param("districts") List<String> districts,
            @Param("keyword") String keyword,
            @Param("openStatus") String openStatus
    );

    List<String> findDistrictList();
    List<AnimalTypeDto> findAnimalTypeList();
    List<HospitalSubAnimalDto> findSubAnimalTypeList();
    List<MedicalServiceDto> findMedicalServiceList();
    List<String> findMedicalSubjectList(); // 🌟 진료과목 리스트 조회 추가

    HospitalDto findHospitalById(@Param("hospitalId") int hospitalId, @Param("userLat") Double userLat, @Param("userLng") Double userLng);

    List<Integer> findMyZzimList(int memberId);
    List<Integer> findMyLikeList(int memberId);
    int checkZzim(@Param("hospitalId") int hospitalId, @Param("memberId") int memberId);
    void insertZzim(@Param("hospitalId") int hospitalId, @Param("memberId") int memberId);
    void deleteZzim(@Param("hospitalId") int hospitalId, @Param("memberId") int memberId);
    int checkLike(@Param("hospitalId") int hospitalId, @Param("memberId") int memberId);
    void insertLike(@Param("hospitalId") int hospitalId, @Param("memberId") int memberId);
    void deleteLike(@Param("hospitalId") int hospitalId, @Param("memberId") int memberId);

    void insertReview(HospitalReviewDto review);
    List<HospitalReviewDto> findReviewListByHospitalId(int hospitalId);
    void updateReviewReply(@Param("reviewId") int reviewId, @Param("replyContent") String replyContent, @Param("replyRole") String replyRole);

    void updateReview(HospitalReviewDto reviewDto);
    void deleteReview(int reviewId);
}