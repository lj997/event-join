package com.eventjoin.repository;

import com.eventjoin.entity.Event;
import com.eventjoin.entity.Registration;
import com.eventjoin.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    
    boolean existsByUserAndEventAndIsCancelledFalse(User user, Event event);
    
    Optional<Registration> findByUserAndEvent(User user, Event event);
    
    @Query("SELECT r FROM Registration r JOIN FETCH r.event e JOIN FETCH e.createdBy " +
           "WHERE r.user.id = :userId AND r.isCancelled = :isCancelled")
    Page<Registration> findByUserIdWithEvent(@Param("userId") Long userId,
                                              @Param("isCancelled") Boolean isCancelled,
                                              Pageable pageable);
    
    @Query("SELECT r FROM Registration r JOIN FETCH r.user " +
           "WHERE r.event.id = :eventId AND r.isCancelled = :isCancelled")
    List<Registration> findByEventIdWithUser(@Param("eventId") Long eventId,
                                              @Param("isCancelled") Boolean isCancelled);
    
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId AND r.isCancelled = false")
    long countByEventIdAndIsCancelledFalse(@Param("eventId") Long eventId);
    
    @Query("SELECT r FROM Registration r JOIN FETCH r.user u JOIN FETCH r.event e " +
           "WHERE e.id = :eventId AND r.isCancelled = false")
    List<Registration> findAllActiveByEventId(@Param("eventId") Long eventId);
}
