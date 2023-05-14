package Miner.GitLab.controller;


import Miner.GitLab.model.commit.Commit;
import Miner.GitLab.model.gitMiner.Example;
import Miner.GitLab.model.issue.Issue;
import Miner.GitLab.model.project.Project;
import Miner.GitLab.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("gitlab/projects")
public class ProjectController {

    @Autowired
    private ProjectService service;

    @GetMapping("/{id}")
    public Project findOne(@PathVariable(value = "id") String id,
                           @RequestParam(defaultValue = "2") Integer sinceCommits,
                           @RequestParam(defaultValue = "20") Integer sinceIssues,
                           @RequestParam(defaultValue = "2") Integer maxPages){
        return service.findOne(id, sinceCommits, sinceIssues, maxPages);
    }

    @PostMapping("/{id}")
    public Example postGitMiner(@PathVariable(value = "id") String id,
                                @RequestParam(defaultValue = "2") Integer sinceCommits,
                                @RequestParam(defaultValue = "20") Integer sinceIssues,
                                @RequestParam(defaultValue = "2") Integer maxPages){
        return service.postGitMiner(id, sinceCommits, sinceIssues, maxPages);
    }
}
