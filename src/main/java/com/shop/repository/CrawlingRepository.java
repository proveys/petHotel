package com.shop.repository;

import com.shop.entity.Crawling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlingRepository  extends JpaRepository<Crawling, Long> {
}