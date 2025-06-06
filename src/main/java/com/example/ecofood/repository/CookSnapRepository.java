package com.example.ecofood.repository;

import com.example.ecofood.domain.CookSnap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CookSnapRepository extends JpaRepository<CookSnap,Long> {

    List<CookSnap> findCookSnapByRecipeId(Long Id);
}
