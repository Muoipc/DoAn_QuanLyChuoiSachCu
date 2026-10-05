package vn.iotstar.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE " +
           "(n.recipientRole = 'ROLE_ADMIN' OR n.recipientRole = 'ALL' OR n.recipient.id = :userId) " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findForAdmin(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE " +
           "(n.recipientRole = 'ALL' OR (n.recipientRole = 'ROLE_MANAGER' AND n.store.id = :storeId) OR n.recipient.id = :userId) " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findForManager(@Param("userId") Long userId, @Param("storeId") Long storeId, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE " +
           "(n.recipientRole = 'ROLE_ADMIN' OR n.recipientRole = 'ALL' OR n.recipient.id = :userId) AND n.isRead = false")
    long countUnreadForAdmin(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE " +
           "(n.recipientRole = 'ALL' OR (n.recipientRole = 'ROLE_MANAGER' AND n.store.id = :storeId) OR n.recipient.id = :userId) AND n.isRead = false")
    long countUnreadForManager(@Param("userId") Long userId, @Param("storeId") Long storeId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE " +
           "(n.recipientRole = 'ROLE_ADMIN' OR n.recipientRole = 'ALL' OR n.recipient.id = :userId) AND n.isRead = false")
    void markAllReadForAdmin(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE " +
           "(n.recipientRole = 'ALL' OR (n.recipientRole = 'ROLE_MANAGER' AND n.store.id = :storeId) OR n.recipient.id = :userId) AND n.isRead = false")
    void markAllReadForManager(@Param("userId") Long userId, @Param("storeId") Long storeId);
}
