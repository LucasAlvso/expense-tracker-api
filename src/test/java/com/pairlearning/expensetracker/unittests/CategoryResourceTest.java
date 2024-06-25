package com.pairlearning.expensetracker.unittests;
import com.pairlearning.expensetracker.domain.Category;
import com.pairlearning.expensetracker.resources.CategoryResource;
import com.pairlearning.expensetracker.services.CategoryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CategoryResourceTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private CategoryService categoryService;

    @Mock
    private CategoryService service;

    @InjectMocks
    private CategoryResource categoryResource;

    // this test is checking that the getAllCategories method correctly returns a list of categories when the service returns a list of categories.
    @Test
    public void testGetAllCategoriesOK() {
        Category category = new Category( 1, 1, "Test", "Test", 100.0);
        List<Category> t = new java.util.ArrayList<>();
        t.add(category);
        when(service.fetchAllCategories(1)).thenReturn(t);
        when(request.getAttribute("userId")).thenReturn(1);
        assertEquals(categoryResource.getAllCategories(request).getStatusCode(), HttpStatus.OK);

    }

    // this test is checking that the getAllCategories method correctly returns a list of categories when the service returns a list of categories.
    @Test
    public void testGetAllCategoriesCategory() {
        Category category = new Category( 1, 1, "Test", "Test", 100.0);
        Category category2 = new Category( 2, 1, "Test2", "Test2", 200.0);
        List<Category> t = new java.util.ArrayList<>();
        t.add(category);
        t.add(category2);
        when(service.fetchAllCategories(1)).thenReturn(t);
        when(request.getAttribute("userId")).thenReturn(1);
        assertEquals(ResponseEntity.ok(t), categoryResource.getAllCategories(request));
        verify(service, times(1)).fetchAllCategories(1);
    }
    // this test is checking that the getCategoryById method correctly returns a category when the service returns a category.
    @Test
    public void testGetCategoryById() {
        Category category = new Category( 1, 1, "Test", "Test", 100.0);
        when(service.fetchCategoryById(1, 1)).thenReturn(category);
        when(request.getAttribute("userId")).thenReturn(1);
        assertEquals(ResponseEntity.ok(category), categoryResource.getCategoryById(request, 1));
        verify(service, times(1)).fetchCategoryById(1, 1);
    }

    // this test is checking that the getCategoryById method correctly returns a category when the service returns a category.
    @Test
    public void shouldReturnCreatedStatusWhenCategoryIsAddedSuccessfully() {
        Map<String, Object> categoryMap = new HashMap<>();
        categoryMap.put("title", "New Category");
        categoryMap.put("description", "Description of new category");
        Category category = new Category(1, 1, "New Category", "Description of new category", 100.0);

        when(request.getAttribute("userId")).thenReturn(1);
        when(categoryService.addCategory(1, "New Category", "Description of new category")).thenReturn(category);

        ResponseEntity<Category> response = categoryResource.addCategory(request, categoryMap);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(category, response.getBody());
    }
    // this test is checking that the addCategory method correctly handles the case where the title of the category is missing by returning a BAD_REQUEST status code.
    @Test
    public void shouldReturnBadRequestWhenTitleIsMissing() {
        Map<String, Object> categoryMap = new HashMap<>();
        categoryMap.put("description", "Description of new category");

        when(request.getAttribute("userId")).thenReturn(1);

        ResponseEntity<Category> response = categoryResource.addCategory(request, categoryMap);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
 // this test is checking that the addCategory method correctly handles the case where the description of the category is missing by returning a BAD_REQUEST status code.
    @Test
    public void shouldReturnBadRequestWhenDescriptionIsMissing() {
        Map<String, Object> categoryMap = new HashMap<>();
        categoryMap.put("title", "New Category");

        when(request.getAttribute("userId")).thenReturn(1);

        ResponseEntity<Category> response = categoryResource.addCategory(request, categoryMap);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void shouldReturnOkStatusWhenCategoryIsUpdatedSuccessfully() {
        Category category = new Category(1, 1, "Updated Category", "Updated Description", 100.0);
        Map<String, Boolean> successMap = new HashMap<>();
        successMap.put("success", true);

        when(request.getAttribute("userId")).thenReturn(1);
        doNothing().when(categoryService).updateCategory(1, 1, category);

        ResponseEntity<Map<String, Boolean>> response = categoryResource.updateCategory(request, 1, category);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(successMap, response.getBody());
    }
    // this test is checking that the updateCategory method correctly handles the case where the category to update is not found by throwing a RuntimeException.
    @Test
    public void shouldThrowExceptionWhenCategoryNotFound() {
        Category category = new Category(1, 1, "Updated Category", "Updated Description", 100.0);

        when(request.getAttribute("userId")).thenReturn(1);
        doThrow(new RuntimeException()).when(categoryService).updateCategory(1, 1, category);

        assertThrows(RuntimeException.class, () -> categoryResource.updateCategory(request, 1, category));
    }

    // this test is checking that the deleteCategory method correctly returns a OK status when the category is deleted successfully.
    @Test
    public void shouldReturnOkStatusWhenCategoryIsDeletedSuccessfully() {
        Map<String, Boolean> successMap = new HashMap<>();
        successMap.put("success", true);

        when(request.getAttribute("userId")).thenReturn(1);
        doNothing().when(categoryService).removeCategoryWithAllTransactions(1, 1);

        ResponseEntity<Map<String, Boolean>> response = categoryResource.deleteCategory(request, 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(successMap, response.getBody());
    }
    // this test is checking that the deleteCategory method correctly handles the case where the category to delete is not found by throwing a RuntimeException.
    @Test
    public void shouldThrowExceptionWhenCategoryToDeleteNotFound() {
        when(request.getAttribute("userId")).thenReturn(1);
        doThrow(new RuntimeException()).when(categoryService).removeCategoryWithAllTransactions(1, 1);

        assertThrows(RuntimeException.class, () -> categoryResource.deleteCategory(request, 1));
    }

}
