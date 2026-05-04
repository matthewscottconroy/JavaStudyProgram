package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class EvolutionaryProgrammingQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.EVOLUTIONARY_PROGRAMMING; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("evo-mc-01", Topic.EVOLUTIONARY_PROGRAMMING, 4,
                "What is the purpose of a fitness function in a genetic algorithm?",
                "To generate random initial solutions",
                "To evaluate and score how well a candidate solution solves the problem",
                "To mutate genes in the population",
                "To control the size of the population",
                "b",
                "The fitness function maps a candidate solution (individual) to a numeric score. "
                + "Higher fitness = better solution. The GA uses fitness scores to guide selection: "
                + "fitter individuals are more likely to be selected for reproduction. "
                + "Designing a good fitness function is often the hardest part of applying a GA."),

            mc("evo-mc-02", Topic.EVOLUTIONARY_PROGRAMMING, 4,
                "What is crossover in genetic algorithms?",
                "Replacing low-fitness individuals with random new ones",
                "Combining parts of two parent solutions to produce offspring",
                "Flipping random bits in a single individual",
                "Evaluating the fitness of the entire population",
                "b",
                "Crossover (recombination) takes two parent solutions and swaps portions to create children. "
                + "Example (single-point): parents [1,0,1,1,0] and [0,1,0,0,1] → child [1,0,0,0,1]. "
                + "This mimics biological reproduction and allows beneficial traits from both parents to combine."),

            mc("evo-mc-03", Topic.EVOLUTIONARY_PROGRAMMING, 4,
                "What is mutation in a genetic algorithm?",
                "Selecting the top 10% of the population for the next generation",
                "A small random change applied to an individual to maintain diversity",
                "Copying the best individual into the next generation unchanged",
                "Removing all individuals below a fitness threshold",
                "b",
                "Mutation randomly alters one or more genes with a small probability. "
                + "It prevents the population from converging prematurely on a local optimum "
                + "by introducing new genetic material. Mutation rate is typically low (0.1%–1%) — "
                + "too high destroys good solutions; too low risks stagnation."),

            mc("evo-mc-04", Topic.EVOLUTIONARY_PROGRAMMING, 4,
                "What is 'elitism' in the context of genetic algorithms?",
                "Giving the fittest individuals extra mutation chances",
                "Automatically carrying the best individual(s) unchanged into the next generation",
                "Initializing the population with only high-quality individuals",
                "Running the GA only on high-performance hardware",
                "b",
                "Elitism guarantees that the best solution found so far is never lost due to crossover/mutation. "
                + "Typically the top 1-2 individuals are copied directly to the next generation. "
                + "This ensures monotonic improvement in best-so-far fitness across generations."),

            trace("evo-tr-01", Topic.EVOLUTIONARY_PROGRAMMING, 4,
                "After one generation with elitism, what is the guaranteed minimum best fitness?",
                "// Generation 0 best fitness: 0.87\n"
                + "// Elitism copies best individual unchanged to generation 1\n"
                + "// After crossover and mutation, generation 1 is evaluated\n"
                + "// New best candidate in generation 1 has fitness 0.79",
                "0.87",
                "Elitism carries the generation 0 best (0.87) unchanged to generation 1. "
                + "Even though the new best candidate scored 0.79, the elite individual ensures the overall "
                + "best fitness in generation 1 is at least 0.87."),

            debug("evo-db-01", Topic.EVOLUTIONARY_PROGRAMMING, 4,
                "The GA converges too quickly to a poor solution. What is the most likely cause?",
                "// Population size: 10\n"
                + "// Mutation rate: 0.0001 (very low)\n"
                + "// Generations: 500\n"
                + "// Result: stuck at fitness 0.60, optimal is ~0.95",
                "The population size is too large",
                "500 generations is too many",
                "Premature convergence due to very low mutation rate and small population — diversity lost early",
                "Fitness values above 0.5 cannot be improved by genetic algorithms",
                "c",
                "Premature convergence: the small population quickly becomes genetically similar, "
                + "and the near-zero mutation rate cannot reintroduce diversity. "
                + "The GA gets trapped in a local optimum. "
                + "Fixes: increase population size (50-200+), raise mutation rate (0.01-0.05), "
                + "or use diversity preservation techniques like niching or island models."),

            codegen("evo-cg-01", Topic.EVOLUTIONARY_PROGRAMMING, 4,
                "Which best represents a simple binary chromosome mutation (flip one bit)?",
                "chromosome[0] = 1;",
                "int idx = rng.nextInt(chromosome.length); chromosome[idx] = 1 - chromosome[idx];",
                "Arrays.fill(chromosome, 0);",
                "chromosome = new int[chromosome.length];",
                "b",
                "Randomly pick one position (rng.nextInt(length)) and flip the bit: 0→1 or 1→0 using (1 - bit). "
                + "Option A always sets position 0 to 1 (not random). "
                + "Option C zeroes all genes (destroys the individual). "
                + "Option D resets to default values (all 0s), losing all information."),

            mc("evo-mc-05", Topic.EVOLUTIONARY_PROGRAMMING, 4,
                "What is tournament selection in genetic algorithms?",
                "Selecting the single best individual from the entire population",
                "Randomly picking a small subset of individuals and selecting the best one among them",
                "Selecting individuals in the order they were created",
                "Removing the worst individuals from the population",
                "b",
                "Tournament selection picks k random individuals, then selects the fittest. "
                + "k=2 is common (binary tournament). Larger k = stronger selection pressure. "
                + "It maintains some diversity while favoring better individuals, unlike pure rank selection."),

            mc("evo-mc-06", Topic.EVOLUTIONARY_PROGRAMMING, 4,
                "What is the main difference between a genetic algorithm (GA) and a genetic programming (GP) approach?",
                "GA uses floating-point genes; GP uses binary genes",
                "GA evolves fixed-length solutions (e.g., parameter vectors); GP evolves variable-length programs (e.g., expression trees)",
                "GP can only optimize text; GA can only optimize numbers",
                "They are identical — just different names for the same algorithm",
                "b",
                "GA: typically evolves fixed-length chromosomes representing solutions. "
                + "GP: evolves programs represented as trees (e.g., parse trees or LISP expressions). "
                + "GP is used when you want to discover the structure of a program, not just its parameters."),

            mc("evo-mc-07", Topic.EVOLUTIONARY_PROGRAMMING, 4,
                "What is 'fitness sharing' used for in genetic algorithms?",
                "Distributing the fitness computation across multiple threads",
                "Penalizing individuals in overcrowded regions of the search space to promote diversity",
                "Sharing the best fitness across all individuals equally",
                "Averaging the fitness of parent and offspring",
                "b",
                "Fitness sharing reduces the effective fitness of individuals that are similar to many others. "
                + "This maintains population diversity and helps GAs explore multiple niches (local optima) simultaneously. "
                + "It prevents the GA from converging entirely on one region of the search space."),

            trace("evo-tr-02", Topic.EVOLUTIONARY_PROGRAMMING, 4,
                "After one-point crossover at position 3, what are the two offspring?",
                "// Parent 1: [1, 0, 1, | 0, 1]\n"
                + "// Parent 2: [0, 1, 0, | 1, 0]\n"
                + "// Crossover point after position 3 (1-indexed)",
                "Offspring 1: [1,0,1,1,0]  Offspring 2: [0,1,0,0,1]",
                "One-point crossover: keep parent 1's first 3 genes, swap the rest. "
                + "Offspring 1 = P1[1..3] + P2[4..5] = [1,0,1,1,0]. "
                + "Offspring 2 = P2[1..3] + P1[4..5] = [0,1,0,0,1]."),

            trace("evo-tr-03", Topic.EVOLUTIONARY_PROGRAMMING, 4,
                "A population has 4 individuals with fitness [0.2, 0.5, 0.8, 0.3]. Which is selected by tournament (k=2) when the randomly chosen pair is individual 1 (0.2) and individual 3 (0.8)?",
                "// Tournament size k=2: compare two random individuals",
                "Individual 3 (fitness 0.8)",
                "Tournament selection picks the better individual from the randomly chosen pair. "
                + "Individual 3 with fitness 0.8 beats individual 1 with fitness 0.2."),

            debug("evo-db-02", Topic.EVOLUTIONARY_PROGRAMMING, 4,
                "The best fitness stays at exactly 0.50 for all 100 generations. What is most likely wrong?",
                "// Mutation rate: 0.0\n"
                + "// Crossover rate: 0.8\n"
                + "// Elitism: yes (top 1 preserved)",
                "Elitism is causing the algorithm to freeze",
                "The crossover rate is too high",
                "Mutation rate of 0.0 means no new genetic material is ever introduced — crossover shuffles existing genes but cannot escape local optima",
                "100 generations is too few",
                "c",
                "With zero mutation, the GA can only recombine genes already present in the population. "
                + "Once the population converges, crossover produces identical individuals and no improvement is possible. "
                + "Mutation is essential for escaping local optima. Even a small rate (0.01) can help significantly."),

            codegen("evo-cg-02", Topic.EVOLUTIONARY_PROGRAMMING, 4,
                "Which correctly performs roulette-wheel (fitness-proportionate) selection from a population?",
                "Individual best = population.stream().max(Comparator.comparingDouble(Individual::getFitness)).orElseThrow();",
                "double totalFitness = population.stream().mapToDouble(Individual::getFitness).sum(); double pick = rng.nextDouble() * totalFitness; double running = 0; for (Individual ind : population) { running += ind.getFitness(); if (running >= pick) return ind; }",
                "Individual random = population.get(rng.nextInt(population.size())); return random;",
                "return population.get(0);",
                "b",
                "Roulette-wheel selection: spin a virtual wheel where each slot's size is proportional to fitness. "
                + "Sum all fitnesses (totalFitness), pick a random threshold, walk through the population accumulating fitness "
                + "until you exceed the threshold. "
                + "Option A: selects the always best (not probabilistic). Option C: uniform random (ignores fitness).")
        );
    }
}
