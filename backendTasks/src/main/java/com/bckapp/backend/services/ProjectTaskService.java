package com.bckapp.backend.services;

import com.bckapp.backend.domain.Backlog;
import com.bckapp.backend.domain.ProjectTask;
import com.bckapp.backend.exceptions.ProjectNotFoundException;
import com.bckapp.backend.repositories.BacklogRepository;
import com.bckapp.backend.repositories.ProjectRepository;
import com.bckapp.backend.repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectTaskService {
    @Autowired
    private BacklogRepository backlogRepository;

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectService projectService;

    public ProjectTask addProjectTask(String projectIdentifier, ProjectTask projectTask, String username){


            //Svi projektni zadaci treba da budu dodati u specificni projekat i projekat je != null, Backlog postoji
            Backlog backlog =  projectService.findProjectByIdentifier(projectIdentifier, username).getBacklog(); //backlogRepository.findByProjectIdentifier(projectIdentifier);
            //setovanje backloga do project taska
            System.out.println(backlog);
            projectTask.setBacklog(backlog);
            //hocu da projektna sekvenca bude kao: IDPRO-1 IDPRO-2 ...pa da sledeci budu 100 101 iako se izbrise neki
            Integer BacklogSequence = backlog.getPTSequence();
            //Updatovanje BL SEQUENCE
            BacklogSequence++;

            backlog.setPTSequence(BacklogSequence);
            //Dodati sekvencu u Project Task
            projectTask.setProjectSequence(backlog.getProjectIdentifier()+"-"+BacklogSequence);
            projectTask.setProjectIdentifier(projectIdentifier);

            //INITIAL priority kada je priority null

            //INITIAL status kada je status null
        if(projectTask.getStatus()==""|| projectTask.getStatus()==null){
            projectTask.setStatus("TO_DO");
        }

            // bag sa priority u Spring Boot Server-u, mora da se prvo proveri da li je null
            if(projectTask.getPriority()==null||projectTask.getPriority()==0){
            projectTask.setPriority(3);
        }
            return projectTaskRepository.save(projectTask);
    }

    public Iterable<ProjectTask>findBacklogById(String id, String username){
        projectService.findProjectByIdentifier(id, username);
//        Project project = projectRepository.findByProjectIdentifier(id);
//
//        if(project==null){
//            throw new ProjectNotFoundException("Project with ID: '"+id+"' does not exist");
//        }
        return projectTaskRepository.findByProjectIdentifierOrderByPriority(id);
    }

    public ProjectTask findPTByProjectSequence(String backlog_id, String pt_id, String username){
        //biti siguran da se trazi postojeci backlog
        projectService.findProjectByIdentifier(backlog_id, username);

        //biti siguran da Task postoji
        ProjectTask projectTask = projectTaskRepository.findByProjectSequence(pt_id);

        if(projectTask == null){
            throw new ProjectNotFoundException("Project Task '"+pt_id+"' not found");
        }
        //biti siguran da backlog/project id u putanji koja odgovara pravom projektu
        if(!projectTask.getProjectIdentifier().equals(backlog_id)){
            throw new ProjectNotFoundException("Project Task '"+pt_id+"' does not exist in project: '"+backlog_id);
        }
        return projectTask;
    }
    public ProjectTask updateByProjectSequence(ProjectTask updatedTask, String backlog_id, String pt_id, String username){
        ProjectTask projectTask = findPTByProjectSequence(backlog_id, pt_id, username);

        projectTask = updatedTask;

        return projectTaskRepository.save(projectTask);
    }

    public void deletePTByProjectSequence(String backlog_id, String pt_id, String username){
        ProjectTask projectTask = findPTByProjectSequence(backlog_id, pt_id, username);

        // brisanje taska radi na nacin ispod ali je on ruzan!
//        Backlog backlog = projectTask.getBacklog();
//        List<ProjectTask> pts = backlog.getProjectTasks();
//        pts.remove(projectTask);
//        backlogRepository.save(backlog);

        projectTaskRepository.delete(projectTask);
    }
}