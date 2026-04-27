package com.eventjoin.repository;

import com.eventjoin.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    @Query("SELECT n FROM Notification n JOIN FETCH n.event e " +
           "WHERE n.user.id = :userId AND n.isRead = :isRead " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndIsRead(@Param("userId") Long userId,
                                               @Param("isRead") Boolean isRead);
    
    @Query("SELECT n FROM Notification n JOIN FETCH n.event e " +
           "WHERE n.user.id = :userId " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdWithEvent(@Param("userId") Long userId, Pageable pageable);
    
    long countByUserIdAndIsRead(Long userId, Boolean isRead);
}
