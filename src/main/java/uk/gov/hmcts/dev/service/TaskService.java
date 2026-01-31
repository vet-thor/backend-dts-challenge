package uk.gov.hmcts.dev.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.dev.dto.CaseRequest;
import uk.gov.hmcts.dev.dto.SearchCriteria;
import uk.gov.hmcts.dev.dto.TaskResponseData;
import uk.gov.hmcts.dev.util.SecurityUtils;
import uk.gov.hmcts.dev.util.helper.ErrorMessageHelper;
import uk.gov.hmcts.dev.mapper.TaskMapper;
import uk.gov.hmcts.dev.model.TaskStatus;
import uk.gov.hmcts.dev.repository.TaskRepository;

import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ErrorMessageHelper errorMessageHelper;
    private final TaskMapper mapper;

    @Transactional
    public TaskResponseData createTask(CaseRequest request){

        if(isNull(request.status())) {
            request = new CaseRequest(
                    null,
                    request.title(),
                    request.description(),
                    TaskStatus.OPEN,
                    request.due()
            );
        }

        var task = mapper.toTask(request);

        var response = taskRepository.save(task);

        return TaskResponseData.builder()
                .task(mapper.toTaskResponse(response))
                .build();
    }

    public TaskResponseData getTask(SearchCriteria keywords){
        var pageable = PageRequest.of(keywords.page(), keywords.limit(), Sort.by(keywords.sortOrder(), keywords.sortBy()));

        var cases = taskRepository.findAll(
                CaseSearchSpecification.withCriteria(keywords),
                pageable);

        return TaskResponseData.builder()
                .tasks(mapper.pageToTasksResponse(cases))
                .totalElement(cases.getTotalElements())
                .totalPage(cases.getTotalPages())
                .build();
    }

    @Cacheable(value = "task", key="#id")
    public TaskResponseData getTask(UUID id){
        var response = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(errorMessageHelper.caseNotFoundErrorMessage()));

        return TaskResponseData.builder()
                .task(mapper.toTaskResponse(response))
                .build();
    }

    @Transactional
    @CachePut(value = "task", key = "#request.id")
    public TaskResponseData updateTask(CaseRequest request){
        var task = taskRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException(errorMessageHelper.caseNotFoundErrorMessage()));

        if(nonNull(request.title())){
            task.setTitle(request.title());
        }

        if(nonNull(request.description())){
            task.setDescription(request.description());
        }

        if(nonNull(request.status())){
            task.setStatus(request.status());
        }

        if(nonNull(request.due())){
            task.setDue(request.due());
        }

        SecurityUtils.getPrincipal().ifPresent(jwtUserDetails ->
                task.setUpdatedBy(jwtUserDetails.getId()));

        return TaskResponseData.builder()
                .task(mapper.toTaskResponse(taskRepository.save(task)))
                .build();
    }

    @Transactional
    @CacheEvict(value = "task", key = "#id")
    public void deleteTask(UUID id){
        var response = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(errorMessageHelper.caseNotFoundErrorMessage()));

        response.setDeleted(true);

        taskRepository.save(response);
    }
}
