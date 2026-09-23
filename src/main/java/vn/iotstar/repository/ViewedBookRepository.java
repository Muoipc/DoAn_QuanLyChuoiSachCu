package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.ViewedBook;

import java.util.Optional;

@Repository
public interface ViewedBookRepository extends JpaRepository<ViewedBook, Long> {
    Page<ViewedBook> findByUserIdOrderByViewedAtDesc(Long userId, Pageable pageable);
    Optional<ViewedBook> findByUserIdAndBookId(Long userId, Long bookId);
}
