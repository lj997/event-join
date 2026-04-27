package com.eventjoin.repository;

import com.eventjoin.entity.Event;
import com.eventjoin.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    Page<Event> findByStatus(EventStatus status, Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.status = :status " +
           "AND (:keyword IS NULL OR e.title LIKE %:keyword% OR e.description LIKE %:keyword%) " +
           "AND (:location IS NULL OR e.location LIKE %:location%) " +
           "AND (:startDate IS NULL OR e.eventDateTime >= :startDate) " +
           "AND (:endDate IS NULL OR e.eventDateTime <= :endDate)")
    Page<Event> searchEvents(@Param("status") EventStatus status,
                              @Param("keyword") String keyword,
                              @Param("location") String location,
                              @Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate,
                              Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.status = :status " +
           "AND e.eventDateTime >= :now " +
           "AND e.eventDateTime <= :tomorrow")
    List<Event> findEventsStartingSoon(@Param("status") EventStatus status,
                                        @Param("now") LocalDateTime now,
                                        @Param("tomorrow") LocalDateTime tomorrow);
    
    Optional<Event> findByIdAndStatus(Long id, EventStatus status);
    
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.createdBy WHERE e.id = :id")
    Optional<Event> findByIdWithCreator(@Param("id") Long id);
}
