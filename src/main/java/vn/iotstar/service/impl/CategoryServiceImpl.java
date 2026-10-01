package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.Category;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.service.ICategoryService;
import vn.iotstar.util.SlugUtil;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Category findById(Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Override
    public Category save(Category category) {
        if (category.getSlug() == null || category.getSlug().isBlank()) {
            category.setSlug(SlugUtil.toSlug(category.getCategoryName()));
        }
        return categoryRepository.save(category);
    }

    @Override
    public void deleteById(Integer id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public void toggleActive(Integer id) {
        categoryRepository.findById(id).ifPresent(c -> {
            c.setIsActive(!Boolean.TRUE.equals(c.getIsActive()));
            categoryRepository.save(c);
        });
    }
}
