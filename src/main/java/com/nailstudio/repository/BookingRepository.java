package com.nailstudio.repository;

import com.nailstudio.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findAllByOrderByCreatedAtDesc(Pageable pageable);
}