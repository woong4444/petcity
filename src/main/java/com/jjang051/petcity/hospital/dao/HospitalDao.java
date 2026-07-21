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

    // 병원 리스트 조회
    List<HospitalDto> findHospitalList(
            @Param("offset") int offset,
            @Param("limit") int limit,
            @Param("openStatus") String openStatus,
            @Param("animalId") Integer animalId,
            @Param("subAnimalId") Integer subAnimalId,
            @Param("subjects") List<String> subjects,
            @Param("serviceIds") List<Integer> serviceIds,
            @Param("districts") List<String> districts,
            @Param("keyword") String keyword,
            @Param("sort") String sort,
            @Param("userLat") Double userLat,
            @Param("userLng") Double userLng
    );

    // 병원 개수 카운트
    int countHospitalList(
            @Param("openStatus") String openStatus,
            @Param("animalId") Integer animalId,
            @Param("subAnimalId") Integer subAnimalId,
            @Param("subjects") List<String> subjects,
            @Param("serviceIds") List<Integer> serviceIds,
            @Param("districts") List<String> districts,
            @Param("keyword") String keyword
    );

    List<String> findDistrictList();
    List<AnimalTypeDto> findAnimalTypeList();
    List<HospitalSubAnimalDto> findSubAnimalTypeList();
    List<MedicalServiceDto> findMedicalServiceList();

    // 🌟 추가된 진료과목 리스트 조회
    List<String> findMedicalSubjectList();

    // 병원 상세 조회
    HospitalDto findHospitalById(
            @Param("hospitalId") Long hospitalId,
            @Param("userLat") Double userLat,
            @Param("userLng") Double userLng
    );

    // 찜, 좋아요, 리뷰 관련 메서드들
    List<Integer> findMyZzimList(@Param("memberId") Long memberId);
    List<Integer> findMyLikeList(@Param("memberId") Long memberId);

    int checkZzim(@Param("hospitalId") Long hospitalId, @Param("memberId") Long memberId);
    void insertZzim(@Param("hospitalId") Long hospitalId, @Param("memberId") Long memberId);
    void deleteZzim(@Param("hospitalId") Long hospitalId, @Param("memberId") Long memberId);

    int checkLike(@Param("hospitalId") Long hospitalId, @Param("memberId") Long memberId);
    void insertLike(@Param("hospitalId") Long hospitalId, @Param("memberId") Long memberId);
    void deleteLike(@Param("hospitalId") Long hospitalId, @Param("memberId") Long memberId);

    void insertReview(
            @Param("hospitalId") Long hospitalId,
            @Param("memberId") Long memberId,
            @Param("rating") int rating,
            @Param("content") String content,
            @Param("petId") Integer petId
    );

    List<HospitalReviewDto> findReviewListByHospitalId(@Param("hospitalId") Long hospitalId);

    void updateReviewReply(
            @Param("reviewId") Long reviewId,
            @Param("replyContent") String replyContent,
            @Param("replyRole") String replyRole
    );

    void updateReview(HospitalReviewDto dto);

    void deleteReview(@Param("reviewId") int reviewId);
}