package com.scrambled.recipeapp;

import com.scrambled.recipeapp.model.Recipe;
import com.scrambled.recipeapp.repository.RecipeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecipeDataLoader implements CommandLineRunner {
    private final RecipeRepository recipeRepository;

    public RecipeDataLoader(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @SuppressWarnings("null")
    @Override
    public void run(String... args) {
        if (recipeRepository.count() > 0) {
            return;
        }

        recipeRepository.saveAll(List.of(
                recipe("Fluffy Sunday Pancakes",
                        "Ingredients\n• 1 cup all-purpose flour\n• 1 tablespoon sugar\n• 1 teaspoon baking powder\n• ¾ cup milk\n• 1 egg\n• 2 tablespoons melted butter\n\nMethod\n1. Whisk the flour, sugar, and baking powder together.\n2. Add the milk, egg, and butter; stir just until combined.\n3. Cook small scoops in a warm buttered pan until golden on both sides. Serve with fruit or maple syrup."),
                recipe("Creamy Tomato Pasta",
                        "Ingredients\n• 250 g pasta\n• 1 tablespoon olive oil\n• 2 cloves garlic, minced\n• 1 can crushed tomatoes\n• ½ cup cream\n• Salt, pepper, and parmesan\n\nMethod\n1. Cook the pasta in salted water until just tender.\n2. Warm the oil and garlic, then stir in tomatoes and simmer for 10 minutes.\n3. Add cream, season to taste, and toss with drained pasta. Finish with parmesan."),
                recipe("Crispy Veggie Fried Rice",
                        "Ingredients\n• 2 cups cold cooked rice\n• 1 cup mixed vegetables\n• 2 eggs\n• 2 tablespoons soy sauce\n• 1 teaspoon sesame oil\n• Sliced spring onion\n\nMethod\n1. Scramble the eggs in a hot pan and set aside.\n2. Stir-fry the vegetables until tender-crisp, then add the rice.\n3. Add soy sauce and sesame oil; toss until hot. Fold the eggs through and top with spring onion."),
                recipe("One-Pan Lemon Chickpeas",
                        "Ingredients\n• 1 can chickpeas, drained\n• 1 tablespoon olive oil\n• 1 clove garlic, minced\n• ½ cup vegetable stock\n• Juice of ½ lemon\n• A handful of baby spinach\n\nMethod\n1. Warm the oil in a pan and cook the garlic for 30 seconds.\n2. Add chickpeas and stock; simmer for 8 minutes.\n3. Stir in lemon juice and spinach. Serve warm with toast or rice.")
        ));
    }

    private Recipe recipe(String name, String details) {
        Recipe recipe = new Recipe();
        recipe.setName(name);
        recipe.setRealRecipe(details);
        return recipe;
    }
}
