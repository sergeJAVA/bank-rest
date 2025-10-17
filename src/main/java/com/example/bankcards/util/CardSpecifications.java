package com.example.bankcards.util;

import com.example.bankcards.entity.Card;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class CardSpecifications {

    public static Specification<Card> userId(Long userId) {
        return (root, query, cb) ->
                userId == null ? cb.conjunction() : cb.equal(root.get("user").get("id"), userId);
    }

}
