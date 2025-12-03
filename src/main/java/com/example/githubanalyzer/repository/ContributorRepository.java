package com.example.githubanalyzer.repository;

import com.example.githubanalyzer.entity.ContributorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContributorRepository extends JpaRepository<ContributorEntity, Long> {
}
