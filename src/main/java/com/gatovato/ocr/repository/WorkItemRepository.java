/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gatovato.ocr.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import com.gatovato.ocr.entity.WorkItem;

/**
 *
 * @author paint
 */
public interface WorkItemRepository extends CrudRepository<WorkItem, Long> {
    
    WorkItem findById(long id);
    
    List<WorkItem> findByPath(String path);
    
}
