package com.scrambled.recipeapp.repository;

import com.scrambled.recipeapp.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
// so much code trust trust star this repo if you really are digging through the files to find this.
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
}
