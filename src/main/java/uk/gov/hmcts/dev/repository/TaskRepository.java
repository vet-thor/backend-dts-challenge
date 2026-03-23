package uk.gov.hmcts.dev.repository;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import uk.gov.hmcts.dev.model.Task;

import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {
    boolean existsByIdAndCreatedBy(UUID taskId, UUID createBy);
    boolean existsByTitleIgnoreCase(String title);
    boolean existsByTitleIgnoreCaseAndCreatedBy(String title, UUID createdBy);
}
