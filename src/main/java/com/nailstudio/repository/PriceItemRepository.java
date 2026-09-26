package com.nailstudio.repository;

import com.nailstudio.model.PriceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PriceItemRepository extends JpaRepository<PriceItem, Long> {

    Optional<PriceItem> findByNameIgnoreCase(String name);
}
