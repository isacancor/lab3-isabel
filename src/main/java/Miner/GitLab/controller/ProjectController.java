package Miner.GitLab.controller;


import Miner.GitLab.model.gitMiner.Example;
import Miner.GitLab.model.project.Project;
import Miner.GitLab.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("gitlab/projects")
public class ProjectController {

    @Autowired
    private ProjectService service;

    @GetMapping("/{id}")
    public Project findOne(@PathVariable(value = "id") String id){
        return service.findOne(id);
    }

    @PostMapping("/{id}")
    public Example postGitMiner(@PathVariable(value = "id") String id){
        return service.postGitMiner(id);
    }
}
