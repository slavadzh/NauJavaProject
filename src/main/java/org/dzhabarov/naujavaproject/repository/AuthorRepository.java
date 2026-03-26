package org.dzhabarov.naujavaproject.repository;

import org.dzhabarov.naujavaproject.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "authors")
public interface AuthorRepository extends JpaRepository<Author, Long> {
}
