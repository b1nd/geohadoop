package ru.b1nd.namenode.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.b1nd.namenode.domain.File;

public interface FileRepository extends CrudRepository<File, Long> {

    File findFileByName(String name);
}
