package ch17.ai;

import java.util.List;

/** The Java type we want the model's answer converted into. */
public record MovieRecommendation(String title, int year, List<String> genres, double score) {}
