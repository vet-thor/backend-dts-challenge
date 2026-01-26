package uk.gov.hmcts.dev.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "task")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@SQLRestriction("deleted <> true")
public class Task extends AbstractBaseEntity{
    @NonNull
    private String title;
    @NonNull
    private String description;
    @NonNull
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
    @NonNull
    private LocalDateTime due;

}
