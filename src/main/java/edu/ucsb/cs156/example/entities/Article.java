// This file should be deleted or renamed to Article.java
// The Article.java file you created should be used instead

package edu.ucsb.cs156.example.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This is a JPA entity that represents an Article.
 * 
 * Articles contain information about web resources that users have submitted.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "articles")
public class Article {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String title;
  private String url;
  private String explanation;
  private String email;
  private String dateAdded;
}
