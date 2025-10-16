package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;


public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {

    Page<Card> findAll(Specification<Card> spec, Pageable pageable);

    Optional<Card> findByCardNum(String cardNum);

}
