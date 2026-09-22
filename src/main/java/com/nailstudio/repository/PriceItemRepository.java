package com.nailstudio.repository;

import com.nailstudio.model.PriceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceItemRepository extends JpaRepository<PriceItem, Long> {
}