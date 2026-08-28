package com.studyprogram.questions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

/** Jackson deserialization target for a single question JSON file. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonQuestionDto {
    public String id;
    public String type;           // MULTIPLE_CHOICE | TRACING | DEBUGGING | CODE_GENERATION | CODING
    public int difficulty;
    public String prompt;
    public String code;
    public List<String> choices      = Collections.emptyList();
    public String answer;
    public String explanation        = "";
    public List<String> alternatives = Collections.emptyList();
    public List<String> hints        = Collections.emptyList();
    public String starterCode;    // CODING only
    public String testCode;       // CODING only
    public List<String> relatedTopics = Collections.emptyList(); // prerequisite Topic names
    public java.util.Map<String, String> starterFiles  = Collections.emptyMap(); // multi-file CODING
    public java.util.Map<String, String> solutionFiles = Collections.emptyMap(); // multi-file CODING
}
