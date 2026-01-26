package uk.gov.hmcts.dev.service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import uk.gov.hmcts.dev.dto.SearchCriteria;
import uk.gov.hmcts.dev.model.Task;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.*;

public class CaseSearchSpecification {
  public static Specification<Task> withCriteria(SearchCriteria keyword) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (nonNull(keyword.title())) {
        predicates.add(
                cb.like(
                        cb.lower(root.get(SqlColumn.TITLE)),
                        SqlConstant.SQL_WILDCARD +
                                keyword.title().toLowerCase() +
                                SqlConstant.SQL_WILDCARD
                )
        );
      }

      if (nonNull(keyword.description())) {
        predicates.add(
                cb.like(
                        cb.lower(root.get(SqlColumn.DESCRIPTION)),
                        SqlConstant.SQL_WILDCARD +
                                keyword.description().toLowerCase() +
                                SqlConstant.SQL_WILDCARD
                )
        );
      }

      if(nonNull(keyword.status())){
        predicates.add(
                cb.equal(
                        root.get(SqlColumn.STATUS),
                        keyword.status()
                )
        );
      }

      if(nonNull(keyword.dueFrom()) && nonNull(keyword.dueTo())){
        predicates.add(
                cb.between(
                        root.get(SqlColumn.DUE),
                        keyword.dueFrom(),
                        keyword.dueTo()
                )
        );
      }

      if(nonNull(keyword.createdBy())){
        predicates.add(
                cb.equal(
                        root.get(SqlColumn.CREATED_BY),
                        keyword.createdBy()
                )
        );
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}

interface SqlColumn {
  String TITLE = "title";
  String DESCRIPTION = "description";
  String STATUS = "status";
  String DUE = "due";
  String CREATED_BY = "createdBy";
}

interface SqlConstant{
  String SQL_WILDCARD = "%";
}
