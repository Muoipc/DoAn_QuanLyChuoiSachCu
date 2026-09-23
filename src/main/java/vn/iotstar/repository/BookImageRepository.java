package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.BookImage;

import java.util.List;

@Repository
public interface BookImageRepository extends JpaRepository<BookImage, Long> {
    List<BookImage> findByBookIdOrderByDisplayOrderAsc(Long bookId);
}
