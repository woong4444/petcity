package com.jjang051.petcity.pet.dao;

import com.jjang051.petcity.pet.dto.PetDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PetDao {
    void insertPet(PetDto petDto);
    void updatePet(PetDto petDto);
    List<PetDto> findPetsByMemberId(int memberId);

    void deletePet(int petId);
}