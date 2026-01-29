package com.agustinbenitez.addoncreator.core;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages Git operations for projects
 */
public class GitManager {
    private static final Logger logger = LoggerFactory.getLogger(GitManager.class);
    private Git git;
    private Repository repository;

    public static class GitChange {
        public final String filePath;
        public final String type; // MODIFIED, ADDED, DELETED, UNTRACKED

        public GitChange(String filePath, String type) {
            this.filePath = filePath;
            this.type = type;
        }
        
        @Override
        public String toString() {
            return filePath;
        }
    }

    public void openRepository(File projectDir) throws IOException {
        if (git != null) {
            close();
        }

        File gitDir = new File(projectDir, ".git");
        if (!gitDir.exists()) {
            throw new IOException("No git repository found at " + projectDir.getAbsolutePath());
        }

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        repository = builder.setGitDir(gitDir)
                .readEnvironment()
                .findGitDir()
                .build();
        
        git = new Git(repository);
        logger.info("Opened git repository at {}", projectDir.getAbsolutePath());
    }

    public void initRepository(File projectDir) throws GitAPIException {
        if (git != null) {
            close();
        }
        
        git = Git.init().setDirectory(projectDir).call();
        repository = git.getRepository();
        logger.info("Initialized git repository at {}", projectDir.getAbsolutePath());
    }

    public String getCurrentBranch() throws IOException {
        if (repository == null) return null;
        return repository.getBranch();
    }

    public boolean hasUncommittedChanges() throws GitAPIException {
        if (git == null) return false;
        Status status = git.status().call();
        return !status.isClean();
    }

    public List<GitChange> getChanges() throws GitAPIException {
        if (git == null) return List.of();
        Status status = git.status().call();
        List<GitChange> changes = new ArrayList<>();
        
        status.getModified().forEach(f -> changes.add(new GitChange(f, "MODIFIED")));
        status.getChanged().forEach(f -> changes.add(new GitChange(f, "MODIFIED")));
        status.getAdded().forEach(f -> changes.add(new GitChange(f, "ADDED")));
        status.getUntracked().forEach(f -> changes.add(new GitChange(f, "UNTRACKED")));
        status.getMissing().forEach(f -> changes.add(new GitChange(f, "DELETED")));
        status.getRemoved().forEach(f -> changes.add(new GitChange(f, "DELETED")));
        
        return changes;
    }

    public void add(Collection<String> filePatterns) throws GitAPIException {
        if (git == null) return;
        var command = git.add();
        for (String pattern : filePatterns) {
            command.addFilepattern(pattern);
        }
        command.call();
    }
    
    public String getDiff(String filePath) {
        if (git == null) return "";
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Check if file is untracked
            Status status = git.status().addPath(filePath).call();
            if (status.getUntracked().contains(filePath)) {
                 File file = new File(repository.getWorkTree(), filePath);
                 if (file.exists()) {
                     return Files.readString(file.toPath());
                 }
                 return "";
            }

            ObjectId headId = repository.resolve("HEAD");
            if (headId == null) {
                // Initial commit or empty repo
                File file = new File(repository.getWorkTree(), filePath);
                 if (file.exists()) {
                     return Files.readString(file.toPath());
                 }
                return "";
            }

            DiffFormatter formatter = new DiffFormatter(out);
            formatter.setRepository(repository);
            
            try (ObjectReader reader = repository.newObjectReader()) {
                CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
                RevWalk walk = new RevWalk(repository);
                RevCommit commit = walk.parseCommit(headId);
                RevTree tree = commit.getTree();
                oldTreeParser.reset(reader, tree.getId());
                
                FileTreeIterator newTreeParser = new FileTreeIterator(repository);
                
                formatter.setPathFilter(PathFilter.create(filePath));
                List<DiffEntry> diffs = formatter.scan(oldTreeParser, newTreeParser);
                
                for (DiffEntry entry : diffs) {
                    formatter.format(entry);
                }
            }
            
            return out.toString();
            
        } catch (Exception e) {
            logger.error("Failed to get diff for " + filePath, e);
            return "Error getting diff: " + e.getMessage();
        }
    }

    public String getFileContentFromHead(String filePath) {
        if (repository == null) return "";
        try {
            ObjectId headId = repository.resolve("HEAD");
            if (headId == null) return "";

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(headId);
                RevTree tree = commit.getTree();
                
                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);
                    treeWalk.setFilter(PathFilter.create(filePath));
                    
                    if (!treeWalk.next()) {
                        return ""; // File not found in HEAD
                    }
                    
                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repository.open(objectId);
                    return new String(loader.getBytes(), StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to read file from HEAD: " + filePath, e);
            return "";
        }
    }

    public void push(String username, String password) throws GitAPIException {
        if (git == null) return;
        CredentialsProvider credentials = new UsernamePasswordCredentialsProvider(username, password);
        git.push().setCredentialsProvider(credentials).call();
        logger.info("Pushed to remote");
    }

    public void fetch(String username, String password) throws GitAPIException {
        if (git == null) return;
        CredentialsProvider credentials = new UsernamePasswordCredentialsProvider(username, password);
        git.fetch().setCredentialsProvider(credentials).call();
        logger.info("Fetched from remote");
    }

    public void addAll() throws GitAPIException {
        if (git == null) return;
        git.add().addFilepattern(".").call();
    }

    public void commit(String message) throws GitAPIException {
        if (git == null) return;
        git.commit().setMessage(message).call();
        logger.info("Committed changes with message: {}", message);
    }

    public void addRemote(String name, String url) throws GitAPIException, URISyntaxException {
        if (git == null) return;
        git.remoteAdd().setName(name).setUri(new URIish(url)).call();
        logger.info("Added remote {} with url {}", name, url);
    }

    public void close() {
        if (git != null) {
            git.close();
            git = null;
        }
        if (repository != null) {
            repository.close();
            repository = null;
        }
    }
    
    public boolean isRepositoryOpen() {
        return git != null;
    }
}
