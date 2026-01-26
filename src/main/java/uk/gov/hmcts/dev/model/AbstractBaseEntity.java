package uk.gov.hmcts.dev.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.dev.dto.JwtUserDetails;
import uk.gov.hmcts.dev.util.SecurityUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Objects.isNull;

@Getter
@Setter
@SuperBuilder
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID id;
    @Column(columnDefinition = "bool default false")
    protected boolean deleted;
    @Column(columnDefinition = "timestamp with time zone DEFAULT CURRENT_DATE NOT NULL", updatable = false)
    protected LocalDateTime createdAt;
    @Column(columnDefinition = "timestamp with time zone")
    protected LocalDateTime updatedAt;
    @Column(updatable = false)
    protected UUID createdBy;
    @Column(updatable = false)
    protected UUID updatedBy;

    @PrePersist
    protected void onCreate() {
        if(isNull(createdAt)){
            createdAt = LocalDateTime.now();
        }

        if(isNull(createdBy)){
            createdBy = SecurityUtils.getPrincipal()
                    .map(JwtUserDetails::getId)
                    .orElse(null);
        }
    }

    @PreUpdate
    protected void onModify(){
        if(isNull(updatedAt)){
            updatedAt = LocalDateTime.now();
        }

        if(isNull(updatedBy)){
            updatedBy = SecurityUtils.getPrincipal()
                    .map(JwtUserDetails::getId)
                    .orElse(null);
        }
    }
}
