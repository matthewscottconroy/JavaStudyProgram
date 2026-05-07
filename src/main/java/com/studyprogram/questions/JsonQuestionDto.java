package com.studyprogram.questions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

/** Jackson deserialization target for a single question JSON file. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonQuestionDto {
    public String id;
    public String type;           // MULTIPLE_CHOICE | TRACING | DEBUGGING | CODE_GENERATION
    public int difficulty;
    public String prompt;
    public String code;
    public List<String> choices      = Collections.emptyList();
    public String answer;
    public String explanation        = "";
    public List<String> alternatives = Collections.emptyList();
    public List<String> hints        = Collections.emptyList();
}
