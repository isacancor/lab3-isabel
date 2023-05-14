package Miner.GitLab.service;


import Miner.GitLab.model.comment.Comment;
import Miner.GitLab.model.comment.CommentSearch;
import Miner.GitLab.model.comment.Note;
import Miner.GitLab.model.commit.Commit;
import Miner.GitLab.model.commit.CommitSearch;
import Miner.GitLab.model.gitMiner.Example;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ProjectService {


    @Autowired
    RestTemplate restTemplate;

    private final String uri = "https://gitlab.com/api/v4/projects/";
    private final String token = "glpat-3aox7aT7i7sWL_MERLPt";

    public Project findOne(String id,
                           Integer maxPage, Integer sinceCommits, Integer sinceIssues){
        ProjectSearch p = restTemplate.getForObject(uri + id, ProjectSearch.class);
        Project project = Project.ofFormat(p);

        if(!findAllCommits(project.getId(), sinceCommits, maxPage).isEmpty()){
            project.setCommits(findAllCommits(project.getId(), sinceCommits, maxPage));
        }
        if(!findAllIssues(project.getId(), sinceIssues, maxPage).isEmpty()){
            project.setIssues(findAllIssues(project.getId(), sinceIssues, maxPage));
        }

        return project;
    }

    public List<Commit> findAllCommits(String id, Integer sinceCommits, Integer maxPage){
        List<Commit> commits = new ArrayList<>();
        List<CommitSearch> cs = new ArrayList<>();
        int page = 1;
        Boolean hasNext = true;

        while(hasNext && page <= maxPage){
            UriComponentsBuilder uriBuilder =
                    UriComponentsBuilder.fromUriString(uri + id + "/repository/commits")
                            .queryParam("page", page);
            HttpEntity<?> entity = new HttpEntity<>(new HttpHeaders());

            ResponseEntity<CommitSearch[]> response =
                    restTemplate.exchange(
                            uriBuilder.toUriString(),
                            HttpMethod.GET,
                            entity,
                            CommitSearch[].class
                    );
            List<CommitSearch> pageCommit = Arrays.stream(response.getBody()).toList();
            cs.addAll(pageCommit);

            String linkHeader = response.getHeaders().getFirst("Link");
            if(linkHeader == null || !linkHeader.contains("rel=\"next\"")){
                hasNext = false;
            }else{
                page++;
            }
        }

        for(CommitSearch c: cs){
            String[] d = c.getCommittedDate().split("T")[0].split("-");

            LocalDate date = LocalDate.of(Integer.valueOf(d[0]), Integer.valueOf(d[1]),
                    Integer.valueOf(d[2]));

            if(date.datesUntil(LocalDate.now()).toList().stream().count() < sinceCommits){
                commits.add(Commit.ofFormat(c));
            }
        }

        return commits;
    }

    public List<Issue> findAllIssues(String id, Integer sinceIssues, Integer maxPage){
        List<Issue> issues = new ArrayList<>();
        List<IssueSearch> is = new ArrayList<>();
        int page = 1;
        Boolean hasNext = true;

        while(hasNext && page <= maxPage){
            UriComponentsBuilder uriBuilder =
                    UriComponentsBuilder.fromUriString(uri + id + "/issues")
                            .queryParam("page", page);
            HttpEntity<?> entity = new HttpEntity<>(new HttpHeaders());

            ResponseEntity<IssueSearch[]> response =
                    restTemplate.exchange(
                            uriBuilder.toUriString(),
                            HttpMethod.GET,
                            entity,
                            IssueSearch[].class
                    );
            List<IssueSearch> pageCommit = Arrays.stream(response.getBody()).toList();
            is.addAll(pageCommit);

            String linkHeader = response.getHeaders().getFirst("Link");
            if(linkHeader == null || !linkHeader.contains("rel=\"next\"")){
                hasNext = false;
            }else{
                page++;
            }
        }

        for(IssueSearch i: is){
            String[] d = i.getUpdatedAt().split("T")[0].split("-");

            LocalDate date = LocalDate.of(Integer.valueOf(d[0]), Integer.valueOf(d[1]),
                    Integer.valueOf(d[2]));

            if(date.datesUntil(LocalDate.now()).toList().stream().count() < sinceIssues){
                Issue issue = Issue.ofFormat(i);

                if(i.getAssignee() != null){
                    issue.setAsignee(User.ofFormat(i.getAssignee()));
                }
                issue.setAuthor(User.ofFormat(i.getAuthor()));
                issue.setComments(findAllComments(id, issue.getRef_id(), maxPage));
                issues.add(issue);
            }
        }

        return issues;
    }

    public List<Comment> findAllComments(String id, String iid, Integer maxPage){
        List<Comment> res = new ArrayList<>();
        List<CommentSearch> cs = new ArrayList<>();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        int page = 1;
        Boolean hasNext = true;

        while(hasNext && page <= maxPage){
            UriComponentsBuilder uriBuilder =
                    UriComponentsBuilder.fromUriString(uri + id + "/issues/" + iid + "/discussions")
                            .queryParam("page", page);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<CommentSearch[]> response =
                    restTemplate.exchange(
                            uriBuilder.toUriString(),
                            HttpMethod.GET,
                            entity,
                            CommentSearch[].class
                    );
            List<CommentSearch> pageCommit = Arrays.stream(response.getBody()).toList();
            cs.addAll(pageCommit);

            String linkHeader = response.getHeaders().getFirst("Link");
            if(linkHeader == null || !linkHeader.contains("rel=\"next\"")){
                hasNext = false;
            }else{
                page++;
            }
        }

        for(CommentSearch commentSearch: cs){
            for(Note n : commentSearch.getNotes()){
                Comment comment = Comment.ofFormat(n);
                comment.setAuthor(User.ofFormat(n.getAuthor()));
                res.add(comment);
            }
        }

        return res;
    }

    public Example postGitMiner(String id, Integer sinceIssues, Integer sinceCommit, Integer maxPages){
        String uri = "http://localhost:8080/gitminer/projects";
        Project project = findOne(id, sinceCommit, sinceIssues, maxPages);
        Example e = Example.ofFormat(project);

        ResponseEntity<Example> response = restTemplate.postForEntity(uri, e, Example.class);

        return response.getBody();
    }
}
