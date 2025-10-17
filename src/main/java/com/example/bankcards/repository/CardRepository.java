package com.example.bankcards.repository;

import com.example.bankcards.dto.CardStatus;
import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {

    @EntityGraph(attributePaths = {"user"})
    Page<Card> findAll(Specification<Card> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Optional<Card> findByCardNum(String cardNum);

    @EntityGraph(attributePaths = {"user"})
    List<Card> findAllByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<Card> findByExpirationDateBeforeAndStatusNot(LocalDateTime date, CardStatus status);

    @EntityGraph(attributePaths = {"user"})
    Optional<Card> findByCardNumAndUser_Id(String cardNum, Long id);

}
