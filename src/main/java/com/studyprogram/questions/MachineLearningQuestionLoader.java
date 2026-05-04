package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class MachineLearningQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.MACHINE_LEARNING; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("ml-mc-01", Topic.MACHINE_LEARNING, 4,
                "What is the purpose of a training dataset in machine learning?",
                "Data used to evaluate the model's final accuracy",
                "Data the model learns patterns from by adjusting its parameters",
                "Data that is randomly generated to test edge cases",
                "Data that is encrypted to protect user privacy",
                "b",
                "A training set contains labeled examples (input → expected output). "
                + "The model adjusts its parameters (weights in a neural net, thresholds in a decision tree) "
                + "to minimize prediction errors on this set. "
                + "A separate test set evaluates generalization — how well it performs on unseen data."),

            mc("ml-mc-02", Topic.MACHINE_LEARNING, 4,
                "What is overfitting?",
                "A model that is too slow to train",
                "A model that performs well on training data but poorly on new, unseen data",
                "Training a model with too little data",
                "A model with too few parameters to capture patterns",
                "b",
                "Overfitting occurs when a model memorizes training data noise rather than learning generalizable patterns. "
                + "Signs: very high training accuracy, low test accuracy. "
                + "Mitigations: more training data, regularization (dropout, L2), cross-validation, simpler models."),

            mc("ml-mc-03", Topic.MACHINE_LEARNING, 4,
                "What does a feature represent in machine learning?",
                "A trained neural network node",
                "An input variable used to make predictions",
                "A measure of model accuracy",
                "A type of loss function",
                "b",
                "Features are the measurable properties of the data used as model inputs. "
                + "Example: predicting house prices — features might be square footage, number of rooms, location. "
                + "Feature engineering (selecting and transforming features) often has more impact than model choice."),

            mc("ml-mc-04", Topic.MACHINE_LEARNING, 4,
                "Which Java library is commonly used for machine learning tasks?",
                "java.ml (built-in ML package)",
                "Weka or Deeplearning4j (DL4J)",
                "java.ai.learn",
                "javax.neuralnet",
                "b",
                "Weka provides classic ML algorithms (decision trees, SVM, clustering) in Java. "
                + "Deeplearning4j (DL4J) provides deep learning. "
                + "Many practitioners call Python libraries (scikit-learn, TensorFlow, PyTorch) via subprocess "
                + "or use REST APIs rather than pure Java ML libraries."),

            trace("ml-tr-01", Topic.MACHINE_LEARNING, 4,
                "What is the accuracy of this model?",
                "// 100 test cases: 70 correct predictions, 30 wrong\n"
                + "int correct = 70;\n"
                + "int total = 100;\n"
                + "double accuracy = (double) correct / total;\n"
                + "System.out.printf(\"%.0f%%%n\", accuracy * 100);",
                "70%",
                "accuracy = 70 / 100 = 0.70. Multiplied by 100 and formatted as %.0f%% gives 70%."),

            debug("ml-db-01", Topic.MACHINE_LEARNING, 4,
                "A model achieves 99% accuracy on training data but 55% on test data. What is wrong?",
                "double trainAcc = 0.99;\n"
                + "double testAcc  = 0.55;\n"
                + "// Model deployed to production",
                "The training and test accuracy should be averaged",
                "The model is overfitting — it memorized training data and fails to generalize",
                "99% training accuracy proves the model is correct",
                "The test set is too small to be reliable",
                "b",
                "A 44-point gap between training and test accuracy is a classic overfitting sign. "
                + "The model learned noise specific to the training set. "
                + "Solutions: increase training data, reduce model complexity, apply regularization (dropout, L2), "
                + "use cross-validation to detect this earlier."),

            codegen("ml-cg-01", Topic.MACHINE_LEARNING, 4,
                "Which computes the mean squared error between predictions and actual values?",
                "double mse = predictions.sum() / actuals.sum();",
                "double mse = 0; for (int i = 0; i < n; i++) { double diff = predictions[i] - actuals[i]; mse += diff * diff; } mse /= n;",
                "double mse = Math.abs(predictions[0] - actuals[0]);",
                "double mse = Arrays.stream(predictions).average().orElse(0);",
                "b",
                "MSE = (1/n) * Σ(predicted - actual)². "
                + "Loop over all n samples, accumulate squared differences, divide by n. "
                + "Option A divides sums (not MSE). Option C only checks one element. "
                + "Option D computes mean of predictions, not mean error."),

            mc("ml-mc-05", Topic.MACHINE_LEARNING, 4,
                "What is the difference between classification and regression in machine learning?",
                "Classification predicts numbers; regression predicts categories",
                "Classification predicts a discrete category; regression predicts a continuous numeric value",
                "They are the same — just different names for prediction tasks",
                "Classification works only on images; regression works only on text",
                "b",
                "Classification: predicting which class an input belongs to (spam/not-spam, digit 0-9). "
                + "Regression: predicting a continuous output (house price, temperature). "
                + "Common classification algorithms: logistic regression, decision trees, SVM. "
                + "Common regression algorithms: linear regression, random forest regression."),

            mc("ml-mc-06", Topic.MACHINE_LEARNING, 4,
                "What is cross-validation used for?",
                "Validating that two models produce the same predictions",
                "Estimating model performance on unseen data without needing a separate test set",
                "Comparing the source code of two ML libraries",
                "Ensuring the training and test sets have the same size",
                "b",
                "K-fold cross-validation: split data into K subsets (folds). Train on K-1 folds, test on the remaining fold. "
                + "Repeat K times with a different test fold. Average the K scores for a reliable performance estimate. "
                + "This makes better use of limited data than a single train/test split."),

            mc("ml-mc-07", Topic.MACHINE_LEARNING, 4,
                "What does 'normalization' (feature scaling) do to input data?",
                "Removes duplicate rows from the dataset",
                "Scales feature values to a common range (e.g., 0–1 or mean=0, std=1) to prevent large-scale features from dominating",
                "Converts categorical features to numeric codes",
                "Adds noise to prevent overfitting",
                "b",
                "Features with very different scales (age: 0-100, income: 0-1,000,000) can bias gradient-based models. "
                + "Min-max scaling: x' = (x - min) / (max - min) → range [0,1]. "
                + "Standardization (Z-score): x' = (x - mean) / std → mean=0, std=1. "
                + "Tree-based models don't require scaling; linear and neural models typically do."),

            trace("ml-tr-02", Topic.MACHINE_LEARNING, 4,
                "A model predicts [2.0, 4.0] for two samples; actual values are [3.0, 3.0]. What is the MSE?",
                "double[] pred = {2.0, 4.0};\n"
                + "double[] actual = {3.0, 3.0};\n"
                + "// MSE = ?",
                "1.0",
                "MSE = ((2-3)² + (4-3)²) / 2 = (1 + 1) / 2 = 1.0."),

            trace("ml-tr-03", Topic.MACHINE_LEARNING, 4,
                "What is the accuracy given this confusion matrix result?",
                "// Binary classification on 200 samples:\n"
                + "// True Positives (TP) = 80\n"
                + "// True Negatives (TN) = 90\n"
                + "// False Positives (FP) = 15\n"
                + "// False Negatives (FN) = 15\n"
                + "// Accuracy = (TP + TN) / total",
                "85%",
                "Accuracy = (80 + 90) / 200 = 170 / 200 = 0.85 = 85%."),

            debug("ml-db-02", Topic.MACHINE_LEARNING, 4,
                "A model achieves 95% accuracy on a dataset where 95% of samples are class 'A'. What is the problem?",
                "double accuracy = 0.95;",
                "95% accuracy is always excellent — deploy the model immediately",
                "The model might predict class 'A' for everything (majority class baseline) and have no real predictive power",
                "Accuracy above 90% always indicates overfitting",
                "The dataset is too small at 95% to be useful",
                "b",
                "On imbalanced datasets, a naive model that always predicts the majority class matches the base rate. "
                + "95% accuracy here might mean zero recall for class 'B'. "
                + "Use: precision, recall, F1-score, or AUC-ROC instead of raw accuracy for imbalanced data."),

            codegen("ml-cg-02", Topic.MACHINE_LEARNING, 4,
                "Which correctly normalizes an array to [0,1] range using min-max scaling?",
                "for (int i=0; i<data.length; i++) data[i] = data[i] / 100;",
                "double min = Arrays.stream(data).min().getAsDouble(); double max = Arrays.stream(data).max().getAsDouble(); for (int i=0; i<data.length; i++) data[i] = (data[i]-min)/(max-min);",
                "Arrays.sort(data);",
                "double avg = Arrays.stream(data).average().orElse(0); for (int i=0; i<data.length; i++) data[i] -= avg;",
                "b",
                "Min-max: subtract min, divide by (max-min). Result is in [0,1]. "
                + "Option A divides by hardcoded 100 (not general). Option C sorts (doesn't scale). "
                + "Option D subtracts mean (mean-centering, not normalization).")
        );
    }
}
