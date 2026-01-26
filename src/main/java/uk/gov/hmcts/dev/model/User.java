package uk.gov.hmcts.dev.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Setter
@Getter
@Entity
@Table(name = "person")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@SQLRestriction("deleted <> true")
public class User extends AbstractBaseEntity{
    @NotNull
    private String username;
    @NotNull
    @Column(nullable = false, updatable = false)
    private String password;
    private String role;
    private LangType lang;
}
