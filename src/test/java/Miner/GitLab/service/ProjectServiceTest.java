package Miner.GitLab.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ProjectServiceTest {

    @Autowired
    ProjectService service;

    private static final String id = "4207231";

    @Test
    void findOne(){
        System.out.println(service.findOne(id, 2, 2, 20));
    }

    @Test
    void findAllCommits(){
        System.out.println(service.findAllCommits(id, 2, 2));
    }

    @Test
    void findAllIssues(){
        System.out.println(service.findAllIssues(id, 20, 2));
    }

    @Test
    void  findAllComments() {
        System.out.println(service.findAllComments(id, "2390", 2));
    }


}
