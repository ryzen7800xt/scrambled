package com.scrambled.recipeapp.controller;

import com.scrambled.recipeapp.model.Comment;
import com.scrambled.recipeapp.model.InboxMessage;
import com.scrambled.recipeapp.model.Recipe;
import com.scrambled.recipeapp.model.User;
import com.scrambled.recipeapp.repository.CommentRepository;
import com.scrambled.recipeapp.repository.InboxMessageRepository;
import com.scrambled.recipeapp.repository.RecipeRepository;
import com.scrambled.recipeapp.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private InboxMessageRepository inboxRepository;

    @Autowired
    private AuthService authService;

    @GetMapping
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    @PostMapping
    @Transactional
    public Recipe createRecipe(
            @RequestBody Recipe recipe,
            @CookieValue(name = AuthService.SESSION_COOKIE, required = false) String token) {
        if (recipe.getName() == null || recipe.getName().isBlank()
                || recipe.getRealRecipe() == null || recipe.getRealRecipe().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A recipe name and instructions are required.");
        }
        User author = authService.requireUser(token);
        recipe.setId(null);
        recipe.setUpvotes(0);
        recipe.setDownvotes(0);
        recipe.setAuthor(author);
        recipe.setComments(new ArrayList<>());
        Recipe savedRecipe = recipeRepository.save(recipe);
        InboxMessage message = new InboxMessage();
        message.setMessage("Your recipe \"" + savedRecipe.getName() + "\" has been added.");
        message.setCreatedAt(Instant.now());
        message.setRead(false);
        message.setUser(author);
        inboxRepository.save(message);
        return savedRecipe;
    }

    @PostMapping("/{id}/upvote")
    public Recipe upvoteRecipe(@PathVariable Long id) {
        Recipe recipe = findRecipe(id);
        recipe.setUpvotes(recipe.getUpvotes() + 1);
        return recipeRepository.save(recipe);
    }

    @PostMapping("/{id}/downvote")
    public Recipe downvoteRecipe(@PathVariable Long id) {
        Recipe recipe = findRecipe(id);
        recipe.setDownvotes(recipe.getDownvotes() + 1);
        return recipeRepository.save(recipe);
    }

    @PostMapping("/{id}/comments")
    public Comment addComment(@PathVariable Long id, @RequestBody Comment comment) {
        if (comment.getText() == null || comment.getText().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A comment cannot be empty.");
        }
        Recipe recipe = findRecipe(id);
        comment.setRecipe(recipe);
        comment.setUser(null);
        return commentRepository.save(comment);
    }

    @SuppressWarnings("null")
    private Recipe findRecipe(Long id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found."));
    }
}
