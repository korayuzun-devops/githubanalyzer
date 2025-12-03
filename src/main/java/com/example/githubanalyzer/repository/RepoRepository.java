package com.example.githubanalyzer.repository;

import com.example.githubanalyzer.entity.RepoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoRepository extends JpaRepository<RepoEntity, Long> {
}
