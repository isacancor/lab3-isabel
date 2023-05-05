package Miner.GitLab.service;

import Miner.GitLab.model.comment.Comment;
import Miner.GitLab.model.comment.CommentSearch;
import Miner.GitLab.model.comment.Note;
import Miner.GitLab.model.commit.Commit;
import Miner.GitLab.model.commit.CommitSearch;
import Miner.GitLab.model.issue.Issue;
import Miner.GitLab.model.issue.IssueSearch;
import Miner.GitLab.model.project.Project;
import Miner.GitLab.model.project.ProjectSearch;
import Miner.GitLab.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ProjectService {

    @Autowired
    RestTemplate restTemplate;

    private final String uri = "https://gitlab.com/api/v4/projects/";
    private final String token = "glpat-3aox7aT7i7sWL_MERLPt";

    public Project findOne(String id){
        ProjectSearch p = restTemplate.getForObject(uri + id, ProjectSearch.class);
        Project project = Project.ofFormat(p);

        project.setCommits(findAllCommits(project.getId()));
        project.setIssues(findAllIssues(project.getId()));

        return project;
    }

    public List<Commit> findAllCommits(String id){
        CommitSearch[] c = restTemplate.getForObject(uri + id + "/repository/commits",
                CommitSearch[].class);

        return Arrays.stream(c).toList().stream()
                .map(e-> Commit.ofFormat(e)).toList();
    }

    public List<Issue> findAllIssues(String id){
        List<Issue> res = new ArrayList<>();
        IssueSearch[] i = restTemplate.getForObject(uri + id + "/issues",
                IssueSearch[].class);

        for(IssueSearch s:i){
            Issue issue = Issue.ofFormat(s);

            if(s.getAssignee() != null){
                issue.setAsignee(User.ofFormat(s.getAssignee()));
            }
            issue.setAuthor(User.ofFormat(s.getAuthor()));
            issue.setComments(findAllComments(id, issue.getRef_id()));
            res.add(issue);
        }

        return res;
    }

    public List<Comment> findAllComments(String id, String iid){
        List<Comment> res = new ArrayList<>();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<CommentSearch[]> request = new HttpEntity<>(null, headers);
        ResponseEntity<CommentSearch[]> response = restTemplate
                .exchange(uri + id + "/issues/" + iid + "/discussions", HttpMethod.GET,
                        request, CommentSearch[].class);

        for(CommentSearch cs: response.getBody()){
            for(Note n : cs.getNotes()){
                Comment comment = Comment.ofFormat(n);
                comment.setAuthor(User.ofFormat(n.getAuthor()));
                res.add(comment);
            }
        }

        return res;
    }
}
