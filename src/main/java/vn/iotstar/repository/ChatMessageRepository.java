package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.ChatMessage;
import vn.iotstar.entity.User;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByUserIdAndStoreIdOrderByCreatedAtAsc(Long userId, Long storeId);

    @Query("SELECT DISTINCT m.user FROM ChatMessage m WHERE m.store.id = :storeId ORDER BY m.user.fullName ASC")
    List<User> findDistinctUsersByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT m FROM ChatMessage m WHERE m.store.id = :storeId AND m.user.id = :userId ORDER BY m.createdAt ASC")
    List<ChatMessage> findConversation(@Param("storeId") Long storeId, @Param("userId") Long userId);

    long countByStoreIdAndIsReadFalseAndSenderType(Long storeId, ChatMessage.SenderType senderType);

    @Transactional
    @Modifying
    @Query("DELETE FROM ChatMessage m WHERE m.user.id = :userId AND m.store.id = :storeId")
    void deleteByUserIdAndStoreId(@Param("userId") Long userId, @Param("storeId") Long storeId);

    @Transactional
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.user.id = :userId AND m.store.id = :storeId AND m.senderType = 'STORE_STAFF'")
    void markAllAsRead(@Param("userId") Long userId, @Param("storeId") Long storeId);

    @Query("SELECT DISTINCT m.user.id FROM ChatMessage m WHERE m.store.id = :storeId AND m.needsAttention = true")
    List<Long> findUserIdsNeedingAttention(@Param("storeId") Long storeId);
}
