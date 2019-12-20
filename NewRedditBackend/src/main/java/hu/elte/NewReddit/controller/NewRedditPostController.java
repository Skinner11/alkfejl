/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.elte.NewReddit.controller;

import hu.elte.NewReddit.model.ApiResponse;
import hu.elte.NewReddit.model.Comment;
import hu.elte.NewReddit.model.RedditPost;
import hu.elte.NewReddit.model.Subreddit;
import hu.elte.NewReddit.model.User;
import hu.elte.NewReddit.repository.CommentRepository;
import hu.elte.NewReddit.repository.RedditPostRepository;
import hu.elte.NewReddit.repository.SubredditRepository;
import hu.elte.NewReddit.repository.UserRepository;
import hu.elte.NewReddit.security.AuthenticatedUser;
import java.security.Principal;
import java.util.Date;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 * @author Maffia
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/posts")
public class NewRedditPostController {

	@Autowired
	private RedditPostRepository redditPostRepository;

	@Autowired
	private SubredditRepository subredditRepository;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private AuthenticatedUser authenticatedUser;

	@Autowired
	private UserRepository userRepository;

	@GetMapping("")
	public ResponseEntity<Iterable<RedditPost>> getAllPosts() {
		return new ResponseEntity(redditPostRepository.findAll(), HttpStatus.OK);
	}

	@GetMapping(value = "/{id}")
	public ResponseEntity<RedditPost> getPostById(@PathVariable Long id) {
		Optional<RedditPost> res = redditPostRepository.findById(id);
		if (res.isPresent()) {
			return new ResponseEntity(res, HttpStatus.OK);
		}
		return new ResponseEntity(null, HttpStatus.NOT_FOUND);
	}

	@GetMapping(value = "/{id}/comments")
	public ResponseEntity<Iterable<Comment>> getCommentsToPost(@PathVariable Long id) {
		Optional<RedditPost> post = redditPostRepository.findById(id);
		if (post.isPresent()) {
			return new ResponseEntity(commentRepository.findAllByPost(post.get()), HttpStatus.OK);
		}
		return new ResponseEntity(null, HttpStatus.NOT_FOUND);
	}

	@PostMapping(value = "/{id}/comments")
	public ResponseEntity saveCommentToPost(@PathVariable Long id, @RequestBody Comment reqComment, Principal principal) {
		Optional<RedditPost> post = redditPostRepository.findById(id);
		if (post.isPresent() && principal != null) {
			Optional<User> user = userRepository.findByUsername(principal.getName());
			Comment comment = new Comment();
			comment.setPost(post.get());
			comment.setUser(user.get());
			comment.setText(reqComment.getText());
			comment.setVotes(1);
			commentRepository.save(comment);
			return new ResponseEntity(HttpStatus.OK);
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND);
	}

	@GetMapping(value = "/subreddits/{subbredditId}")
	public ResponseEntity<Iterable<RedditPost>> getPostsBySubbreddit(@PathVariable Long subbredditId) {
		Optional<Subreddit> subred = subredditRepository.findById(subbredditId);
		if (subred.isPresent()) {
			return new ResponseEntity(redditPostRepository.findAllBySubreddit(subred.get()), HttpStatus.OK);
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND);
	}

	@DeleteMapping("/{postId}")
	public ResponseEntity<ApiResponse> deltePostById(@PathVariable Long postId) {

		String date = new Date(System.currentTimeMillis()).toString();

		Optional<RedditPost> post = redditPostRepository.findById(postId);

		if (post.isPresent()) {
			redditPostRepository.delete(post.get());
			return new ResponseEntity(new ApiResponse(200, "Post successfully deleted", date), HttpStatus.OK);
		}
		return new ResponseEntity(new ApiResponse(200, "Post not found", date), HttpStatus.NOT_FOUND);
	}
}
