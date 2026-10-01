package vn.iotstar.service;

import vn.iotstar.entity.Category;
import java.util.List;

public interface ICategoryService {
    List<Category> findAll();
    Category findById(Integer id);
    Category save(Category category);
    void deleteById(Integer id);
    void toggleActive(Integer id);
}
