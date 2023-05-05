package Miner.GitLab.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ProjectServiceTest {

    @Autowired
    ProjectService service;

    @Test
    void findOne(){
        System.out.println(service.findOne("4207231"));
    }

    @Test
    void findAllCommits(){
        System.out.println(service.findAllCommits("4207231"));
    }

    @Test
    void findAllIssues(){
        System.out.println(service.findAllIssues("4207231"));
    }

    @Test
    void  findAllComments() {
        System.out.println(service.findAllComments("4207231", "2390"));
    }

    @Test
    void postGitMiner(){
        System.out.println(service.postGitMiner("4207231"));

    }

}
